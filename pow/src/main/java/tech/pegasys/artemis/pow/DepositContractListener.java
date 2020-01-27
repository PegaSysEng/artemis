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

package tech.pegasys.artemis.pow;

import static tech.pegasys.artemis.pow.contract.DepositContract.DEPOSITEVENT_EVENT;

import com.google.common.eventbus.EventBus;
import com.google.common.primitives.UnsignedLong;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.abi.EventEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import tech.pegasys.artemis.pow.contract.DepositContract;
import tech.pegasys.artemis.pow.contract.DepositContract.DepositEventEventResponse;
import tech.pegasys.artemis.pow.event.Deposit;

public class DepositContractListener {
  private static final Logger LOG = LogManager.getLogger();
  private final Disposable subscriptionNewDeposit;
  private DepositContract contract;
  private volatile Optional<EthBlock.Block> cachedBlock = Optional.empty();

  public DepositContractListener(Web3j web3j, EventBus eventBus, DepositContract contract) {
    this.contract = contract;

    // Filter by the contract address and by begin/end blocks
    EthFilter depositEventFilter =
        new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                contract.getContractAddress().substring(2))
            .addSingleTopic(EventEncoder.encode(DEPOSITEVENT_EVENT));

    // Subscribe to the event of a validator being registered in the
    // DepositContract
    subscriptionNewDeposit =
        contract
            .depositEventEventFlowable(depositEventFilter)
            .flatMap(event -> getBlockTimestamp(web3j, event))
            .subscribe(eventBus::post);
  }

  private Flowable<Deposit> getBlockTimestamp(
      final Web3j web3j, final DepositEventEventResponse event) {
    LOG.debug("Getting timestamp for deposit event in block {}", event.log.getBlockNumber());
    return getBlockByHash(web3j, event.log.getBlockHash())
        .map(block -> new Deposit(event, UnsignedLong.valueOf(block.getTimestamp())));
  }

  private Flowable<Block> getBlockByHash(final Web3j web3j, final String blockHash) {
    return cachedBlock
        .filter(block -> block.getHash().equals(blockHash))
        .map(Flowable::just)
        .orElseGet(
            () ->
                web3j
                    .ethGetBlockByHash(blockHash, false)
                    .flowable()
                    .map(
                        blockResponse -> {
                          cachedBlock = Optional.of(blockResponse.getBlock());
                          return blockResponse.getBlock();
                        }));
  }

  public DepositContract getContract() {
    return contract;
  }

  public void stop() {
    subscriptionNewDeposit.dispose();
  }
}
