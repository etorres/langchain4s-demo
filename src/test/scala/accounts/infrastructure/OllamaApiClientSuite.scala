package es.eriktorr.langchain4s
package accounts.infrastructure

import accounts.application.OllamaTestConfig
import common.infrastructure.HttpClient.httpClientWith
import spec.TestFilters.online
import spec.AsyncSuite

import cats.effect.{IO, Resource}
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.{Duration, DurationInt}

final class OllamaApiClientSuite extends AsyncSuite:
  test("should download a model from the ollama library and load it into memory".tag(online)):
    (for
      logger <- Resource.eval(Slf4jLogger.fromName[IO]("debug-logger"))
      httpClient <- httpClientWith(OllamaApiClientSuite.timeout, verbose)(using logger)
      apiClient = OllamaApiClient.impl(OllamaTestConfig.tinyLlamaLocalContainer.config, httpClient)
    yield apiClient)
      .use: apiClient =>
        (for
          _ <- apiClient.pullModel
          _ <- apiClient.loadModel
        yield ()).assertEquals((), "successfully download the model")

  override def munitIOTimeout: Duration = OllamaApiClientSuite.timeout + 1.seconds
end OllamaApiClientSuite

object OllamaApiClientSuite:
  private val timeout = 2.minutes
