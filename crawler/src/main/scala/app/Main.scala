package app

import api.{RequestRepo, RequestRoutes}
import api.RequestRepo.DummyImpl

import cats.data.Kleisli
import cats.effect._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.{Request, Response}
import org.http4s.server.blaze._
import cats.effect.IO

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  private val requestRepo: RequestRepo = new DummyImpl
  val httpRoutes: Kleisli[IO, Request[IO], Response[IO]] = Router[IO](
    "/" -> RequestRoutes.routes(requestRepo)
  ).orNotFound

  val serverBuilder: BlazeServerBuilder[IO] = BlazeServerBuilder[IO](global)
    .bindHttp(Const.Port, Const.IP)
    .withHttpApp(httpRoutes)

  // Run webserver
  val fiber = serverBuilder.resource.use(_ => IO.never).start.unsafeRunSync()

  fiber.join
}
