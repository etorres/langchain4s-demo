package es.eriktorr.langchain4s
package accounts.infrastructure

import accounts.application.OllamaConfig
import accounts.infrastructure.OllamaApiClient.ApiError.{GenerateFailed, PullFailed}
import common.data.error.HandledError

import cats.effect.IO
import cats.implicits.{catsSyntaxTuple5Semigroupal, showInterpolator}
import io.circe.syntax.given
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.http4s.circe.CirceEntityCodec.given
import org.http4s.client.Client
import org.http4s.{Method, Request, Uri}

import java.time.Instant

trait OllamaApiClient:
  def loadModel: IO[Unit]

  def pullModel: IO[Unit]

object OllamaApiClient:
  def impl(config: OllamaConfig, httpClient: Client[IO]): OllamaApiClient = new OllamaApiClient:
    override def loadModel: IO[Unit] =
      val request =
        Request[IO](Method.POST, Uri.unsafeFromString(s"${config.baseUrl}/api/generate"))
          .withEntity(
            GenerateRequest(
              keepAlive = Some("30m"),
              model = config.model.name,
              stream = Some(false),
            ).asJson,
          )
      httpClient
        .expect[GenerateResponse](request)
        .map(response => response.done && response.doneReason == "load")
        .ifM(IO.unit, IO.raiseError(GenerateFailed(config.model)))

    override def pullModel: IO[Unit] =
      val request =
        Request[IO](Method.POST, Uri.unsafeFromString(s"${config.baseUrl}/api/pull"))
          .withEntity(
            PullRequest(
              insecure = Some(true),
              model = config.model.name,
              stream = Some(false),
            ).asJson,
          )
      httpClient
        .expect[PullResponse](request)
        .map(_.status == "success")
        .ifM(IO.unit, IO.raiseError(PullFailed(config.model)))

  final private case class GenerateRequest(
      keepAlive: Option[String],
      model: String,
      stream: Option[Boolean],
  )

  private object GenerateRequest:
    given Encoder[GenerateRequest] = (request: GenerateRequest) =>
      Json.obj(
        ("keep_alive", Json.fromStringOrNull(request.keepAlive)),
        ("model", Json.fromString(request.model)),
        ("stream", Json.fromBooleanOrNull(request.stream)),
      )

  final private case class GenerateResponse(
      createdAt: Instant,
      done: Boolean,
      doneReason: String,
      model: String,
      response: String,
  )

  private object GenerateResponse:
    given Decoder[GenerateResponse] = (cursor: HCursor) =>
      (
        cursor.downField("created_at").as[Instant],
        cursor.downField("done").as[Boolean],
        cursor.downField("done_reason").as[String],
        cursor.downField("model").as[String],
        cursor.downField("response").as[String],
      ).mapN(GenerateResponse.apply)

  final private case class PullRequest(
      insecure: Option[Boolean],
      model: String,
      stream: Option[Boolean],
  )

  private object PullRequest:
    given Encoder[PullRequest] = (request: PullRequest) =>
      Json.obj(
        ("insecure", Json.fromBooleanOrNull(request.insecure)),
        ("model", Json.fromString(request.model)),
        ("stream", Json.fromBooleanOrNull(request.stream)),
      )

  final private case class PullResponse(status: String)

  private object PullResponse:
    given Decoder[PullResponse] = (cursor: HCursor) =>
      cursor.downField("status").as[String].map(PullResponse.apply)

  sealed abstract class ApiError(message: String) extends HandledError(message)

  object ApiError:
    final case class GenerateFailed(model: OllamaModel)
        extends HandledError(show"Failed to load the model $model into memory")

    final case class PullFailed(model: OllamaModel)
        extends HandledError(show"Failed to download the model $model from the ollama library")
