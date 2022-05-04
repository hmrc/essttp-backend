/*
 * Copyright 2022 HM Revenue & Customs
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

import com.google.inject.{AbstractModule, Provides}
import essttp.journey.model.JourneyId
import journey.JourneyIdGenerator
import org.scalatest.freespec.{AnyFreeSpecLike, FixtureAnyFreeSpecLike}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FixtureTestSuite, TestData, fixture}
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.{Application, Mode}
import play.api.inject.Injector
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.{DefaultTestServerFactory, RunningServer}
import play.core.server.ServerConfig
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, LocalDateTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton
import scala.util.Random

trait ItSpec
  extends AnyFreeSpecLike
  with RichMatchers
  with GuiceOneServerPerTest { self =>

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout  = scaled(Span(20, Seconds)),
    interval = scaled(Span(300, Millis))
  )

  lazy val frozenZonedDateTime: ZonedDateTime = {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    //the frozen time has to be in future otherwise the journeys will dissapear from mongodb because of expiry index
    LocalDateTime.parse("2057-11-02T16:28:55.185", formatter).atZone(ZoneId.of("Europe/London"))
  }

  val clock: Clock = Clock.fixed(frozenZonedDateTime.toInstant, ZoneId.of("UTC"))

  lazy val overridingsModule: AbstractModule = new AbstractModule {
    override def configure(): Unit = ()

    @Provides
    @Singleton
    def clock: Clock = self.clock

    /**
     * This one is randomised every time new test application is spawned. Thanks to that there will be no
     * collisions in database when 2 tests insert journey.
     */
    @Provides
    @Singleton
    def journeyIdGenerator(testJourneyIdGenerator: TestJourneyIdGenerator): JourneyIdGenerator = testJourneyIdGenerator

    @Provides
    @Singleton
    def testJourneyIdGenerator(): TestJourneyIdGenerator = {
      val randomPart: String = Random.alphanumeric.take(5).mkString
      val journeyIdPrefix =TestJourneyIdPrefix(s"TestJourneyId-$randomPart-")
      new TestJourneyIdGenerator(journeyIdPrefix)
    }
  }

  def journeyIdGenerator: TestJourneyIdGenerator = app.injector.instanceOf[TestJourneyIdGenerator]

  implicit def hc: HeaderCarrier = HeaderCarrier()

  val testServerPort = 19001
  val baseUrl: String = s"http://localhost:$testServerPort"
  val databaseName: String = "essttp-backend-it"

  def conf: Map[String, Any] = Map(
    "mongodb.uri" -> s"mongodb://localhost:27017/$databaseName",
    "microservice.services.essttp-backend.protocol" -> "http",
    "microservice.services.essttp-backend.host" -> "localhost",
    "microservice.services.essttp-backend.port" -> testServerPort,
  )

  override def newAppForTest(testData: org.scalatest.TestData): Application = new GuiceApplicationBuilder()
    .configure(conf)
    .overrides(GuiceableModule.fromGuiceModules(Seq(overridingsModule)))
    .build()

  override protected def newServerForTest(app: Application, testData: TestData): RunningServer =
    TestServerFactory.start(app)

  object TestServerFactory extends DefaultTestServerFactory {
    override protected def serverConfig(app: Application): ServerConfig = {
      val sc = ServerConfig(port    = Some(testServerPort), sslPort = Some(0), mode = Mode.Test, rootDir = app.path)
      sc.copy(configuration = sc.configuration.withFallback(overrideServerConfiguration(app)))
    }
  }


}

final case class TestJourneyIdPrefix(value: String)

class TestJourneyIdGenerator (testJourneyIdPrefix: TestJourneyIdPrefix) extends JourneyIdGenerator {

  private val idIterator: Iterator[JourneyId] = Stream.from(0).map(i => JourneyId(s"${testJourneyIdPrefix.value}$i")).iterator
  private val nextJourneyIdCached = new AtomicReference[JourneyId](idIterator.next())

  def readNextJourneyId(): JourneyId = nextJourneyIdCached.get()

  override def nextJourneyId(): JourneyId = {
    nextJourneyIdCached.getAndSet(idIterator.next())
  }
}