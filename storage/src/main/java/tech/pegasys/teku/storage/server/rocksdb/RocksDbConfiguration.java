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

package tech.pegasys.teku.storage.server.rocksdb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import org.rocksdb.CompressionType;

/**
 * Defines the configuration for a RocksDB database. The configuration used when a database is
 * created is written to a metadata.yaml file and reloaded to ensure we continue using compatible
 * values for the lifetime of that database.
 *
 * <p>To preserve backwards compatibility always ensure that the value assigned in field
 * declarations is compatible with existing databases. These values will be used if the field didn't
 * exist at the time the database was created, so typically should match the RocksDB default.
 *
 * <p>If the value to use for new databases, differs from the original, set it in a factory function
 * e.g. {@link #v5ArchiveDefaults()}.
 *
 * <p>Values that are safe to change for existing databases are marked with {@link
 * Access#WRITE_ONLY}. They will not be written to the metadata file but if present, the value will
 * be loaded providing a simple way to experiment with different values without it being fixed at
 * database creation.
 */
@SuppressWarnings("FieldMayBeFinal")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RocksDbConfiguration {
  public static final int DEFAULT_MAX_OPEN_FILES = 1024;
  public static final int DEFAULT_MAX_BACKGROUND_COMPACTIONS = 4;
  public static final int DEFAULT_BACKGROUND_THREAD_COUNT = 4;
  public static final long DEFAULT_CACHE_CAPACITY = 8388608;

  /* --------------- Safe to Change Properties ------------ */

  @JsonProperty(value = "maxOpenFiles", access = Access.WRITE_ONLY)
  private int maxOpenFiles = DEFAULT_MAX_OPEN_FILES;

  @JsonProperty(value = "maxBackgroundCompactions", access = Access.WRITE_ONLY)
  private int maxBackgroundCompactions = DEFAULT_MAX_BACKGROUND_COMPACTIONS;

  @JsonProperty(value = "backgroundThreadCount", access = Access.WRITE_ONLY)
  private int backgroundThreadCount = DEFAULT_BACKGROUND_THREAD_COUNT;

  @JsonProperty(value = "cacheCapacity", access = Access.WRITE_ONLY)
  private long cacheCapacity = DEFAULT_CACHE_CAPACITY;

  /* ---------------     Fixed Properties     ------------ */

  @JsonProperty("compressionType")
  private CompressionType compressionType = CompressionType.NO_COMPRESSION;

  @JsonProperty("bottomMostCompressionType")
  private CompressionType bottomMostCompressionType = CompressionType.NO_COMPRESSION;

  @JsonIgnore private Path databaseDir;

  public static RocksDbConfiguration v3And4Settings(final Path databaseDir) {
    return new RocksDbConfiguration().withDatabaseDir(databaseDir);
  }

  public static RocksDbConfiguration v5HotDefaults() {
    return new RocksDbConfiguration();
  }

  public static RocksDbConfiguration v5ArchiveDefaults() {
    return new RocksDbConfiguration();
  }

  public RocksDbConfiguration withDatabaseDir(final Path databaseDir) {
    this.databaseDir = databaseDir;
    return this;
  }

  public Path getDatabaseDir() {
    return databaseDir;
  }

  public int getMaxOpenFiles() {
    return maxOpenFiles;
  }

  public int getMaxBackgroundCompactions() {
    return maxBackgroundCompactions;
  }

  public int getBackgroundThreadCount() {
    return backgroundThreadCount;
  }

  public long getCacheCapacity() {
    return cacheCapacity;
  }

  public CompressionType getCompressionType() {
    return compressionType;
  }

  public CompressionType getBottomMostCompressionType() {
    return bottomMostCompressionType;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("maxOpenFiles", maxOpenFiles)
        .add("maxBackgroundCompactions", maxBackgroundCompactions)
        .add("backgroundThreadCount", backgroundThreadCount)
        .add("cacheCapacity", cacheCapacity)
        .add("compressionType", compressionType)
        .add("bottomMostCompressionType", bottomMostCompressionType)
        .add("databaseDir", databaseDir)
        .toString();
  }
}
