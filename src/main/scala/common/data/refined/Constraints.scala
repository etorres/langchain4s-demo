package es.eriktorr.langchain4s
package common.data.refined

import io.github.iltotore.iron.DescribedAs
import io.github.iltotore.iron.constraint.any.Not
import io.github.iltotore.iron.constraint.char.{Letter, UpperCase}
import io.github.iltotore.iron.constraint.collection.{FixedLength, ForAll}
import io.github.iltotore.iron.constraint.string.Blank

object Constraints:
  type CountryCode =
    DescribedAs[
      ForAll[Letter & UpperCase] & FixedLength[2],
      "The country should be 2 uppercase characters",
    ]

  type CurrencyCode =
    DescribedAs[
      ForAll[Letter & UpperCase] & FixedLength[3],
      "The currency should be 3 uppercase characters",
    ]

  type NonEmptyString =
    DescribedAs[Not[Blank], "Should contain at least one non-whitespace character"]
