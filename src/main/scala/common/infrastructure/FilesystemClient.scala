package es.eriktorr.langchain4s
package common.infrastructure

import cats.effect.IO

import java.io.FileNotFoundException
import java.nio.file.{Path, Paths}
import scala.language.unsafeNulls

object FilesystemClient:
  def pathTo(
      resource: String,
      classLoader: ClassLoader = Thread.currentThread().getContextClassLoader,
  ): IO[Path] =
    IO.fromOption(Option(classLoader.getResource(resource)).map(url => Paths.get(url.toURI)))(
      FileNotFoundException(
        s"Resource \"$resource\" was not found in the classpath from the given classloader",
      ),
    )
