/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.networking.p2p.libp2p;

import static tech.pegasys.teku.logging.StatusLogger.STATUS_LOG;
import static tech.pegasys.teku.util.async.SafeFuture.failedFuture;
import static tech.pegasys.teku.util.async.SafeFuture.reportExceptions;

import identify.pb.IdentifyOuterClass;
import io.libp2p.core.Host;
import io.libp2p.core.PeerId;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.dsl.Builder.Defaults;
import io.libp2p.core.dsl.BuilderJKt;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.core.multistream.ProtocolBinding;
import io.libp2p.core.pubsub.PubsubApi;
import io.libp2p.core.pubsub.PubsubApiKt;
import io.libp2p.core.pubsub.PubsubPublisherApi;
import io.libp2p.etc.types.ByteArrayExtKt;
import io.libp2p.mux.mplex.MplexStreamMuxer;
import io.libp2p.protocol.Identify;
import io.libp2p.protocol.Ping;
import io.libp2p.pubsub.gossip.Gossip;
import io.libp2p.pubsub.gossip.GossipParams;
import io.libp2p.pubsub.gossip.GossipRouter;
import io.libp2p.security.noise.NoiseXXSecureChannel;
import io.libp2p.security.secio.SecIoSecureChannel;
import io.libp2p.transport.tcp.TcpTransport;
import io.netty.channel.ChannelHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.Hash;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import tech.pegasys.teku.networking.p2p.connection.ReputationManager;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryPeer;
import tech.pegasys.teku.networking.p2p.gossip.GossipNetwork;
import tech.pegasys.teku.networking.p2p.gossip.TopicChannel;
import tech.pegasys.teku.networking.p2p.gossip.TopicHandler;
import tech.pegasys.teku.networking.p2p.libp2p.gossip.LibP2PGossipNetwork;
import tech.pegasys.teku.networking.p2p.libp2p.rpc.RpcHandler;
import tech.pegasys.teku.networking.p2p.network.NetworkConfig;
import tech.pegasys.teku.networking.p2p.network.P2PNetwork;
import tech.pegasys.teku.networking.p2p.network.PeerAddress;
import tech.pegasys.teku.networking.p2p.network.PeerHandler;
import tech.pegasys.teku.networking.p2p.peer.NodeId;
import tech.pegasys.teku.networking.p2p.peer.Peer;
import tech.pegasys.teku.networking.p2p.peer.PeerConnectedSubscriber;
import tech.pegasys.teku.networking.p2p.rpc.RpcMethod;
import tech.pegasys.teku.util.async.AsyncRunner;
import tech.pegasys.teku.util.async.SafeFuture;
import tech.pegasys.teku.util.cli.VersionProvider;

public class LibP2PNetwork implements P2PNetwork<Peer> {

  private static final Logger LOG = LogManager.getLogger();

  private final PrivKey privKey;
  private final NodeId nodeId;

  private final Host host;
  private final PeerManager peerManager;
  private final Multiaddr advertisedAddr;
  private final Gossip gossip;
  private final GossipNetwork gossipNetwork;
  private final NetworkConfig config;

  private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);
  private final Map<RpcMethod, RpcHandler> rpcHandlers = new ConcurrentHashMap<>();
  private final int listenPort;

  public LibP2PNetwork(
      final AsyncRunner asyncRunner,
      final NetworkConfig config,
      final ReputationManager reputationManager,
      final MetricsSystem metricsSystem,
      final List<RpcMethod> rpcMethods,
      final List<PeerHandler> peerHandlers) {
    this.privKey = config.getPrivateKey();
    this.nodeId = new LibP2PNodeId(PeerId.fromPubKey(privKey.publicKey()));
    this.config = config;

    advertisedAddr =
        MultiaddrUtil.fromInetSocketAddress(
            new InetSocketAddress(config.getAdvertisedIp(), config.getAdvertisedPort()), nodeId);
    this.listenPort = config.getListenPort();

    // Setup gossip
    gossip = createGossip();
    final PubsubPublisherApi publisher = gossip.createPublisher(privKey, new Random().nextLong());
    gossipNetwork = new LibP2PGossipNetwork(gossip, publisher);

    // Setup rpc methods
    rpcMethods.forEach(method -> rpcHandlers.put(method, new RpcHandler(asyncRunner, method)));

    // Setup peers
    peerManager = new PeerManager(metricsSystem, reputationManager, peerHandlers, rpcHandlers);

    // temporary flag
    // set true for Lighthouse compatibility
    // set false for Prysm compatibility
    // TODO remove when all clients adjust the same Noise spec
    NoiseXXSecureChannel.setRustInteroperability(false);
    final Multiaddr listenAddr =
        MultiaddrUtil.fromInetSocketAddress(
            new InetSocketAddress(config.getNetworkInterface(), config.getListenPort()));
    host =
        BuilderJKt.hostJ(
            Defaults.None,
            b -> {
              b.getIdentity().setFactory(() -> privKey);
              b.getTransports().add(TcpTransport::new);
              b.getSecureChannels().add(NoiseXXSecureChannel::new);
              b.getSecureChannels().add(SecIoSecureChannel::new); // to be removed later
              b.getMuxers().add(MplexStreamMuxer::new);

              b.getNetwork().listen(listenAddr.toString());

              b.getProtocols().addAll(getDefaultProtocols());
              b.getProtocols().addAll(rpcHandlers.values());

              if (config.getWireLogsConfig().isLogWireCipher()) {
                b.getDebug().getBeforeSecureHandler().setLogger(LogLevel.DEBUG, "wire.ciphered");
              }
              if (config.getWireLogsConfig().isLogWirePlain()) {
                b.getDebug().getAfterSecureHandler().setLogger(LogLevel.DEBUG, "wire.plain");
              }
              if (config.getWireLogsConfig().isLogWireMuxFrames()) {
                b.getDebug().getMuxFramesHandler().setLogger(LogLevel.DEBUG, "wire.mux");
              }

              b.getConnectionHandlers().add(peerManager);
            });
  }

  private Gossip createGossip() {
    GossipParams gossipParams =
        GossipParams.builder()
            .D(config.getGossipConfig().getD())
            .DLow(config.getGossipConfig().getDLow())
            .DHigh(config.getGossipConfig().getDHigh())
            .DLazy(config.getGossipConfig().getDLazy())
            .fanoutTTL(config.getGossipConfig().getFanoutTTL())
            .gossipSize(config.getGossipConfig().getAdvertise())
            .gossipHistoryLength(config.getGossipConfig().getHistory())
            .heartbeatInterval(config.getGossipConfig().getHeartbeatInterval())
            .build();

    GossipRouter router = new GossipRouter(gossipParams);
    router.setMessageIdGenerator(
        msg ->
            Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(Hash.sha2_256(msg.getData().toByteArray())));

    ChannelHandler debugHandler =
        config.getWireLogsConfig().isLogWireGossip()
            ? new LoggingHandler("wire.gossip", LogLevel.DEBUG)
            : null;
    PubsubApi pubsubApi = PubsubApiKt.createPubsubApi(router);

    return new Gossip(router, pubsubApi, debugHandler);
  }

  private List<ProtocolBinding<?>> getDefaultProtocols() {
    final Ping ping = new Ping();
    IdentifyOuterClass.Identify identifyMsg =
        IdentifyOuterClass.Identify.newBuilder()
            .setProtocolVersion("ipfs/0.1.0")
            .setAgentVersion(VersionProvider.CLIENT_IDENTITY + "/" + VersionProvider.VERSION)
            .setPublicKey(ByteArrayExtKt.toProtobuf(privKey.publicKey().bytes()))
            .addListenAddrs(ByteArrayExtKt.toProtobuf(advertisedAddr.getBytes()))
            .setObservedAddr(
                ByteArrayExtKt.toProtobuf( // TODO: Report external IP?
                    advertisedAddr.getBytes()))
            .addAllProtocols(ping.getProtocolDescriptor().getAnnounceProtocols())
            .addAllProtocols(gossip.getProtocolDescriptor().getAnnounceProtocols())
            .build();
    return List.of(ping, new Identify(identifyMsg), gossip);
  }

  @Override
  public SafeFuture<?> start() {
    if (!state.compareAndSet(State.IDLE, State.RUNNING)) {
      return SafeFuture.failedFuture(new IllegalStateException("Network already started"));
    }
    LOG.info("Starting libp2p network...");
    return SafeFuture.of(host.start())
        .thenApply(
            i -> {
              STATUS_LOG.listeningForLibP2P(getNodeAddress());
              return null;
            });
  }

  @Override
  public String getNodeAddress() {
    return advertisedAddr.toString();
  }

  @Override
  public SafeFuture<Peer> connect(final PeerAddress peer) {
    return peer.as(MultiaddrPeerAddress.class)
        .map(staticPeer -> peerManager.connect(staticPeer, host.getNetwork()))
        .orElseGet(
            () ->
                failedFuture(
                    new IllegalArgumentException(
                        "Unsupported peer address: " + peer.getClass().getName())));
  }

  @Override
  public PeerAddress createPeerAddress(final String peerAddress) {
    return MultiaddrPeerAddress.fromAddress(peerAddress);
  }

  @Override
  public PeerAddress createPeerAddress(final DiscoveryPeer discoveryPeer) {
    return MultiaddrPeerAddress.fromDiscoveryPeer(discoveryPeer);
  }

  @Override
  public long subscribeConnect(final PeerConnectedSubscriber<Peer> subscriber) {
    return peerManager.subscribeConnect(subscriber);
  }

  @Override
  public void unsubscribeConnect(final long subscriptionId) {
    peerManager.unsubscribeConnect(subscriptionId);
  }

  @Override
  public boolean isConnected(final PeerAddress peerAddress) {
    return peerManager.getPeer(peerAddress.getId()).isPresent();
  }

  @Override
  public Optional<Peer> getPeer(final NodeId id) {
    return peerManager.getPeer(id);
  }

  @Override
  public Stream<Peer> streamPeers() {
    return peerManager.streamPeers();
  }

  @Override
  public int getPeerCount() {
    return peerManager.getPeerCount();
  }

  @Override
  public int getListenPort() {
    return listenPort;
  }

  @Override
  public void stop() {
    if (!state.compareAndSet(State.RUNNING, State.STOPPED)) {
      return;
    }
    LOG.debug("JvmLibP2PNetwork.stop()");
    reportExceptions(host.stop());
  }

  @Override
  public NodeId getNodeId() {
    return nodeId;
  }

  @Override
  public Optional<String> getEnr() {
    return Optional.empty();
  }

  @Override
  public Optional<String> getDiscoveryAddress() {
    return Optional.empty();
  }

  @Override
  public SafeFuture<?> gossip(final String topic, final Bytes data) {
    return gossipNetwork.gossip(topic, data);
  }

  @Override
  public TopicChannel subscribe(final String topic, final TopicHandler topicHandler) {
    return gossipNetwork.subscribe(topic, topicHandler);
  }

  @Override
  public Map<String, Collection<NodeId>> getSubscribersByTopic() {
    return gossipNetwork.getSubscribersByTopic();
  }
}
