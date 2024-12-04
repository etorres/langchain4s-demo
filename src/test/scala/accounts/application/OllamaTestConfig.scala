package es.eriktorr.langchain4s
package accounts.application

import accounts.infrastructure.OllamaModel

import com.comcast.ip4s.{Host, Port}

@SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
enum OllamaTestConfig(val config: OllamaConfig):
  case llama3LocalContainer
      extends OllamaTestConfig(
        OllamaConfig(
          host = Host.fromString(OllamaTestConfig.host).get,
          insecure = OllamaTestConfig.insecure,
          model = OllamaModel.LLAMA_3,
          port = Port.fromInt(OllamaTestConfig.port).get,
        ),
      )
  case `llama3.2LocalContainer`
      extends OllamaTestConfig(
        OllamaConfig(
          host = Host.fromString(OllamaTestConfig.host).get,
          insecure = OllamaTestConfig.insecure,
          model = OllamaModel.LLAMA_3_2,
          port = Port.fromInt(OllamaTestConfig.port).get,
        ),
      )
  case tinyLlamaLocalContainer
      extends OllamaTestConfig(
        OllamaConfig(
          host = Host.fromString(OllamaTestConfig.host).get,
          insecure = OllamaTestConfig.insecure,
          model = OllamaModel.TINY_LLAMA,
          port = Port.fromInt(OllamaTestConfig.port).get,
        ),
      )

object OllamaTestConfig:
  final private val host = "localhost"
  final private val insecure = true
  final private val port = 11434
