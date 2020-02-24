/*
 * Copyright 2020 ConsenSys AG.
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

package tech.pegasys.artemis.beaconrestapi.beaconhandlers;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.javalin.http.Context;
import io.javalin.http.util.ContextUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;
import tech.pegasys.artemis.beaconrestapi.schema.BadRequest;
import tech.pegasys.artemis.beaconrestapi.schema.BeaconBlockResponse;
import tech.pegasys.artemis.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.artemis.datastructures.util.DataStructureUtil;
import tech.pegasys.artemis.provider.JsonProvider;
import tech.pegasys.artemis.storage.ChainStorageClient;
import tech.pegasys.artemis.storage.HistoricalChainData;
import tech.pegasys.artemis.storage.Store;
import tech.pegasys.artemis.util.async.SafeFuture;

public class BeaconBlockHandlerTest {
  private static final BadRequest BAD_REQUEST =
      new BadRequest(
          SC_BAD_REQUEST, "missingQueryParameter: must specify either: root or epoch or slot.");

  private final JsonProvider jsonProvider = new JsonProvider();
  private final ChainStorageClient storageClient = mock(ChainStorageClient.class);
  private final HistoricalChainData historicalChainData = mock(HistoricalChainData.class);
  private final Store store = mock(Store.class);
  private final Bytes32 blockRoot = Bytes32.random();
  private final Context context = mock(Context.class);

  private final SignedBeaconBlock signedBeaconBlock =
      DataStructureUtil.randomSignedBeaconBlock(1, 1);
  private final BeaconBlockHandler handler =
      new BeaconBlockHandler(storageClient, historicalChainData, jsonProvider);
  private final Context realContext =
      spy(ContextUtil.init(mock(HttpServletRequest.class), mock(HttpServletResponse.class)));

  @Test
  public void shouldReturnNotFoundWhenRootQueryAndStoreNull() throws Exception {
    final String rootKey = "0xf22e4ec2";
    final Map<String, List<String>> params = Map.of("root", List.of(rootKey));

    when(storageClient.getStore()).thenReturn(null);
    when(context.queryParamMap()).thenReturn(params);
    when(context.queryParam(any())).thenReturn(rootKey);

    handler.handle(context);

    verify(context).result(jsonProvider.objectToJSON(BAD_REQUEST));
  }

  @Test
  public void shouldReturnNotFoundWhenValidParamNotSpecified() throws Exception {
    handler.handle(context);
    verify(context).result(jsonProvider.objectToJSON(BAD_REQUEST));
  }

  @Test
  public void shouldReturnNotFoundWhenEpochQueryAndBlockNotFound() throws Exception {
    final String epochNum = "1";
    final Map<String, List<String>> params = Map.of("epoch", List.of(epochNum));

    when(context.queryParamMap()).thenReturn(params);
    when(context.queryParam(any())).thenReturn(epochNum);
    when(storageClient.getBlockRootBySlot(any())).thenReturn(Optional.of(blockRoot));
    when(storageClient.getStore()).thenReturn(store);
    when(store.getBlock(any())).thenReturn(null);
    when(historicalChainData.getFinalizedBlockAtSlot(any()))
        .thenReturn(SafeFuture.completedFuture(empty()));

    handler.handle(context);

    verify(context).status(SC_NOT_FOUND);
  }

  @Test
  public void shouldReturnNotFoundWhenSlotQueryAndBlockNotFound() throws Exception {
    final String slotNum = "1";
    final Map<String, List<String>> params = Map.of("slot", List.of(slotNum));

    when(context.queryParamMap()).thenReturn(params);
    when(context.queryParam(any())).thenReturn(slotNum);
    when(storageClient.getStore()).thenReturn(store);
    when(storageClient.getBlockRootBySlot(any())).thenReturn(Optional.of(blockRoot));
    when(store.getBlock(any())).thenReturn(null);
    when(historicalChainData.getFinalizedBlockAtSlot(any()))
        .thenReturn(SafeFuture.completedFuture(empty()));

    handler.handle(context);

    verify(context).status(SC_NOT_FOUND);
  }

  @Test
  public void shouldReturnNotFoundWhenEpochQueryAndNoBlockRootAndBlockNotFound() throws Exception {
    final String epochNum = "1";
    final Map<String, List<String>> params = Map.of("epoch", List.of(epochNum));

    when(context.queryParamMap()).thenReturn(params);
    when(context.queryParam(any())).thenReturn(epochNum);
    when(storageClient.getBlockRootBySlot(any())).thenReturn(empty());
    when(historicalChainData.getFinalizedBlockAtSlot(any()))
        .thenReturn(SafeFuture.completedFuture(empty()));

    handler.handle(context);

    verify(context).status(SC_NOT_FOUND);
  }

  @Test
  public void shouldReturnNotFoundWhenSlotQueryAndNoBlockRootAndBlockNotFound() throws Exception {
    final String slotNum = "1";
    final Map<String, List<String>> params = Map.of("slot", List.of(slotNum));

    when(context.queryParamMap()).thenReturn(params);
    when(context.queryParam(any())).thenReturn(slotNum);
    when(storageClient.getBlockRootBySlot(any())).thenReturn(empty());
    when(historicalChainData.getFinalizedBlockAtSlot(any()))
        .thenReturn(SafeFuture.completedFuture(empty()));

    handler.handle(context);

    verify(context).status(SC_NOT_FOUND);
  }

  @Test
  public void shouldReturnBlockWhenRootParamSpecified() throws Exception {
    final String hash = signedBeaconBlock.getParent_root().toHexString();
    final Map<String, List<String>> params = Map.of("root", List.of(hash));

    when(context.queryParamMap()).thenReturn(params);
    when(context.queryParam(any())).thenReturn(hash);
    when(storageClient.getStore()).thenReturn(store);
    when(store.getSignedBlock(any())).thenReturn(signedBeaconBlock);

    handler.handle(context);

    final String jsonResponse =
        jsonProvider.objectToJSON(new BeaconBlockResponse(signedBeaconBlock));
    verify(context).result(jsonResponse);
  }

  @Test
  public void shouldReturnBlockWhenEpochQuery() throws Exception {
    final String epochNum = "1";
    final Map<String, List<String>> params = Map.of("epoch", List.of(epochNum));

    when(context.queryParamMap()).thenReturn(params);
    when(context.queryParam(any())).thenReturn(epochNum);
    when(storageClient.getBlockRootBySlot(any())).thenReturn(Optional.of(blockRoot));
    when(storageClient.getStore()).thenReturn(store);
    when(store.getSignedBlock(any())).thenReturn(signedBeaconBlock);
    when(historicalChainData.getFinalizedBlockAtSlot(any()))
        .thenReturn(SafeFuture.completedFuture(empty()));

    handler.handle(context);

    final String jsonResponse =
        jsonProvider.objectToJSON(new BeaconBlockResponse(signedBeaconBlock));
    verify(context).result(jsonResponse);
  }

  @Test
  public void shouldReturnBlockWhenSlotQuery() throws Exception {
    final String slotNum = "1";
    final Map<String, List<String>> params = Map.of("slot", List.of(slotNum));

    when(context.queryParamMap()).thenReturn(params);
    when(context.queryParam(any())).thenReturn(slotNum);
    when(storageClient.getStore()).thenReturn(store);
    when(storageClient.getBlockRootBySlot(any())).thenReturn(Optional.of(blockRoot));
    when(store.getSignedBlock(any())).thenReturn(signedBeaconBlock);
    when(historicalChainData.getFinalizedBlockAtSlot(any()))
        .thenReturn(SafeFuture.completedFuture(empty()));

    handler.handle(context);

    final String jsonResponse =
        jsonProvider.objectToJSON(new BeaconBlockResponse(signedBeaconBlock));
    verify(context).result(jsonResponse);
  }

  @Test
  public void shouldReturnBlockWhenEpochQueryAndNoBlockRoot() throws Exception {
    final String epochNum = "1";
    final Map<String, List<String>> params = Map.of("epoch", List.of(epochNum));

    when(context.queryParamMap()).thenReturn(params);
    when(context.queryParam(any())).thenReturn(epochNum);
    when(storageClient.getBlockRootBySlot(any())).thenReturn(empty());
    when(historicalChainData.getFinalizedBlockAtSlot(any()))
        .thenReturn(SafeFuture.completedFuture(Optional.of(signedBeaconBlock)));

    handler.handle(context);

    final String jsonResponse =
        jsonProvider.objectToJSON(new BeaconBlockResponse(signedBeaconBlock));
    verify(context).result(jsonResponse);
  }

  @Test
  public void shouldReturnBlockWhenSlotQueryAndNoBlockRoot() throws Exception {
    final String slotNum = "1";
    final Map<String, List<String>> params = Map.of("slot", List.of(slotNum));

    when(context.queryParamMap()).thenReturn(params);
    when(context.queryParam(any())).thenReturn(slotNum);
    when(storageClient.getBlockRootBySlot(any())).thenReturn(empty());
    when(historicalChainData.getFinalizedBlockAtSlot(any()))
        .thenReturn(SafeFuture.completedFuture(Optional.of(signedBeaconBlock)));

    handler.handle(context);

    final String jsonResponse =
        jsonProvider.objectToJSON(new BeaconBlockResponse(signedBeaconBlock));
    verify(context).result(jsonResponse);
  }

  @Test
  public void shouldFailWhenNoParams() throws Exception {
    handler.handle(realContext);

    final String actualResponse = realContext.resultString();
    final String expectedResponse = jsonProvider.objectToJSON(BAD_REQUEST);

    assertThat(actualResponse).isEqualTo(expectedResponse);
  }

  @Test
  public void shouldFailWithEmptyRootParamValue() throws Exception {
    final Map<String, List<String>> params = Map.of("root", emptyList());

    doReturn(params).when(realContext).queryParamMap();
    doReturn(null).when(realContext).queryParam(any());

    handler.handle(realContext);
    assertThat(realContext.resultString()).contains("root").contains("cannot be null or empty");
  }

  @Test
  public void shouldFailWithEmptyEpochParamValue() throws Exception {
    final Map<String, List<String>> params = Map.of("epoch", emptyList());

    doReturn(params).when(realContext).queryParamMap();
    doReturn(null).when(realContext).queryParam(any());

    handler.handle(realContext);
    assertThat(realContext.resultString()).contains("epoch").contains("cannot be null or empty");
  }

  @Test
  public void shouldFailWithEmptySlotParamValue() throws Exception {
    final Map<String, List<String>> params = Map.of("slot", emptyList());

    doReturn(params).when(realContext).queryParamMap();
    doReturn(null).when(realContext).queryParam(any());

    handler.handle(realContext);
    assertThat(realContext.resultString()).contains("slot").contains("cannot be null or empty");
  }
}
