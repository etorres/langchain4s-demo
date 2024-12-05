package es.eriktorr.langchain4s
package accounts.domain

import cats.Order
import io.circe.Decoder

enum AccountCommand(val description: String):
  case CreateAccount extends AccountCommand("Create a new account")
  case Help extends AccountCommand("Display a help message")
  case ShowAccount extends AccountCommand("Show account details")

object AccountCommand:
  given Decoder[AccountCommand] = Decoder.decodeString.emap(value =>
    AccountCommand.values.find(_.description == value) match
      case Some(accountCommand) => Right(accountCommand)
      case None => Left(s"Unsupported command found: $value"),
  )

  given Order[AccountCommand] = Order.by(_.description)
