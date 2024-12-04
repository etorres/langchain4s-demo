package es.eriktorr.langchain4s
package common.domain

import common.data.refined.Constraints.NonEmptyString

import io.github.iltotore.iron.*

opaque type Requirement <: String :| NonEmptyString = String :| NonEmptyString

object Requirement extends RefinedTypeOps[String, NonEmptyString, Requirement]
