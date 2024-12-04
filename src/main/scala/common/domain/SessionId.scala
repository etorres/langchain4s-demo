package es.eriktorr.langchain4s
package common.domain

import cats.implicits.catsSyntaxEither
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.string.ValidUUID

import java.util.UUID

opaque type SessionId <: String :| ValidUUID = String :| ValidUUID

object SessionId extends RefinedTypeOps[String, ValidUUID, SessionId]:
  def either(uuid: UUID): Either[Throwable, SessionId] =
    SessionId.either(uuid.toString).leftMap(IllegalArgumentException(_))
