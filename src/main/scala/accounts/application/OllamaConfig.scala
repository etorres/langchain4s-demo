package es.eriktorr.langchain4s
package accounts.application

import accounts.infrastructure.OllamaModel

import com.comcast.ip4s.{Host, Port}

final case class OllamaConfig(host: Host, insecure: Boolean, model: OllamaModel, port: Port):
  def baseUrl: String =
    val protocol = if insecure then "http" else "https"
    s"$protocol://$host:$port"
