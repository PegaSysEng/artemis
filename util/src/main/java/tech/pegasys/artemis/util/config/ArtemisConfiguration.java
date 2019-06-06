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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.config.Configuration;
import org.apache.tuweni.config.ConfigurationError;
import org.apache.tuweni.config.PropertyValidator;
import org.apache.tuweni.config.Schema;
import org.apache.tuweni.config.SchemaBuilder;
import org.apache.tuweni.crypto.SECP256K1;
import tech.pegasys.artemis.util.bls.BLSSignature;

/** Configuration of an instance of Artemis. */
public final class ArtemisConfiguration {

  @SuppressWarnings({"DoubleBraceInitialization"})
  static final Schema createSchema() {
    SchemaBuilder builder =
        SchemaBuilder.create()
            .addString(
                "node.networkMode",
                "mock",
                "represents what network to use",
                PropertyValidator.anyOf("mock", "hobbits"));
    builder.addString("node.identity", null, "Identity of the peer", null);
    builder.addString("node.timer", "QuartzTimer", "Timer used for slots", null);
    builder.addString("node.networkInterface", "0.0.0.0", "Peer to peer network interface", null);
    builder.addInteger("node.port", 9000, "Peer to peer port", PropertyValidator.inRange(0, 65535));
    builder.addInteger(
        "node.advertisedPort",
        9000,
        "Peer to peer advertised port",
        PropertyValidator.inRange(0, 65535));
    builder.addInteger(
        "deposit.numValidators",
        128,
        "represents the total number of validators in the network",
        PropertyValidator.inRange(1, 65535));
    builder.addInteger(
        "deposit.numNodes",
        1,
        "represents the total number of nodes on the network",
        PropertyValidator.inRange(1, 65535));
    builder.addString("deposit.mode", "normal", "PoW Deposit Mode", null);
    builder.addString("deposit.inputFile", "", "PoW simulation optional input file", null);
    builder.addString("deposit.nodeUrl", null, "URL for Eth 1.0 node", null);
    builder.addString(
        "deposit.contractAddr", null, "Contract address for the deposit contract", null);
    builder.addListOfString(
        "node.peers",
        Collections.emptyList(),
        "Static peers",
        (key, position, peers) ->
            peers != null
                ? peers.stream()
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
                    .collect(Collectors.toList())
                : null);
    builder.addLong(
        "node.networkID", 1L, "The identifier of the network (mainnet, testnet, sidechain)", null);

    // Metrics
    builder.addBoolean("metrics.enabled", false, "Enables metrics collection via Prometheus", null);
    builder.addString(
        "metrics.metricsNetworkInterface",
        "0.0.0.0",
        "Metrics network interface to expose metrics for Prometheus",
        null);
    builder.addInteger(
        "metrics.metricsPort",
        8008,
        "Metrics port to expose metrics for Prometheus",
        PropertyValidator.inRange(0, 65535));
    // Outputs
    builder.addString(
        "output.providerType",
        "JSON",
        "Output provider types: CSV, JSON",
        PropertyValidator.anyOf("CSV", "JSON"));
    builder.addString("output.outputFile", "", "Path/filename of the output file", null);
    builder.addBoolean(
        "output.formatted", false, "Output of JSON file is serial or formatted", null);
    builder.addListOfString(
        "output.events",
        new ArrayList<String>() {
          {
            add("TimeSeriesRecord");
          }
        },
        "Output selector for specific events",
        null);

    // Constants
    // Misc
    builder.addInteger("constants.SHARD_COUNT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.TARGET_COMMITTEE_SIZE", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_BALANCE_CHURN_QUOTIENT", Integer.MIN_VALUE, null, null);
    builder.addLong("constants.BEACON_CHAIN_SHARD_NUMBER", -1L, null, null);
    builder.addInteger("constants.MAX_INDICES_PER_SLASHABLE_VOTE", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_EXIT_DEQUEUES_PER_EPOCH", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.SHUFFLE_ROUND_COUNT", Integer.MIN_VALUE, null, null);

    // Deposit Contract
    builder.addString("constants.DEPOSIT_CONTRACT_ADDRESS", "", null, null);
    builder.addInteger("constants.DEPOSIT_CONTRACT_TREE_DEPTH", Integer.MIN_VALUE, null, null);

    // Gwei values
    builder.addLong("constants.MIN_DEPOSIT_AMOUNT", Long.MIN_VALUE, null, null);
    builder.addLong("constants.MAX_DEPOSIT_AMOUNT", Long.MIN_VALUE, null, null);
    builder.addLong("constants.FORK_CHOICE_BALANCE_INCREMENT", Long.MIN_VALUE, null, null);
    builder.addLong("constants.EJECTION_BALANCE", Long.MIN_VALUE, null, null);

    // Initial Values
    builder.addInteger("constants.GENESIS_FORK_VERSION", Integer.MIN_VALUE, null, null);
    builder.addLong("constants.GENESIS_SLOT", Long.MIN_VALUE, null, null);
    builder.addLong("constants.GENESIS_EPOCH", Long.MIN_VALUE, null, null);
    builder.addInteger("constants.GENESIS_START_SHARD", Integer.MIN_VALUE, null, null);
    builder.addLong("constants.FAR_FUTURE_EPOCH", -1L, null, null);
    builder.addDefault("constants.ZERO_HASH", Bytes32.ZERO);
    builder.addDefault("constants.EMPTY_SIGNATURE", BLSSignature.empty());
    builder.addDefault("constants.BLS_WITHDRAWAL_PREFIX_BYTE", Bytes32.EMPTY);

    // Time parameters
    builder.addInteger("constants.SECONDS_PER_SLOT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MIN_ATTESTATION_INCLUSION_DELAY", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.SLOTS_PER_EPOCH", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MIN_SEED_LOOKAHEAD", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.ACTIVATION_EXIT_DELAY", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.EPOCHS_PER_ETH1_VOTING_PERIOD", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.SLOTS_PER_HISTORICAL_ROOT", Integer.MIN_VALUE, null, null);
    builder.addInteger(
        "constants.MIN_VALIDATOR_WITHDRAWABILITY_DELAY", Integer.MIN_VALUE, null, null);

    // State list lengths
    builder.addInteger("constants.LATEST_BLOCK_ROOTS_LENGTH", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.LATEST_RANDAO_MIXES_LENGTH", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.LATEST_ACTIVE_INDEX_ROOTS_LENGTH", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.LATEST_SLASHED_EXIT_LENGTH", Integer.MIN_VALUE, null, null);

    // Reward and penalty quotients
    builder.addInteger("constants.BASE_REWARD_QUOTIENT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.WHISTLEBLOWER_REWARD_QUOTIENT", Integer.MIN_VALUE, null, null);
    builder.addInteger(
        "constants.ATTESTATION_INCLUSION_REWARD_QUOTIENT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.INACTIVITY_PENALTY_QUOTIENT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MIN_PENALTY_QUOTIENT", Integer.MIN_VALUE, null, null);

    // Max transactions per block
    builder.addInteger("constants.MAX_PROPOSER_SLASHINGS", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_ATTESTER_SLASHINGS", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_ATTESTATIONS", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_DEPOSITS", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_VOLUNTARY_EXITS", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.MAX_TRANSFERS", Integer.MIN_VALUE, null, null);

    // Signature domains
    builder.addInteger("constants.DOMAIN_BEACON_BLOCK", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.DOMAIN_RANDAO", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.DOMAIN_ATTESTATION", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.DOMAIN_DEPOSIT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.DOMAIN_VOLUNTARY_EXIT", Integer.MIN_VALUE, null, null);
    builder.addInteger("constants.DOMAIN_TRANSFER", Integer.MIN_VALUE, null, null);

    // Artemis specific
    builder.addString("constants.SIM_DEPOSIT_VALUE", "", null, null);
    builder.addInteger("constants.DEPOSIT_DATA_SIZE", Integer.MIN_VALUE, null, null);

    builder.validateConfiguration(
        config -> {
          return null;
        });

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

  /** @return the identity of the node, the hexadecimal representation of its secret key */
  public String getTimer() {
    return config.getString("node.timer");
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
    return config.getInteger("deposit.numValidators");
  }

  /** @return the total number of nodes on the network */
  public int getNumNodes() {
    return config.getInteger("deposit.numNodes");
  }

  /** @return the Deposit simulation flag, w/ optional input file */
  public String getInputFile() {
    String inputFile = config.getString("deposit.inputFile");
    if (inputFile == null || inputFile.equals("")) return null;
    return inputFile;
  }

  public String getContractAddr() {
    return config.getString("deposit.contractAddr");
  }

  public String getNodeUrl() {
    return config.getString("deposit.nodeUrl");
  }

  /** @return if simulation is enabled or not */
  public String getDepositMode() {
    return config.getString("deposit.mode");
  }

  /** @return the Output provider types: CSV, JSON */
  public String getProviderType() {
    return config.getString("output.providerType");
  }

  /** @return if metrics is enabled or not */
  public Boolean isMetricsEnabled() {
    return config.getBoolean("metrics.enabled");
  }

  public String getMetricsNetworkInterface() {
    return config.getString("metrics.metricsNetworkInterface");
  }

  public int getMetricsPort() {
    return config.getInteger("metrics.metricsPort");
  }

  /** @return the Path/filename of the output file. */
  public String getOutputFile() {
    return config.getString("output.outputFile");
  }

  /** @return if output is enabled or not */
  public Boolean isOutputEnabled() {
    return this.getOutputFile().length() > 0;
  }

  /** @return If Output of JSON file is serial or formatted */
  public Boolean isFormat() {
    return config.getBoolean("output.formatted");
  }

  /** @return specific events of Output selector */
  public List<String> getEvents() {
    return config.getListOfString("output.events");
  }

  /** @return specific dynamic event fields of Output selector */
  public Map<String, Object> getEventFields() {

    return config.getMap("output.fields");
  }

  /** @return misc constants */
  public int getShardCount() {
    return config.getInteger("constants.SHARD_COUNT");
  }

  public int getTargetCommitteeSize() {
    return config.getInteger("constants.TARGET_COMMITTEE_SIZE");
  }

  public int getMaxBalanceChurnQuotient() {
    return config.getInteger("constants.MAX_BALANCE_CHURN_QUOTIENT");
  }

  public long getBeaconChainShardNumber() {
    return config.getLong("constants.BEACON_CHAIN_SHARD_NUMBER");
  }

  public int getMaxIndicesPerSlashableVote() {
    return config.getInteger("constants.MAX_INDICES_PER_SLASHABLE_VOTE");
  }

  public int getMaxExitDequeuesPerEpoch() {
    return config.getInteger("constants.MAX_EXIT_DEQUEUES_PER_EPOCH");
  }

  public int getShuffleRoundCount() {
    return config.getInteger("constants.SHUFFLE_ROUND_COUNT");
  }

  /** @return deposit contract constants */
  public String getDepositContractAddress() {
    return config.getString("constants.DEPOSIT_CONTRACT_ADDRESS");
  }

  public int getDepositContractTreeDepth() {
    return config.getInteger("constants.DEPOSIT_CONTRACT_TREE_DEPTH");
  }

  /** @return gwei value constants */
  public long getMinDepositAmount() {
    return config.getLong("constants.MIN_DEPOSIT_AMOUNT");
  }

  public long getMaxDepositAmount() {
    return config.getLong("constants.MAX_DEPOSIT_AMOUNT");
  }

  public long getForkChoiceBalanceIncrement() {
    return config.getLong("constants.FORK_CHOICE_BALANCE_INCREMENT");
  }

  public long getEjectionBalance() {
    return config.getLong("constants.EJECTION_BALANCE");
  }

  /** @return initial value constants */
  public int getGenesisForkVersion() {
    return config.getInteger("constants.GENESIS_FORK_VERSION");
  }

  public long getGenesisSlot() {
    return config.getLong("constants.GENESIS_SLOT");
  }

  public long getGenesisEpoch() {
    return config.getLong("constants.GENESIS_EPOCH");
  }

  public int getGenesisStartShard() {
    return config.getInteger("constants.GENESIS_START_SHARD");
  }

  public long getFarFutureEpoch() {
    return Long.MAX_VALUE;
  }

  public Object getZeroHash() {
    return config.get("constants.ZERO_HASH");
  }

  public Object getEmptySignature() {
    return config.get("constants.EMPTY_SIGNATURE");
  }

  public Object getBlsWithdrawalPrefixByte() {
    return config.get("constants.BLS_WITHDRAWAL_PREFIX_BYTE");
  }

  /** @return time parameter constants */
  public int getSecondsPerSlot() {
    return config.getInteger("constants.SECONDS_PER_SLOT");
  }

  public int getMinAttestationInclusionDelay() {
    return config.getInteger("constants.MIN_ATTESTATION_INCLUSION_DELAY");
  }

  public int getSlotsPerEpoch() {
    return config.getInteger("constants.SLOTS_PER_EPOCH");
  }

  public int getMinSeedLookahead() {
    return config.getInteger("constants.MIN_SEED_LOOKAHEAD");
  }

  public int getActivationExitDelay() {
    return config.getInteger("constants.ACTIVATION_EXIT_DELAY");
  }

  public int getEpochsPerEth1VotingPeriod() {
    return config.getInteger("constants.EPOCHS_PER_ETH1_VOTING_PERIOD");
  }

  public int getSlotsPerHistoricalRoot() {
    return config.getInteger("constants.SLOTS_PER_HISTORICAL_ROOT");
  }

  public int getMinValidatorWithdrawabilityDelay() {
    return config.getInteger("constants.MIN_VALIDATOR_WITHDRAWABILITY_DELAY");
  }

  /** @return state list length constants */
  public int getLatestRandaoMixesLength() {
    return config.getInteger("constants.LATEST_RANDAO_MIXES_LENGTH");
  }

  public int getLatestActiveIndexRootsLength() {
    return config.getInteger("constants.LATEST_ACTIVE_INDEX_ROOTS_LENGTH");
  }

  public int getLatestSlashedExitLength() {
    return config.getInteger("constants.LATEST_SLASHED_EXIT_LENGTH");
  }

  /** @return reward and penalty quotient constants */
  public int getBaseRewardQuotient() {
    return config.getInteger("constants.BASE_REWARD_QUOTIENT");
  }

  public int getWhistleblowerRewardQuotient() {
    return config.getInteger("constants.WHISTLEBLOWER_REWARD_QUOTIENT");
  }

  public int getAttestationInclusionRewardQuotient() {
    return config.getInteger("constants.ATTESTATION_INCLUSION_REWARD_QUOTIENT");
  }

  public int getInactivityPenaltyQuotient() {
    return config.getInteger("constants.INACTIVITY_PENALTY_QUOTIENT");
  }

  public int getMinPenaltyQuotient() {
    return config.getInteger("constants.MIN_PENALTY_QUOTIENT");
  }

  /** @return max transactions per block constants */
  public int getMaxProposerSlashings() {
    return config.getInteger("constants.MAX_PROPOSER_SLASHINGS");
  }

  public int getMaxAttesterSlashings() {
    return config.getInteger("constants.MAX_ATTESTER_SLASHINGS");
  }

  public int getMaxAttestations() {
    return config.getInteger("constants.MAX_ATTESTATIONS");
  }

  public int getMaxDeposits() {
    return config.getInteger("constants.MAX_DEPOSITS");
  }

  public int getMaxVoluntaryExits() {
    return config.getInteger("constants.MAX_VOLUNTARY_EXITS");
  }

  public int getMaxTransfers() {
    return config.getInteger("constants.MAX_TRANSFERS");
  }

  /** @return signature domain constants */
  public int getDomainBeaconBlock() {
    return config.getInteger("constants.DOMAIN_BEACON_BLOCK");
  }

  public int getDomainRandao() {
    return config.getInteger("constants.DOMAIN_RANDAO");
  }

  public int getDomainAttestation() {
    return config.getInteger("constants.DOMAIN_ATTESTATION");
  }

  public int getDomainDeposit() {
    return config.getInteger("constants.DOMAIN_DEPOSIT");
  }

  public int getDomainVoluntaryExit() {
    return config.getInteger("constants.DOMAIN_VOLUNTARY_EXIT");
  }

  public int getDomainTransfer() {
    return config.getInteger("constants.DOMAIN_TRANSFER");
  }

  /** @return Artemis specific constants */
  public String getSimDepositValue() {
    return config.getString("constants.SIM_DEPOSIT_VALUE");
  }

  public int getDepositDataSize() {
    return config.getInteger("constants.DEPOSIT_DATA_SIZE");
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

  /** @return the mode of the network to use - mock or hobbits */
  public String getNetworkMode() {
    return config.getString("node.networkMode");
  }
}
