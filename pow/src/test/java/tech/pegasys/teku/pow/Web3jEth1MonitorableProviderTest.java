/*
 * Copyright 2021 ConsenSys AG.
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

package tech.pegasys.teku.pow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthSyncing;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.async.StubAsyncRunner;
import tech.pegasys.teku.infrastructure.time.StubTimeProvider;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.util.config.Constants;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Web3jEth1MonitorableProviderTest {
  final Web3j web3 = mock(Web3j.class);
  final Request request1 = mock(Request.class);
  final Request request2 = mock(Request.class);
  StubAsyncRunner asyncRunner;
  StubTimeProvider timeProvider;
  Web3jEth1Provider provider;

  static final String CHAIN_ID_CORRECT = "5";
  static final String CHAIN_ID_WRONG = "1";

  @BeforeEach
  void setup() {
    asyncRunner = new StubAsyncRunner();
    timeProvider = StubTimeProvider.withTimeInSeconds(1000);
    provider = new Web3jEth1Provider("0 test.host.org", web3, asyncRunner, timeProvider);
  }

  @Test
  void shouldUpdateCallTimestampOnSuccess() throws ExecutionException, InterruptedException {
    succeedACall();

    assertThat(provider.getLastCallTime()).isEqualTo(UInt64.valueOf(1000));
    assertThat(provider.getLastValidationTime()).isNotEqualTo(UInt64.valueOf(1000));
  }

  @Test
  void shouldUpdateCallTimestampOnFailure() {
    failACall();

    assertThat(provider.getLastCallTime()).isEqualTo(UInt64.valueOf(1000));
    assertThat(provider.getLastValidationTime()).isNotEqualTo(UInt64.valueOf(1000));
  }

  @Test
  void shouldUpdateValidationTimestamp() {
    prepareRequestWithChainidResponse(request1, CHAIN_ID_WRONG);
    when(web3.ethChainId()).thenReturn(request1);
    prepareRequestWithSyncingResponse(request2, false);
    when(web3.ethSyncing()).thenReturn(request2);

    try {
      provider.validate().get();
    } catch (Exception ignored) {
    }

    assertThat(provider.getLastCallTime()).isEqualTo(UInt64.valueOf(1000));
    assertThat(provider.getLastValidationTime()).isEqualTo(UInt64.valueOf(1000));
  }

  @Test
  void validationShouldFailIfCorrectChainIdStillSyncing()
      throws ExecutionException, InterruptedException {
    prepareRequestWithChainidResponse(request1, CHAIN_ID_CORRECT);
    when(web3.ethChainId()).thenReturn(request1);
    prepareRequestWithSyncingResponse(request2, true);
    when(web3.ethSyncing()).thenReturn(request2);

    assertThat(provider.validate().get()).isFalse();
    assertThat(provider.isValid()).isFalse();
  }

  @Test
  void validationShouldFailIfWrongChainIdStillSyncing()
      throws ExecutionException, InterruptedException {
    prepareRequestWithChainidResponse(request1, CHAIN_ID_WRONG);
    when(web3.ethChainId()).thenReturn(request1);
    prepareRequestWithSyncingResponse(request2, true);
    when(web3.ethSyncing()).thenReturn(request2);

    assertThat(provider.validate().get()).isFalse();
    assertThat(provider.isValid()).isFalse();
  }

  @Test
  void validationShouldFailIfCorrectChainIdNotSyncing()
      throws ExecutionException, InterruptedException {
    prepareRequestWithChainidResponse(request1, CHAIN_ID_WRONG);
    when(web3.ethChainId()).thenReturn(request1);
    prepareRequestWithSyncingResponse(request2, false);
    when(web3.ethSyncing()).thenReturn(request2);

    assertThat(provider.validate().get()).isFalse();
    assertThat(provider.isValid()).isFalse();
  }

  @Test
  void validationShouldSucceedIfCorrectChainIdNotSyncing()
      throws ExecutionException, InterruptedException {
    prepareRequestWithChainidResponse(request1, CHAIN_ID_CORRECT);
    when(web3.ethChainId()).thenReturn(request1);
    prepareRequestWithSyncingResponse(request2, false);
    when(web3.ethSyncing()).thenReturn(request2);

    assertThat(provider.validate().get()).isTrue();
    assertThat(provider.isValid()).isTrue();
  }

  @Test
  void validationShouldFailIfCorrectChainIdFailSyncingCall()
      throws ExecutionException, InterruptedException {
    prepareRequestWithChainidResponse(request1, CHAIN_ID_CORRECT);
    when(web3.ethChainId()).thenReturn(request1);
    prepareFailingRequest(request2);
    when(web3.ethSyncing()).thenReturn(request2);

    assertThat(provider.validate().get()).isFalse();
    assertThat(provider.isValid()).isFalse();
  }

  @Test
  void needsToBeValidatedChecks() throws ExecutionException, InterruptedException {
    // needs to be validated on initialization
    assertThat(provider.needsToBeValidated()).isTrue();

    failAValidation();

    // advance less then required
    timeProvider.advanceTimeBySeconds(
        Constants.ETH1_INVALID_ENDPOINT_CHECK_INTERVAL.getSeconds() - 10);

    // just failed, no need to validate
    assertThat(provider.needsToBeValidated()).isFalse();

    // advance to go after
    timeProvider.advanceTimeBySeconds(Constants.ETH1_INVALID_ENDPOINT_CHECK_INTERVAL.getSeconds());

    // after the interval needs to be validated
    assertThat(provider.needsToBeValidated()).isTrue();

    succeedAValidation();

    // just validated, no need to validate
    assertThat(provider.needsToBeValidated()).isFalse();

    timeProvider.advanceTimeBySeconds(
        Constants.ETH1_VALID_ENDPOINT_CHECK_INTERVAL.getSeconds() + 1);

    // after the interval needs to be validated
    assertThat(provider.needsToBeValidated()).isTrue();

    succeedAValidation();

    failACall();

    // advance less then required
    timeProvider.advanceTimeBySeconds(
        Constants.ETH1_FAILED_ENDPOINT_CHECK_INTERVAL.getSeconds() - 10);

    // just failed a call, no need to validate
    assertThat(provider.needsToBeValidated()).isFalse();

    // advance to go after
    timeProvider.advanceTimeBySeconds(Constants.ETH1_FAILED_ENDPOINT_CHECK_INTERVAL.getSeconds());

    // after the interval needs to be validated
    assertThat(provider.needsToBeValidated()).isTrue();
  }

  private void prepareRequestWithSyncingResponse(Request request, boolean isSyncing) {
    EthSyncing response = new EthSyncing();
    EthSyncing.Result result = new EthSyncing.Result();
    result.setSyncing(isSyncing);
    response.setResult(result);
    when(request.sendAsync()).thenReturn(SafeFuture.completedFuture(response));
  }

  private void prepareRequestWithChainidResponse(Request request, String chainId) {
    EthChainId response = new EthChainId();
    response.setResult(chainId);
    when(request.sendAsync()).thenReturn(SafeFuture.completedFuture(response));
  }

  private void prepareFailingRequest(Request request) {
    when(request.sendAsync()).thenReturn(SafeFuture.failedFuture(new RuntimeException("error")));
  }

  private void failAValidation() throws ExecutionException, InterruptedException {
    prepareRequestWithChainidResponse(request1, CHAIN_ID_WRONG);
    when(web3.ethChainId()).thenReturn(request1);
    prepareRequestWithSyncingResponse(request2, false);
    when(web3.ethSyncing()).thenReturn(request2);

    assertThat(provider.validate().get()).isFalse();
    assertThat(provider.isValid()).isFalse();
  }

  private void succeedAValidation() throws ExecutionException, InterruptedException {
    prepareRequestWithChainidResponse(request1, CHAIN_ID_CORRECT);
    when(web3.ethChainId()).thenReturn(request1);
    prepareRequestWithSyncingResponse(request2, false);
    when(web3.ethSyncing()).thenReturn(request2);

    assertThat(provider.validate().get()).isTrue();
    assertThat(provider.isValid()).isTrue();
  }

  private void failACall() {
    prepareFailingRequest(request1);
    when(web3.ethSyncing()).thenReturn(request1);

    assertThat(provider.ethSyncing()).isCompletedExceptionally();
  }

  private void succeedACall() throws ExecutionException, InterruptedException {
    prepareRequestWithSyncingResponse(request1, true);
    when(web3.ethSyncing()).thenReturn(request1);

    assertThat(provider.ethSyncing().get()).isTrue();
  }
}
