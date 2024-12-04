package es.eriktorr.langchain4s

import cats.effect.{ExitCode, IO}
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import org.typelevel.log4cats.slf4j.Slf4jLogger

object LangChain4sApp extends CommandIOApp(name = "langchain4s-demo", header = "LangChain4s Demo"):
  override def main: Opts[IO[ExitCode]] = Opts(for
    logger <- Slf4jLogger.create[IO]
    _ <- logger.info("Start")
  yield ExitCode.Success) // TODO
