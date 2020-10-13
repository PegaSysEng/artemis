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

package tech.pegasys.teku.cli.options;

import static tech.pegasys.teku.cli.options.BeaconRestApiOptions.DEFAULT_REST_API_PORT;

import picocli.CommandLine.Option;

public class ValidatorClientOptions {

  @Option(
      names = {"--beacon-node-api-endpoint"},
      paramLabel = "<ENDPOINT>",
      description = "Endpoint of the Beacon Node REST API",
      arity = "1")
  private String beaconNodeApiEndpoint = "http://127.0.0.1:" + DEFAULT_REST_API_PORT;

  public String getBeaconNodeApiEndpoint() {
    return beaconNodeApiEndpoint;
  }
}
