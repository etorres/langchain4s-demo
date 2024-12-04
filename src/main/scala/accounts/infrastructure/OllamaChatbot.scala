package es.eriktorr.langchain4s
package accounts.infrastructure

import accounts.application.OllamaConfig
import accounts.domain.{AccountCommand, CreateAccountRequest}
import accounts.infrastructure.OllamaChatbot.ChatbotError.{UnmetRequirements, UnsupportedCommand}
import accounts.infrastructure.OllamaChatbot.{
  ActionAiResponse,
  Assistant,
  ChatbotError,
  CreateAccountAiResponse,
}
import common.data.error.HandledError
import common.domain.*
import common.infrastructure.FilesystemClient

import cats.data.NonEmptyChain
import cats.effect.{IO, Resource}
import cats.implicits.{catsSyntaxEither, catsSyntaxTuple2Parallel, catsSyntaxTuple2Semigroupal}
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.input.PromptTemplate
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.{AiServices, MemoryId, UserMessage}
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.store.embedding.{EmbeddingSearchRequest, EmbeddingStore}
import io.circe.parser.parse
import io.circe.{Decoder, HCursor}
import io.github.iltotore.iron.cats.*
import io.github.iltotore.iron.circe.given

import java.util.Map as JavaMap
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.jdk.CollectionConverters.given
import scala.language.unsafeNulls

final class OllamaChatbot private (
    assistant: Assistant,
    chatModel: ChatLanguageModel,
    embeddingModel: EmbeddingModel,
    embeddingStore: EmbeddingStore[TextSegment],
):
  def commandFrom(
      message: Message,
      sessionId: SessionId,
  ): IO[Either[? <: ChatbotError, AccountCommand]] = for
    promptTemplate <- IO.delay {
      val queryEmbedding = embeddingModel.embed(message).content()
      val embeddingSearchResult = embeddingStore.search(
        EmbeddingSearchRequest
          .builder()
          .maxResults(3)
          .minScore(0.7d)
          .queryEmbedding(queryEmbedding)
          .build(),
      )
      val contents = embeddingSearchResult
        .matches()
        .asScala
        .toList
        .map(_.embedded().text())
        .mkString("\n\n")
      PromptTemplate
        .from(
          s"""Extract an action that you can perform from the following question to the best
             |of your ability: {{userMessage}}
             |
             |Give the action in the form of a JSON object following this structure:
             |`{"action": "Do something"}`
             |Only return JSON, without any explanation, without surrounding markdown code markup.
             |
             |Base your answer on the following information:
             |
             |{{contents}}""".stripMargin,
        )
        .apply(
          variablesFrom(
            "userMessage" -> message,
            "contents" -> contents,
          ),
        )
    }
    aiResponse <- IO.blocking(assistant.chat(sessionId, promptTemplate.text())).timeout(30.seconds)
    command = parse(aiResponse)
      .flatMap(_.as[ActionAiResponse])
      .leftMap(_ => UnsupportedCommand)
      .map(_.accountCommand)
  yield command

  def createAccountRequestFrom(
      message: Message,
      sessionId: SessionId,
  ): IO[Either[? <: ChatbotError, CreateAccountRequest]] = for
    userMessage <- IO.pure(
      s"""Extract the country and currency mentioned in the text below. Give the country and
         |currency in the form of a JSON object following this structure:
         |`{"country": "ES", "currency": "EUR"}`
         |Only return JSON, without any explanation, without surrounding markdown code markup.
         |
         |Here is the text:
         |
         |$message""".stripMargin,
    )
    aiResponse <- IO.blocking(assistant.chat(sessionId, userMessage)).timeout(30.seconds)
    createAccountRequest = parse(aiResponse).flatMap(_.as[CreateAccountAiResponse]) match
      case Left(_) => createAccountRequestFrom(CreateAccountAiResponse.empty)
      case Right(value) => createAccountRequestFrom(value)
  yield createAccountRequest

  private def createAccountRequestFrom(aiResponse: CreateAccountAiResponse) = (
    Country.eitherNec(aiResponse.country.getOrElse("")),
    Currency.eitherNec(aiResponse.currency.getOrElse("")),
  ).parMapN(CreateAccountRequest.apply)
    .leftMap(errors => UnmetRequirements(errors.map(Requirement.applyUnsafe)))

  def help: IO[Response] = generateWith(
    s"""{{document}}
       |Explain the above in 2-3 sentences:""".stripMargin,
  )

  /** Not needed, can be computed from: [[es.eriktorr.langchain4s.accounts.domain.AccountCommand]].
    * @return
    *   The list of actions that can be performed by this agent.
    */
  def listActions: IO[Response] = generateWith(
    s"""Extract the actions you can perform from the document, delimited by ####.
       |Please output the list of actions using <actions></actions>.
       |Respond with "No actions found!" if no actions were found.
       |####
       |{{document}}
       |####""".stripMargin,
  )

  private def generateWith(template: String) = for
    document <- IO.delay(
      Source.fromResource(OllamaChatbot.systemResource).getLines().mkString("\n"),
    )
    promptTemplate = PromptTemplate.from(template).apply(variablesFrom("document" -> document))
    aiResponse <- IO.blocking(chatModel.generate(promptTemplate.text()))
    response <- IO.fromEither(Response.either(aiResponse).leftMap(IllegalArgumentException(_)))
  yield response

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def variablesFrom(
      first: (String, String),
      others: (String, String)*,
  ): JavaMap[String, AnyRef] =
    Map.from(first +: others).asJava.asInstanceOf[JavaMap[String, AnyRef]]

object OllamaChatbot:
  def resource(
      apiClient: OllamaApiClient,
      config: OllamaConfig,
      verbose: Boolean = false,
  ): Resource[IO, OllamaChatbot] = Resource.eval(for
    _ <- apiClient.pullModel *> apiClient.loadModel
    // model
    chatModel = OllamaChatModel
      .builder()
      .baseUrl(config.baseUrl)
      .logRequests(verbose)
      .logResponses(verbose)
      .modelName(config.model.name)
      .temperature(0.3d)
      .build()
    // retrieval
    documentPath <- FilesystemClient.pathTo(systemResource)
    (embeddingModel, embeddingStore) <- IO.delay {
      val document = FileSystemDocumentLoader.loadDocument(documentPath, TextDocumentParser())
      val splitter = DocumentSplitters.recursive(100, 0 /* , tokenizer: TODO */ )
      val segments = splitter.split(document)
      val embeddingModel = AllMiniLmL6V2EmbeddingModel()
      val embeddings = embeddingModel.embedAll(segments).content()
      val embeddingStore = InMemoryEmbeddingStore[TextSegment]()
      embeddingStore.addAll(embeddings, segments)
      embeddingModel -> embeddingStore
    }
    // agent
    assistant = AiServices
      .builder(classOf[Assistant])
      .chatLanguageModel(chatModel)
      .chatMemoryProvider((memoryId: Any) =>
        MessageWindowChatMemory.builder().id(memoryId).maxMessages(40).build(),
      )
      .contentRetriever(
        EmbeddingStoreContentRetriever.from(InMemoryEmbeddingStore[TextSegment]()),
      )
      .build()
  yield OllamaChatbot(assistant, chatModel, embeddingModel, embeddingStore))
  end resource

  private trait Assistant:
    def chat(@MemoryId sessionId: String, @UserMessage message: String): String

  final private case class ActionAiResponse(accountCommand: AccountCommand)

  private object ActionAiResponse:
    given Decoder[ActionAiResponse] = (cursor: HCursor) =>
      cursor.downField("action").as[AccountCommand].map(ActionAiResponse.apply)

  final private case class CreateAccountAiResponse(
      country: Option[Country],
      currency: Option[Currency],
  )

  private object CreateAccountAiResponse:
    given Decoder[CreateAccountAiResponse] = (cursor: HCursor) =>
      (
        cursor.downField("country").as[Option[Country]],
        cursor.downField("currency").as[Option[Currency]],
      ).mapN(CreateAccountAiResponse.apply)

    val empty: CreateAccountAiResponse = CreateAccountAiResponse(None, None)

  private lazy val systemResource = "accounts.txt"

  sealed abstract class ChatbotError(message: String) extends HandledError(message)

  object ChatbotError:
    case object UnsupportedCommand extends ChatbotError("Unsupported command")

    final case class UnmetRequirements(requirements: NonEmptyChain[Requirement])
        extends ChatbotError("Unmet requirements")
