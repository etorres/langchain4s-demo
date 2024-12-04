package es.eriktorr.langchain4s
package accounts.domain

import cats.effect.IO
import cats.effect.std.UUIDGen

final class AccountsService:
  def createAccount(request: CreateAccountRequest)(using UUIDGen[IO]): IO[Account] =
    for
      uuid <- UUIDGen.randomUUID
      accountId <- IO.fromEither(AccountId.either(uuid))
      account = Account(accountId, request.country, request.currency)
    yield account

// TODO
//  final private class DataExtractor:
//    @Tool(
//      name = "countryCodeFrom",
//      value = Array("Returns a 2-letter code of a given country"),
//    )
//    def countryCodeFrom(name: String): String =
//      Locale.getAvailableLocales
//        .find(_.getDisplayCountry(Locale.US).equalsIgnoreCase(name))
//        .map(_.getCountry)
//        .getOrElse("??")
//
//    @Tool(
//      name = "currencyCodeFrom",
//      value = Array("Returns a 3-letter code of a given currency"),
//    )
//    def currencyCodeFrom(name: String): String =
//      Currency.getAvailableCurrencies.asScala
//        .find(_.getDisplayName.equalsIgnoreCase(name))
//        .map(_.getCurrencyCode)
//        .getOrElse("???")
