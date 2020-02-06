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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import tech.pegasys.artemis.ganache.GanacheController;
import tech.pegasys.artemis.pow.api.DepositEventChannel;
import tech.pegasys.artemis.pow.contract.DepositContract;
import tech.pegasys.artemis.util.async.DelayedExecutorAsyncRunner;

public class DepositContractListenerFactory {
  private static final Logger LOG = LogManager.getLogger();

  public static DepositContractListener simulationDeployDepositContract(
      DepositEventChannel depositEventChannel, GanacheController controller) {
    Web3j web3j = Web3j.build(new HttpService(controller.getProvider()));
    Credentials credentials =
        Credentials.create(controller.getAccounts().get(0).secretKey().bytes().toHexString());
    DepositContract contract = null;
    try {
      contract = DepositContract.deploy(web3j, credentials, new DefaultGasProvider()).send();
    } catch (Exception e) {
      LOG.fatal(
          "DepositContractListenerFactory.simulationDeployDepositContract: DepositContract failed to deploy in the simulation environment",
          e);
    }
    return new DepositContractListener(
        web3j, contract, createDepositRequestManager(web3j, depositEventChannel, contract));
  }

  public static DepositContractListener eth1DepositContract(
      Web3j web3j, DepositEventChannel depositEventChannel, String address) {
    DepositContract contract =
        DepositContract.load(
            address, web3j, new ClientTransactionManager(web3j, address), new DefaultGasProvider());
    return new DepositContractListener(
        web3j, contract, createDepositRequestManager(web3j, depositEventChannel, contract));
  }

  private static DepositRequestManager createDepositRequestManager(
      Web3j web3j, DepositEventChannel depositEventChannel, DepositContract depositContract) {
    return new DepositRequestManager(
        new Eth1Provider(web3j),
        new DelayedExecutorAsyncRunner(),
        depositEventChannel,
        depositContract);
  }
}
