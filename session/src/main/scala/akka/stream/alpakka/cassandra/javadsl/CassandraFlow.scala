/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.stream.alpakka.cassandra.javadsl

import akka.NotUsed
import akka.stream.alpakka.cassandra.CassandraWriteSettings
import akka.stream.alpakka.cassandra.scaladsl
import akka.stream.javadsl.{ Flow, FlowWithContext }
import com.datastax.oss.driver.api.core.cql.{ BoundStatement, PreparedStatement }

/**
 * Java API to create Cassandra flows.
 */
object CassandraFlow {

  /**
   * A flow writing to Cassandra for every stream element.
   * The element is emitted unchanged.
   *
   * @param session Cassandra session from `CassandraSessionRegistry`
   * @param writeSettings settings to configure the write operation
   * @param cqlStatement raw CQL statement
   * @param statementBinder function to bind data from the stream element to the prepared statement
   * @tparam T stream element type
   */
  def create[T](
      session: CassandraSession,
      writeSettings: CassandraWriteSettings,
      cqlStatement: String,
      statementBinder: akka.japi.Function2[T, PreparedStatement, BoundStatement]): Flow[T, T, NotUsed] =
    scaladsl.CassandraFlow
      .create(writeSettings, cqlStatement, (t, preparedStatement) => statementBinder.apply(t, preparedStatement))(
        session.delegate)
      .asJava

  /**
   * A flow writing to Cassandra for every stream element, passing context along.
   * The element and context are emitted unchanged.
   *
   * @param session Cassandra session from `CassandraSessionRegistry`
   * @param writeSettings settings to configure the write operation
   * @param cqlStatement raw CQL statement
   * @param statementBinder function to bind data from the stream element to the prepared statement
   * @tparam T stream element type
   * @tparam Ctx context type
   */
  def withContext[T, Ctx](
      session: CassandraSession,
      writeSettings: CassandraWriteSettings,
      cqlStatement: String,
      statementBinder: akka.japi.Function2[T, PreparedStatement, BoundStatement])
      : FlowWithContext[T, Ctx, T, Ctx, NotUsed] = {
    scaladsl.CassandraFlow
      .withContext(writeSettings, cqlStatement, (t, preparedStatement) => statementBinder.apply(t, preparedStatement))(
        session.delegate)
      .asJava
  }

  /**
   * Creates a flow that batches using an unlogged batch. Use this when most of the elements in the stream
   * share the same partition key. Cassandra unlogged batches that share the same partition key will only
   * resolve to one write internally in Cassandra, boosting write performance.
   *
   * Be aware that this stage does NOT preserve the upstream order.
   *
   * @param session Cassandra session from `CassandraSessionRegistry`
   * @param writeSettings settings to configure the write operation
   * @param cqlStatement raw CQL statement
   * @param statementBinder function to bind data from the stream element to the prepared statement
   * @tparam T stream element type
   * @tparam K extracted key type for grouping into batches
   */
  def createUnloggedBatch[T, K](
      session: CassandraSession,
      writeSettings: CassandraWriteSettings,
      cqlStatement: String,
      statementBinder: akka.japi.Function2[T, PreparedStatement, BoundStatement],
      partitionKey: akka.japi.Function[T, K]): Flow[T, T, NotUsed] = {
    scaladsl.CassandraFlow
      .createUnloggedBatch(
        writeSettings,
        cqlStatement,
        (t, preparedStatement) => statementBinder.apply(t, preparedStatement),
        t => partitionKey.apply(t))(session.delegate)
      .asJava
  }

}
