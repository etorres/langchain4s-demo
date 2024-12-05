package es.eriktorr.langchain4s
package accounts.domain

import accounts.infrastructure.OllamaChatbot
import accounts.infrastructure.OllamaChatbot.ChatbotError
import common.domain.{Message, Response, SessionId}

import cats.effect.IO
import cats.effect.std.UUIDGen
import org.typelevel.log4cats.Logger

final class AccountsAgent(accountsService: AccountsService, ollamaChatbot: OllamaChatbot)(using
    logger: Logger[IO],
    uuidGen: UUIDGen[IO],
):
  def run(message: Message): IO[Response] = for
    uuid <- uuidGen.randomUUID
    sessionId <- IO.fromEither(SessionId.either(uuid))
    commandOrError <- ollamaChatbot.commandFrom(message, sessionId)
    response <- commandOrError match
      case Left(error) =>
        logger
          .warn(error)("Command not found")
          .map(_ =>
            Response.applyUnsafe(
              s"The following actions are supported: ${AccountCommand.values.toList.map(_.description).mkString(", ")}",
            ),
          )
      case Right(command) =>
        command match
          case AccountCommand.CreateAccount =>
            for
              requestOrError <- ollamaChatbot.createAccountRequestFrom(message, sessionId)
              response <- requestOrError match
                case Left(error) =>
                  logger.warn(error)("Arguments not found") *> IO.raiseError(
                    IllegalArgumentException("Not implemented"), // TODO
                  )
                case Right(request) =>
                  for
                    account <- accountsService.createAccount(request)
                    response = Response.applyUnsafe(
                      s"New account created with id: ${account.accountId}",
                    )
                  yield response
            yield response
          case AccountCommand.Help => ollamaChatbot.help
          case AccountCommand.ShowAccount =>
            IO.raiseError(IllegalArgumentException("Not implemented")) // TODO
  yield response
