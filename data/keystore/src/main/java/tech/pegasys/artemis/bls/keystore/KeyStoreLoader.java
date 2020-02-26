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

package tech.pegasys.artemis.bls.keystore;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.artemis.bls.keystore.model.KeyStoreData;

/** Provide utility methods to load/store BLS KeyStore from json format */
public class KeyStoreLoader {
  private static final Logger LOG = LogManager.getLogger();
  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().registerModule(new KeyStoreBytesModule());

  public static KeyStoreData loadFromFile(final Path keystoreFile)
      throws KeyStoreValidationException {
    KeyStorePreConditions.checkNotNull(keystoreFile, "KeyStore path cannot be null");

    try {
      final KeyStoreData keyStoreData =
          OBJECT_MAPPER.readValue(keystoreFile.toFile(), KeyStoreData.class);

      if (keyStoreData.getVersion() != KeyStoreData.KEYSTORE_VERSION) {
        throw new KeyStoreValidationException(
            String.format(
                "Error in parsing keystore: The KeyStore version %d is not supported",
                keyStoreData.getVersion()));
      }

      return keyStoreData;
    } catch (final JsonParseException e) {
      throw new KeyStoreValidationException("Error in parsing keystore: Invalid Json format", e);
    } catch (final JsonMappingException e) {
      final String cause;
      if (e.getCause() instanceof KeyStoreValidationException) {
        cause = e.getCause().getMessage();
      } else {
        cause = "Missing required json elements";
      }
      throw new KeyStoreValidationException(
          String.format("Error in parsing keystore: %s", cause), e);
    } catch (final IOException e) {
      LOG.error("Error in parsing keystore: " + e.getMessage());
      throw new KeyStoreValidationException("Error in parsing keystore: " + e.getMessage(), e);
    }
  }

  public static void saveToFile(final Path keystoreFile, final KeyStoreData keyStoreData) {
    KeyStorePreConditions.checkNotNull(keystoreFile, "KeyStore path cannot be null");
    KeyStorePreConditions.checkNotNull(keyStoreData, "KeyStore data cannot be null");

    try {
      Files.writeString(keystoreFile, toJson(keyStoreData), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new KeyStoreValidationException(
          "Error in writing KeyStore to file: " + e.getMessage(), e);
    }
  }

  public static String toJson(final KeyStoreData keyStoreData) {
    KeyStorePreConditions.checkNotNull(keyStoreData, "KeyStore data cannot be null");

    try {
      return KeyStoreLoader.OBJECT_MAPPER
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(keyStoreData);
    } catch (final JsonProcessingException e) {
      throw new KeyStoreValidationException(
          "Error in converting KeyStore to Json: " + e.getMessage(), e);
    }
  }
}
