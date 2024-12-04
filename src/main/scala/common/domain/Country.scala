package es.eriktorr.langchain4s
package common.domain

import common.data.refined.Constraints.CountryCode

import io.github.iltotore.iron.*

opaque type Country <: String :| CountryCode = String :| CountryCode

object Country extends RefinedTypeOps[String, CountryCode, Country]
