package es.eriktorr.langchain4s
package common.domain

import common.data.refined.Constraints.NonEmptyString

import io.github.iltotore.iron.*

opaque type Response <: String :| NonEmptyString = String :| NonEmptyString

object Response extends RefinedTypeOps[String, NonEmptyString, Response]
