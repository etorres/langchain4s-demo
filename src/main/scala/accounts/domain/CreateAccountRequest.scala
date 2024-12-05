package es.eriktorr.langchain4s
package accounts.domain

import common.domain.{Country, Currency}

final case class CreateAccountRequest(country: Country, currency: Currency)
