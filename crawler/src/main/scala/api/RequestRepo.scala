package api

import app.{ConsoleLogger, Parser}
import cats.effect.IO

import scala.collection.mutable
import scala.util.{Success, Try}
import scala.jdk.CollectionConverters._

trait RequestRepo {
  def addRequest(url: UrlValue): IO[Unit]
  def getRequestResult(requestId: Int): IO[Either[Unit, String]]
}

object RequestRepo {
  class DummyImpl extends RequestRepo with ConsoleLogger {
    val synchronizedMap: mutable.Map[Int, String] = new java.util.concurrent.ConcurrentHashMap[Int, String]().asScala

    override def addRequest(url: UrlValue): IO[Unit] = IO {
      Try(Parser.pageTitle(url)) match {
        case Success(res) => log(s"\nAdding item to map: ${url.str}")
          synchronizedMap.addOne(url.hash, res) // URL is problematic to pass using GET
        case _ =>
      }
    }

    override def getRequestResult(requestId: Int): IO[Either[Unit, String]] = IO {
      synchronizedMap.get(requestId) match {
        case Some(result) => Right(result)
        case None => Left(())
      }
    }
  }
}
