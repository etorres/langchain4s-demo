package es.eriktorr.langchain4s
package spec

import munit.Tag

object TestFilters:
  val envVars: Tag = new Tag("envVars")
  val online: Tag = new Tag("online")

  val envVarsName: String = "TEST_ENV_VARS"
