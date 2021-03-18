import app._
import api._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.{Method, Request, Uri, UriTemplate}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io.GET
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.UriTemplate._
import cats.implicits._
import cats.effect._
import io.circe.generic.auto._
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.concurrent._

class IntegrationTestSuite extends AnyFlatSpec with Matchers with ConsoleLogger {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  private val blockingPool: ExecutorService = Executors.newFixedThreadPool(1)
  private val blocker: Blocker = Blocker.liftExecutorService(blockingPool)
  private val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

  def buildUri(path: String): Uri = {
    val requestUriTemplate: UriTemplate = UriTemplate(
      authority = Some(Uri.Authority(host = Uri.RegName(Const.IP), port = Some(Const.Port))),
      scheme = Some(Uri.Scheme.http),
      path = path.split("/").map(PathElm).toList)

      requestUriTemplate.toUriIfPossible.get
    }

  def requestParsing(url: UrlValue): IO[String] = {
    val req = Request[IO](method = Method.POST, uri = buildUri("requests"))
      .withEntity(url)
    httpClient.expect[String](req)
  }

  "Rest API call" should "add items to queue" in {
    val expectedResult = Vector(
      ("https://google.com/", "Google"),
      ("https://www.reddit.com/","reddit: the front page of the internet"),
      ("https://stackoverflow.com/","Stack Overflow - Where Developers Learn, Share, & Build Careers")
    )

    val webSites: Vector[String] = expectedResult.map(_._1)
    val webSitesVisits: IO[Vector[String]] = webSites.traverse(url => requestParsing(UrlValue(url)))
    val webSitesVisitsEffects: IO[String] = webSitesVisits.map(_.mkString("\n"))

    log("\nStarting Integration Test 1")
    Main.main(Array[String]()) // init server
    Thread.sleep(1000) // time to init API

    webSitesVisitsEffects.unsafeRunAsyncAndForget()
    log("\nDone sending URLs to parse")
    Thread.sleep(10000) // wait to finish parsing

    log("\nLooking for results")
    val result: Seq[(String, String)] = webSites.map { url =>
      val req = GET(buildUri(s"requests-result/${UrlValue(url).hash}"))
      val title: String = httpClient.expect[String](req).unsafeRunSync()
      (url, title)
    }

    result.foreach { case (url, title) => log(s"$url -> $title") }
    log("\nFinished Integration Test 1")

    Main.fiber.cancel.unsafeRunSync()
    blockingPool.shutdown()

    result should contain theSameElementsAs expectedResult
  }
}
