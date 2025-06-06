/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testsupport

import bars.BarsVerifyStatusRepo
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.google.inject.{AbstractModule, Provides}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.{CorrelationId, Journey, JourneyId}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.SingleObservableFuture
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.{AnyContent, Request}
import play.api.test.{DefaultTestServerFactory, FakeRequest, TestServerFactory}
import play.api.{Application, Mode}
import play.core.server.ServerConfig
import repository.JourneyRepo
import services.{CorrelationIdGenerator, JourneyIdGenerator, PegaCorrelationIdGenerator}
import testsupport.stubs.{AuditConnectorStub, AuthStub}
import testsupport.testdata.TdAll
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{AuthProviders, Enrolments}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.{AesCrypto, Decrypter, Encrypter, PlainText}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Clock
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton
import scala.util.Random

trait ItSpec extends AnyFreeSpecLike, RichMatchers, GuiceOneServerPerSuite, WireMockSupport { self =>

  given testCrypto: (Encrypter & Decrypter) = new AesCrypto {
    override protected val encryptionKey: String = "P5xsJ9Nt+quxGZzB4DeLfw=="
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(Span(20, Seconds)),
    interval = scaled(Span(300, Millis))
  )

  def overrideClock: Option[Clock] = None

  lazy val overridingsModule: AbstractModule = new AbstractModule {
    override def configure(): Unit = ()

    @Provides
    @Singleton
    def operationalCryptoFormat: OperationalCryptoFormat = OperationalCryptoFormat(testCrypto)

    @Provides
    @Singleton
    def clock: Clock =
      overrideClock.getOrElse {
        FrozenTime.reset()
        FrozenTime.getClock
      }

    /** This one is randomised every time new test application is spawned. Thanks to that there will be no collisions in
      * database when 2 tests insert journey.
      */
    @Provides
    @Singleton
    def journeyIdGenerator(testJourneyIdGenerator: TestJourneyIdGenerator): JourneyIdGenerator = testJourneyIdGenerator

    @Provides
    @Singleton
    def testJourneyIdGenerator(): TestJourneyIdGenerator = {
      val randomPart: String                   = Random.alphanumeric.take(5).mkString
      val journeyIdPrefix: TestJourneyIdPrefix = TestJourneyIdPrefix(s"TestJourneyId-$randomPart-")
      new TestJourneyIdGenerator(journeyIdPrefix)
    }

    @Provides
    @Singleton
    def testCorrelationIdGenerator(testCorrelationIdGenerator: TestCorrelationIdGenerator): CorrelationIdGenerator =
      testCorrelationIdGenerator

    @Provides
    @Singleton
    def testCorrelationIdGenerator(): TestCorrelationIdGenerator = {
      val randomPart: String                           = UUID.randomUUID().toString.take(8)
      val correlationIdPrefix: TestCorrelationIdPrefix = TestCorrelationIdPrefix(
        s"$randomPart-843f-4988-89c6-d4d3e2e91e26"
      )
      new TestCorrelationIdGenerator(correlationIdPrefix)
    }

    @Provides
    @Singleton
    def testPegaCorrelationIdGenerator(): PegaCorrelationIdGenerator = pegaCorrelationIdGenerator
  }

  lazy val overrideBindings: Seq[GuiceableModule] = Seq.empty

  def journeyIdGenerator: TestJourneyIdGenerator                 = app.injector.instanceOf[TestJourneyIdGenerator]
  def correlationIdGenerator: TestCorrelationIdGenerator         = app.injector.instanceOf[TestCorrelationIdGenerator]
  val pegaCorrelationIdGenerator: TestPegaCorrelationIdGenerator = new TestPegaCorrelationIdGenerator

  given hc: HeaderCarrier = HeaderCarrier()

  val baseUrl: String      = s"http://localhost:${ItSpec.testServerPort.toString}"
  val databaseName: String = "essttp-backend-it"

  val overrideConfig: Map[String, Any] = Map.empty

  def conf: Map[String, Any] = Map[String, Any](
    "mongodb.uri"                                   -> s"mongodb://localhost:27017/$databaseName",
    "microservice.services.essttp-backend.protocol" -> "http",
    "microservice.services.essttp-backend.host"     -> "localhost",
    "microservice.services.essttp-backend.port"     -> ItSpec.testServerPort,
    "microservice.services.auth.port"               -> WireMockSupport.port,
    "microservice.services.date-calculator.port"    -> WireMockSupport.port,
    "microservice.services.pega.port"               -> WireMockSupport.port,
    "logger.root"                                   -> "INFO",
    "logger.application"                            -> "INFO",
    "logger.connector"                              -> "INFO"
  ) ++ overrideConfig

  // in tests use `app`
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(conf)
    .overrides(GuiceableModule.fromGuiceModules(Seq(overridingsModule)))
    .overrides(overrideBindings*)
    .build()

  object CustomTestServerFactory extends DefaultTestServerFactory {
    override protected def serverConfig(app: Application): ServerConfig = {
      val sc = ServerConfig(port = Some(ItSpec.testServerPort), sslPort = None, mode = Mode.Test, rootDir = app.path)
      sc.copy(configuration = sc.configuration.withFallback(overrideServerConfiguration(app)))
    }
  }

  override protected def testServerFactory: TestServerFactory = CustomTestServerFactory // Override the factory

  trait JourneyItTest {
    val tdAll: TdAll                   = new TdAll {
      override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
      override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
    }
    given request: Request[AnyContent] = tdAll.request.withHeaders("Authorization" -> TdAll.authorization.value)

    lazy val journeyRepo: JourneyRepo = app.injector.instanceOf[JourneyRepo]

    def insertJourneyForTest(journey: Journey): Unit = journeyRepo.upsert(journey).futureValue
  }

  trait BarsVerifyStatusItTest {
    import testsupport.TdSupport._

    given Request[?] = FakeRequest()
      .withSessionId()
      .withAuthToken()
      .withAkamaiReputationHeader()
      .withRequestId()
      .withTrueClientIp()
      .withTrueClientPort()
      .withDeviceId()

    private def barsRepo: BarsVerifyStatusRepo = app.injector.instanceOf[BarsVerifyStatusRepo]
    barsRepo.collection.deleteMany(BsonDocument("{}")).toFuture().futureValue
  }

  def stubCommonActions(enrolments: Enrolments = Enrolments(Set.empty)): StubMapping = {
    AuditConnectorStub.audit()
    AuthStub.authorise(enrolments)
  }

  def verifyCommonActions(numberOfAuthCalls: Int): Unit =
    AuthStub.ensureAuthoriseCalled(numberOfAuthCalls, AuthProviders(GovernmentGateway))

  def encryptString(s: String, encrypter: Encrypter): String =
    encrypter
      .encrypt(
        PlainText("\"" + SensitiveString(s).decryptedValue + "\"")
      )
      .value
}

object ItSpec {

  val testServerPort: Int = 19001

}

final case class TestJourneyIdPrefix(value: String)

class TestJourneyIdGenerator(testJourneyIdPrefix: TestJourneyIdPrefix) extends JourneyIdGenerator {

  private val idIterator: Iterator[JourneyId] =
    LazyList.from(0).map(i => JourneyId(s"${testJourneyIdPrefix.value}${i.toString}")).iterator
  private val nextJourneyIdCached             = new AtomicReference[JourneyId](idIterator.next())

  def readNextJourneyId(): JourneyId = nextJourneyIdCached.get()

  override def nextJourneyId(): JourneyId =
    nextJourneyIdCached.getAndSet(idIterator.next())
}

final case class TestCorrelationIdPrefix(value: String)

class TestCorrelationIdGenerator(testCorrelationIdPrefix: TestCorrelationIdPrefix) extends CorrelationIdGenerator {
  private val correlationIdIterator: Iterator[CorrelationId] =
    LazyList
      .from(0)
      .map(i => CorrelationId(UUID.fromString(s"${testCorrelationIdPrefix.value.dropRight(1)}${i.toString}".take(35))))
      .iterator
  private val nextCorrelationIdCached                        = new AtomicReference[CorrelationId](correlationIdIterator.next())

  def readNextCorrelationId(): CorrelationId = nextCorrelationIdCached.get()

  override def nextCorrelationId(): CorrelationId =
    nextCorrelationIdCached.getAndSet(correlationIdIterator.next())
}

class TestPegaCorrelationIdGenerator extends PegaCorrelationIdGenerator {

  val fixedCorrelationId: String = UUID.randomUUID().toString

  override def nextCorrelationId(): String = fixedCorrelationId

}
