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

package tech.pegasys.teku.storage.server.sql;

import com.google.common.annotations.VisibleForTesting;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.nio.file.Path;
import org.flywaydb.core.Flyway;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.sqlite.SQLiteDataSource;
import tech.pegasys.teku.infrastructure.metrics.TekuMetricCategory;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.storage.server.Database;
import tech.pegasys.teku.storage.server.blob.BlobStorage;
import tech.pegasys.teku.storage.server.blob.BlobStorageSchema;
import tech.pegasys.teku.storage.server.blob.RocksDbBlobStorage;
import tech.pegasys.teku.storage.server.rocksdb.RocksDbConfiguration;
import tech.pegasys.teku.storage.server.rocksdb.core.RocksDbInstanceFactory;
import tech.pegasys.teku.util.config.InvalidConfigurationException;
import tech.pegasys.teku.util.config.StateStorageMode;

public class SqlDatabaseFactory {

  static final String DB_FILENAME = "teku.db";

  public static Database create(
      final Path dbDir,
      final StateStorageMode stateStorageMode,
      final long stateStorageFrequency,
      final MetricsSystem metricsSystem,
      final RocksDbConfiguration rocksDbConfiguration) {
    final ComboPooledDataSource dataSource = initDataSource(dbDir);

    final PlatformTransactionManager transactionManager =
        new DataSourceTransactionManager(dataSource);

    final BlobStorage blobStorage = createBlobStorage(metricsSystem, rocksDbConfiguration);
    return new SqlDatabase(
        metricsSystem,
        new SqlChainStorage(transactionManager, dataSource, blobStorage),
        new SqlEth1Storage(transactionManager, dataSource),
        new SqlProtoArrayStorage(transactionManager, dataSource),
        stateStorageMode,
        UInt64.valueOf(stateStorageFrequency));
  }

  private static RocksDbBlobStorage createBlobStorage(
      final MetricsSystem metricsSystem, final RocksDbConfiguration hotDbConfiguration) {
    return new RocksDbBlobStorage(
        RocksDbInstanceFactory.create(
            metricsSystem,
            TekuMetricCategory.STORAGE,
            hotDbConfiguration,
            BlobStorageSchema.class));
  }

  @VisibleForTesting
  static ComboPooledDataSource initDataSource(final Path dbDir) {
    if (!dbDir.toFile().mkdir() && !dbDir.toFile().isDirectory()) {
      throw new InvalidConfigurationException(
          "Unable to create database directory: " + dbDir.toAbsolutePath());
    }
    final ComboPooledDataSource dataSource = createDataSource(dbDir);

    final Flyway flyway = Flyway.configure().dataSource(dataSource).load();

    // Start the migration
    flyway.migrate();
    return dataSource;
  }

  @VisibleForTesting
  static ComboPooledDataSource createDataSource(final Path dbDir) {
    final ComboPooledDataSource dataSource = new ComboPooledDataSource();
    dataSource.setDataSourceName(SQLiteDataSource.class.getName());
    dataSource.setJdbcUrl(
        "jdbc:sqlite:"
            + dbDir.resolve(DB_FILENAME).toAbsolutePath()
            + "?journal_mode=WAL&busy_timeout=5000&mmap_size=268435456");
    dataSource.setMaxStatements(250);
    return dataSource;
  }
}
