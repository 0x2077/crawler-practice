package api

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import io.circe.generic.auto._

object RequestRoutes {
  def routes(requestRepo: RequestRepo): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._

    // add a request — POST /requests
    // get a request result — GET /requests-result/{hash(url)}

    HttpRoutes.of[IO] {
      case req @ POST -> Root / "requests" => {
        req.decode[UrlValue] {
          url => {
            requestRepo.addRequest(url).unsafeRunAsyncAndForget()
            Ok("Added")
          }
        }
      }

      case _ @ GET -> Root / "requests-result" / id =>
        requestRepo.getRequestResult(id.toInt).flatMap {
          case Left(_) => NotFound("Result wasn't found")
          case Right(result) => Ok(result)
        }
    }
  }
}
