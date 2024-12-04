package es.eriktorr.langchain4s
package accounts.domain

import common.domain.{Country, Currency}

final case class Account(accountId: AccountId, country: Country, currency: Currency)
