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

package tech.pegasys.artemis.util.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.consensys.cava.bytes.Bytes32;
import net.consensys.cava.config.Configuration;
import net.consensys.cava.config.ConfigurationError;
import net.consensys.cava.config.PropertyValidator;
import net.consensys.cava.config.Schema;
import net.consensys.cava.config.SchemaBuilder;
import net.consensys.cava.crypto.SECP256K1;

/** Configuration of an instance of Artemis. */
public final class ArtemisConfiguration {

  static final Schema createSchema() {
    SchemaBuilder builder =
        SchemaBuilder.create()
            .addString(
                "node.networkMode",
                "mock",
                "represents what network to use",
                PropertyValidator.anyOf("mock", "rlpx", "hobbits"));
    builder.addString("node.identity", null, "Identity of the peer", PropertyValidator.isPresent());
    builder.addString("node.networkInterface", "0.0.0.0", "Peer to peer network interface", null);
    builder.addInteger("node.port", 9000, "Peer to peer port", PropertyValidator.inRange(0, 65535));
    builder.addInteger(
        "node.advertisedPort",
        9000,
        "Peer to peer advertised port",
        PropertyValidator.inRange(0, 65535));
    builder.addInteger(
        "sim.numValidators",
        128,
        "represents the total number of validators in the network",
        PropertyValidator.inRange(1, 16384));
    builder.addInteger(
        "sim.numNodes",
        1,
        "represents the total number of nodes on the network",
        PropertyValidator.inRange(1, 16384));
    builder.addListOfString(
        "node.peers",
        Collections.emptyList(),
        "Static peers",
        (key, position, peers) ->
            peers.stream()
                .map(
                    peer -> {
                      try {
                        URI uri = new URI(peer);
                        String userInfo = uri.getUserInfo();
                        if (userInfo == null || userInfo.isEmpty()) {
                          return new ConfigurationError("Missing public key");
                        }
                      } catch (URISyntaxException e) {
                        return new ConfigurationError("Invalid uri " + peer);
                      }
                      return null;
                    })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    builder.addLong(
        "node.networkID", 1L, "The identifier of the network (mainnet, testnet, sidechain)", null);

    // Constants
    // Misc
    builder.addInteger("constants.SHARD_COUNT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.TARGET_COMMITTEE_SIZE", Integer.MIN_VALUE, null, null);
    builder.addLong("constants.EJECTION_BALANCE", Long.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_BALANCE_CHURN_QUOTIENT", Integer.MIN_VALUE, null, null);
    builder.addDefault("constants.BEACON_CHAIN_SHARD_NUMBER", "");
    builder.addInteger("constants.MAX_INDICES_PER_SLASHABLE_VOTE", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_WITHDRAWALS_PER_EPOCH", Integer.MIN_VALUE, null, null);

    // Deposit Contract
    builder.addString("constants.DEPOSIT_CONTRACT_ADDRESS", "", null, null);
    builder.addInteger("constants.DEPOSIT_CONTRACT_TREE_DEPTH", Integer.MIN_VALUE, null, null);
    builder.addLong("constants.MIN_DEPOSIT_AMOUNT", Long.MIN_VALUE, null, null);
    builder.addLong("constants.MAX_DEPOSIT_AMOUNT", Long.MIN_VALUE, null, null);

    // Initial Values
    builder.addInteger("constants.GENESIS_FORK_VERSION", Integer.MIN_VALUE, null, null);
    builder.addLong("constants.GENESIS_SLOT", Long.MIN_VALUE, null, null);
    builder.addLong("constants.GENESIS_EPOCH", Long.MIN_VALUE, null, null);
    builder.addInteger("constants.GENESIS_START_SHARD", Integer.MIN_VALUE, null, null);
    builder.addDefault("constants.FAR_FUTURE_EPOCH", "");
    builder.addDefault("constants.ZERO_HASH", "");
    builder.addDefault("constants.EMPTY_SIGNATURE", "");
    builder.addDefault("constants.BLS_WITHDRAWAL_PREFIX_BYTE", "");

    // Time parameters
    builder.addInteger("constants.SLOT_DURATION", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MIN_ATTESTATION_INCLUSION_DELAY", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.EPOCH_LENGTH", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.SEED_LOOKAHEAD", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.ENTRY_EXIT_DELAY", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.ETH1_DATA_VOTING_PERIOD", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MIN_VALIDATOR_WITHDRAWAL_EPOCHS", Integer.MIN_VALUE, null, null);

    // State list lengths
    builder.addInteger("constants.LATEST_BLOCK_ROOTS_LENGTH", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.LATEST_RANDAO_MIXES_LENGTH", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.LATEST_INDEX_ROOTS_LENGTH", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.LATEST_PENALIZED_EXIT_LENGTH", Integer.MIN_VALUE, null, null);

    // Reward and penalty quotients
    builder.addInteger("constants.BASE_REWARD_QUOTIENT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.WHISTLEBLOWER_REWARD_QUOTIENT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.INCLUDER_REWARD_QUOTIENT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.INACTIVITY_PENALTY_QUOTIENT", Integer.MIN_VALUE, null, null);

    // Status flags
    builder.addInteger("constants.INITIATED_EXIT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.WITHDRAWABLE", Integer.MIN_VALUE, null, null);

    // Max transactions per block
    builder.addInteger("constants.MAX_PROPOSER_SLASHINGS", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_ATTESTER_SLASHINGS", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_ATTESTATIONS", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_DEPOSITS", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_EXITS", Integer.MIN_VALUE, null, null);

    // Signature domains
    builder.addInteger("constants.DOMAIN_DEPOSIT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.DOMAIN_ATTESTATION", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.DOMAIN_PROPOSAL", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.DOMAIN_EXIT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.DOMAIN_RANDAO", Integer.MIN_VALUE, null, null);

    // Artemis specific
    builder.addString("constants.SIM_DEPOSIT_VALUE", "", null, null);
    builder.addInteger("constants.DEPOSIT_DATA_SIZE", Integer.MIN_VALUE, null, null);

    return builder.toSchema();
  }

  private static final Schema schema = createSchema();

  /**
   * Reads configuration from file.
   *
   * @param path a toml file to read configuration from
   * @return the new ArtemisConfiguration
   * @throws UncheckedIOException if the file is missing
   */
  public static ArtemisConfiguration fromFile(String path) {
    Path configPath = Paths.get(path);
    try {
      return new ArtemisConfiguration(Configuration.fromToml(configPath, schema));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Reads configuration from a toml text.
   *
   * @param configText the toml text
   * @return the new ArtemisConfiguration
   */
  public static ArtemisConfiguration fromString(String configText) {
    return new ArtemisConfiguration(Configuration.fromToml(configText, schema));
  }

  private final Configuration config;

  private ArtemisConfiguration(Configuration config) {
    this.config = config;
    if (config.hasErrors()) {
      throw new IllegalArgumentException(
          config.errors().stream()
              .map(error -> error.position() + " " + error.toString())
              .collect(Collectors.joining("\n")));
    }
  }

  /** @return the identity of the node, the hexadecimal representation of its secret key */
  public String getIdentity() {
    return config.getString("node.identity");
  }

  /** @return the port this node will listen to */
  public int getPort() {
    return config.getInteger("node.port");
  }

  /** @return the port this node will advertise as its own */
  public int getAdvertisedPort() {
    return config.getInteger("node.advertisedPort");
  }

  /** @return the network interface this node will bind to */
  public String getNetworkInterface() {
    return config.getString("node.networkInterface");
  }

  /** @return the total number of validators in the network */
  public int getNumValidators() {
    return config.getInteger("sim.numValidators");
  }

  /** @return the total number of nodes on the network */
  public int getNumNodes() {
    return config.getInteger("sim.numNodes");
  }

  /** @return misc constants */
  public int getShardCount() {
    return config.getInteger("SHARD_COUNT");
  }

  public int getTargetCommitteeSize() {
    return config.getInteger("TARGET_COMMITTEE_SIZE");
  }

  public long getEjectionBalance() {
    return config.getLong("EJECTION_BALANCE");
  }

  public int getMaxBalanceChurnQuotient() {
    return config.getInteger("MAX_BALANCE_CHURN_QUOTIENT");
  }

  public Object getBeaconChainShardNumber() {
    return config.get("BEACON_CHAIN_SHARD_NUMBER");
  }

  public int getMaxIndicesPerSlashableVote() {
    return config.getInteger("MAX_INDICES_PER_SLASHABLE_VOTE");
  }

  public int getMaxWithdrawalsPerEpoch() {
    return config.getInteger("MAX_WITHDRAWALS_PER_EPOCH");
  }

  /** @return deposit contract constants */
  public String getDepositContractAddress() {
    return config.getString("DEPOSIT_CONTRACT_ADDRESS");
  }

  public int getDepositContractTreeDepth() {
    return config.getInteger("DEPOSIT_CONTRACT_TREE_DEPTH");
  }

  public long getMinDepositAmount() {
    return config.getInteger("MIN_DEPOSIT_AMOUNT");
  }

  public long getMaxDepositAmount() {
    return config.getInteger("MAX_DEPOSIT_AMOUNT");
  }

  /** @return initial value constants */
  public int getGenesisForkVersion() {
    return config.getInteger("GENESIS_FORK_VERSION");
  }

  public long getGenesisSlot() {
    return config.getInteger("GENESIS_SLOT");
  }

  public long getGenesisEpoch() {
    return config.getInteger("GENESIS_EPOCH");
  }

  public int getGenesisStartShard() {
    return config.getInteger("GENESIS_START_SHARD");
  }

  public Object getFarFutureEpoch() {
    return config.getInteger("FAR_FUTURE_EPOCH");
  }

  public Object getZeroHash() {
    return config.getInteger("ZERO_HASH");
  }

  public Object getEmptySignature() {
    return config.getInteger("EMPTY_SIGNATURE");
  }

  public Object getBlsWithdrawalPrefixByte() {
    return config.getInteger("BLS_WITHDRAWAL_PREFIX_BYTE");
  }

  /** @return time parameter constants */
  public int getSlotDuration() {
    return config.getInteger("SLOT_DURATION");
  }

  public int getMinAttestationInclusionDelay() {
    return config.getInteger("MIN_ATTESTATION_INCLUSION_DELAY");
  }

  public int getEpochLength() {
    return config.getInteger("EPOCH_LENGTH");
  }

  public int getSeedLookahead() {
    return config.getInteger("SEED_LOOKAHEAD");
  }

  public int getEntryExitDelay() {
    return config.getInteger("ENTRY_EXIT_DELAY");
  }

  public int getEth1DataVotingPeriod() {
    return config.getInteger("ETH1_DATA_VOTING_PERIOD");
  }

  public int getMinValidatorWithdrawalEpochs() {
    return config.getInteger("MIN_VALIDATOR_WITHDRAWAL_EPOCHS");
  }

  /** @return state list length constants */
  public int getLatestBlockRootsLength() {
    return config.getInteger("LATEST_BLOCK_ROOTS_LENGTH");
  }

  public int getLatestRandaoMixesLength() {
    return config.getInteger("LATEST_RANDAO_MIXES_LENGTH");
  }

  public int getLatestIndexRootsLength() {
    return config.getInteger("LATEST_INDEX_ROOTS_LENGTH");
  }

  public int getLatestPenalizedExitLength() {
    return config.getInteger("LATEST_PENALIZED_EXIT_LENGTH");
  }

  /** @return reward and penalty quotient constants */
  public int getBaseRewardQuotient() {
    return config.getInteger("BASE_REWARD_QUOTIENT");
  }

  public int getWhistleblowerRewardQuotient() {
    return config.getInteger("WHISTLEBLOWER_REWARD_QUOTIENT");
  }

  public int getIncluderRewardQuotient() {
    return config.getInteger("INCLUDER_REWARD_QUOTIENT");
  }

  public int getInactivityPenaltyQuotient() {
    return config.getInteger("INACTIVITY_PENALTY_QUOTIENT");
  }

  /** @return status flag constants */
  public int getInitiatedExit() {
    return config.getInteger("INITIATED_EXIT");
  }

  public int getWithdrawable() {
    return config.getInteger("WITHDRAWABLE");
  }

  /** @return max transactions per block constants */
  public int getMaxProposerSlashings() {
    return config.getInteger("MAX_PROPOSER_SLASHINGS");
  }

  public int getMaxAttesterSlashings() {
    return config.getInteger("MAX_ATTESTER_SLASHINGS");
  }

  public int getMaxAttestations() {
    return config.getInteger("MAX_ATTESTATIONS");
  }

  public int getMaxDeposits() {
    return config.getInteger("MAX_DEPOSITS");
  }

  public int getMaxExits() {
    return config.getInteger("MAX_EXITS");
  }

  /** @return signature domain constants */
  public int getDomainDeposit() {
    return config.getInteger("DOMAIN_DEPOSIT");
  }

  public int getDomainAttestation() {
    return config.getInteger("DOMAIN_ATTESTATION");
  }

  public int getDomainProposal() {
    return config.getInteger("DOMAIN_PROPOSAL");
  }

  public int getDomainExit() {
    return config.getInteger("DOMAIN_EXIT");
  }

  public int getDomainRandao() {
    return config.getInteger("DOMAIN_RANDAO");
  }

  /** @return Artemis specific constants */
  public String getSimDepositValue() {
    return config.getString("SIM_DEPOSIT_VALUE");
  }

  public int getDepositDataSize() {
    return config.getInteger("DEPOSIT_DATA_SIZE");
  }

  /** @return the list of static peers associated with this node */
  public List<URI> getStaticPeers() {
    return config.getListOfString("node.peers").stream()
        .map(
            (peer) -> {
              try {
                return new URI(peer);
              } catch (URISyntaxException e) {
                throw new RuntimeException(e);
              }
            })
        .collect(Collectors.toList());
  }

  /** @return the identity key pair of the node */
  public SECP256K1.KeyPair getKeyPair() {
    return SECP256K1.KeyPair.fromSecretKey(
        SECP256K1.SecretKey.fromBytes(Bytes32.fromHexString(getIdentity())));
  }

  /** @return the identifier of the network (mainnet, testnet, sidechain) */
  public long getNetworkID() {
    return config.getLong("node.networkID");
  }

  /** @return the mode of the network to use - mock, rlpx or hobbits */
  public String getNetworkMode() {
    return config.getString("node.networkMode");
  }
}
