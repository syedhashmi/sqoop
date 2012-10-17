/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sqoop.connector.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.sqoop.common.SqoopException;
import org.apache.sqoop.job.etl.Context;
import org.apache.sqoop.job.etl.Partition;
import org.apache.sqoop.job.etl.Extractor;
import org.apache.sqoop.job.io.DataWriter;

public class GenericJdbcImportExtractor extends Extractor {

  @Override
  public void run(Context context, Partition partition, DataWriter writer) {
    String driver = context.getString(
        GenericJdbcConnectorConstants.CONNECTOR_JDBC_DRIVER);
    String url = context.getString(
        GenericJdbcConnectorConstants.CONNECTOR_JDBC_URL);
    String username = context.getString(
        GenericJdbcConnectorConstants.CONNECTOR_JDBC_USERNAME);
    String password = context.getString(
        GenericJdbcConnectorConstants.CONNECTOR_JDBC_PASSWORD);
    GenericJdbcExecutor executor = new GenericJdbcExecutor(
        driver, url, username, password);

    String query = context.getString(
        GenericJdbcConnectorConstants.CONNECTOR_JDBC_DATA_SQL);
    String conditions =
        ((GenericJdbcImportPartition)partition).getConditions();
    query = query.replace(
        GenericJdbcConnectorConstants.SQL_CONDITIONS_TOKEN, conditions);
    ResultSet resultSet = executor.executeQuery(query);

    try {
      ResultSetMetaData metaData = resultSet.getMetaData();
      int column = metaData.getColumnCount();
      Object[] array = new Object[column];
      while (resultSet.next()) {
        for (int i = 0; i< column; i++) {
          array[i] = resultSet.getObject(i+1);
        }
        writer.writeArrayRecord(array);
      }

    } catch (SQLException e) {
      throw new SqoopException(
          GenericJdbcConnectorError.GENERIC_JDBC_CONNECTOR_0004, e);

    } finally {
      executor.close();
    }
  }

}