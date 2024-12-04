package es.eriktorr.langchain4s
package spec

import accounts.application.OllamaTestConfig

abstract class OllamaSuite extends AsyncSuite:
  def ollamaTestConfig: OllamaTestConfig = OllamaTestConfig.`llama3.2LocalContainer`
