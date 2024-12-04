package es.eriktorr.langchain4s
package accounts.domain

import common.domain.{Country, Currency}

import cats.implicits.catsSyntaxTuple2Semigroupal
import io.circe.{Decoder, HCursor}
import io.github.iltotore.iron.circe.given

final case class CreateAccountRequest(country: Country, currency: Currency)

object CreateAccountRequest:
  given Decoder[CreateAccountRequest] = (cursor: HCursor) =>
    (
      cursor.downField("country").as[Country],
      cursor.downField("currency").as[Currency],
    ).mapN(CreateAccountRequest.apply)
