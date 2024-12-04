package es.eriktorr.langchain4s
package spec

import munit.CatsEffectSuite

abstract class AsyncSuite extends CatsEffectSuite:
  def verbose: Boolean = false
