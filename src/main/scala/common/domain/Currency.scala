package es.eriktorr.langchain4s
package common.domain

import common.data.refined.Constraints.CurrencyCode

import io.github.iltotore.iron.*

opaque type Currency <: String :| CurrencyCode = String :| CurrencyCode

object Currency extends RefinedTypeOps[String, CurrencyCode, Currency]
