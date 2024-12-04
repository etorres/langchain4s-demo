package es.eriktorr.langchain4s
package accounts.domain

import cats.implicits.catsSyntaxEither
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.string.ValidUUID

import java.util.UUID

opaque type AccountId <: String :| ValidUUID = String :| ValidUUID

object AccountId extends RefinedTypeOps[String, ValidUUID, AccountId]:
  def either(uuid: UUID): Either[Throwable, AccountId] =
    AccountId.either(uuid.toString).leftMap(IllegalArgumentException(_))
