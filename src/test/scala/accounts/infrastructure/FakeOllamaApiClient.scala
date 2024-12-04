package es.eriktorr.langchain4s
package accounts.infrastructure

import accounts.infrastructure.OllamaApiClient.ApiError.{GenerateFailed, PullFailed}

import cats.effect.{IO, Resource}

final class FakeOllamaApiClient(
    model: OllamaModel = OllamaModel.TINY_LLAMA,
    successful: Boolean = true,
) extends OllamaApiClient:
  override def loadModel: IO[Unit] =
    if successful then IO.unit else IO.raiseError(GenerateFailed(model))

  override def pullModel: IO[Unit] =
    if successful then IO.unit else IO.raiseError(PullFailed(model))

object FakeOllamaApiClient:
  def resource(): Resource[IO, FakeOllamaApiClient] =
    Resource.pure[IO, FakeOllamaApiClient](FakeOllamaApiClient())
