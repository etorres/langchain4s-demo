package es.eriktorr.langchain4s
package accounts.infrastructure

import cats.Show

enum OllamaModel(val name: String):
  case LLAMA_3 extends OllamaModel("llama3")
  case LLAMA_3_2 extends OllamaModel("llama3.2")
  case TINY_LLAMA extends OllamaModel("tinyllama")

object OllamaModel:
  given Show[OllamaModel] = Show.fromToString
