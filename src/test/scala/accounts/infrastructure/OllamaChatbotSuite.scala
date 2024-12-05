package es.eriktorr.langchain4s
package accounts.infrastructure

import accounts.application.OllamaTestConfig
import accounts.domain.{AccountCommand, CreateAccountRequest}
import accounts.infrastructure.OllamaChatbot.ChatbotError.{UnmetRequirements, UnsupportedCommand}
import common.domain.*
import spec.OllamaSuite

import cats.data.NonEmptyChain
import cats.effect.IO
import cats.effect.std.UUIDGen
import cats.implicits.{catsKernelOrderingForOrder, catsSyntaxEither}

import scala.concurrent.duration.{Duration, DurationInt}

final class OllamaChatbotSuite extends OllamaSuite:
  test("should get the action from a user message"):
    runWith(
      (chatbot, message, sessionId) => chatbot.commandFrom(message, sessionId),
      "Create a new Real account for a Brazilian client",
    ).assertEquals(Right(AccountCommand.CreateAccount))

  test("should fail with an error when no action can be found"):
    runWith(
      (chatbot, message, sessionId) => chatbot.commandFrom(message, sessionId),
      "Sudo: make me a sandwich",
    ).assertEquals(Left(UnsupportedCommand))

  test("should get the country and currency from the request"):
    runWith(
      (chatbot, message, sessionId) => chatbot.createAccountRequestFrom(message, sessionId),
      "Create a new Real account for a Brazilian client",
    ).assertEquals(
      Right(CreateAccountRequest(Country.applyUnsafe("BR"), Currency.applyUnsafe("BRL"))),
    )

  test("should get the country and infer the currency from the request"):
    runWith(
      (chatbot, message, sessionId) => chatbot.createAccountRequestFrom(message, sessionId),
      "Create a new account for a client who lives in Japan",
    ).assertEquals(
      Right(CreateAccountRequest(Country.applyUnsafe("JP"), Currency.applyUnsafe("JPY"))),
    )

  test("should get non-matching country and currency from the request"):
    runWith(
      (chatbot, message, sessionId) => chatbot.createAccountRequestFrom(message, sessionId),
      "Create a new USD account for a client in Argentina",
    ).assertEquals(
      Right(CreateAccountRequest(Country.applyUnsafe("AR"), Currency.applyUnsafe("USD"))),
    )

  test("should fail with an error when some requirements cannot be found"):
    runWith(
      (chatbot, message, sessionId) => chatbot.createAccountRequestFrom(message, sessionId),
      "Sudo: make me a sandwich",
    ).assertEquals(
      Left(
        UnmetRequirements(
          NonEmptyChain.of(
            Requirement.applyUnsafe("The country must be 2 uppercase characters"),
            Requirement.applyUnsafe("The currency must be 3 uppercase characters"),
          ),
        ),
      ),
    )

  test("should generate a help message"):
    runWith(_.help).assert(_.nonEmpty, "expected a non-empty response")

  test("should list possible actions"):
    runWith(testee =>
      for
        obtained <- testee.listActions
        sortedObtained = obtained.map(_.sorted)
      yield sortedObtained,
    ).assertEquals(Right(AccountCommand.values.toList.sorted))

  private def runWith[T](testee: OllamaChatbot => IO[T]) = testResources.use(testee)

  private def runWith[T](
      testee: (OllamaChatbot, Message, SessionId) => IO[T],
      rawMessage: String,
  ) = testResources.use: chatbot =>
    for
      uuid <- UUIDGen[IO].randomUUID
      sessionId <- IO.fromEither(SessionId.either(uuid))
      message <- IO.fromEither(Message.either(rawMessage).leftMap(IllegalArgumentException(_)))
      obtained <- testee(chatbot, message, sessionId)
    yield obtained

  private def testResources = for
    apiClient <- FakeOllamaApiClient.resource()
    chatbot <- OllamaChatbot.resource(
      apiClient = apiClient,
      config = OllamaTestConfig.`llama3.2LocalContainer`.config,
      verbose = verbose,
    )
  yield chatbot

  override def munitIOTimeout: Duration = OllamaChatbotSuite.timeout + 1.seconds
end OllamaChatbotSuite

object OllamaChatbotSuite:
  private val timeout = 2.minutes
