package es.eriktorr.langchain4s
package common.domain

import common.data.refined.Constraints.NonEmptyString

import io.github.iltotore.iron.*

opaque type Message <: String :| NonEmptyString = String :| NonEmptyString

object Message extends RefinedTypeOps[String, NonEmptyString, Message]
