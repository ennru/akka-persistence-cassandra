/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.cassandra.session.impl

import akka.annotation.InternalApi
import akka.stream.stage.{ AsyncCallback, GraphStage, GraphStageLogic, OutHandler }
import akka.stream.{ Attributes, Outlet, SourceShape }
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{ AsyncResultSet, Row, Statement }

import scala.compat.java8.FutureConverters._
import scala.concurrent.{ ExecutionContext, Future }

@InternalApi
private[session] class SelectSource[S <: Statement[S]](underlying: Future[CqlSession], stmt: Future[S])(
    implicit executionContext: ExecutionContext)
    extends GraphStage[SourceShape[Row]] {

  private val out: Outlet[Row] = Outlet("rows")
  override val shape: SourceShape[Row] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {

      var asyncResult: AsyncCallback[AsyncResultSet] = _
      var asyncFailure: AsyncCallback[Throwable] = _
      var resultSet: Option[AsyncResultSet] = None
      var page: java.util.Iterator[Row] = java.util.Collections.emptyIterator()

      override def preStart(): Unit = {
        asyncResult = getAsyncCallback[AsyncResultSet] { rs =>
          resultSet = Some(rs)
          page = rs.currentPage().iterator()
          tryPushOne()
        }
        asyncFailure = getAsyncCallback { e =>
          fail(out, e)
        }
        stmt.failed.foreach(e => asyncFailure.invoke(e))
        stmt.foreach { s =>
          val rsFut = underlying.flatMap(_.executeAsync(s).toScala)
          rsFut.failed.foreach { e =>
            asyncFailure.invoke(e)
          }
          rsFut.foreach(asyncResult.invoke)
        }
      }

      setHandler(out, new OutHandler {
        override def onPull(): Unit =
          tryPushOne()
      })

      def tryPushOne(): Unit = {
        if (page.hasNext) {
          emitMultiple(out, page)
          page = java.util.Collections.emptyIterator()
        } else {
          resultSet match {
            case Some(rs) if rs.hasMorePages =>
              val fetch = rs.fetchNextPage().toScala
              fetch.failed.foreach { e =>
                asyncFailure.invoke(e)
              }
              fetch.foreach(asyncResult.invoke)

            case Some(rs) =>
              complete(out)

            case _ =>
          }
        }
      }
    }

}
