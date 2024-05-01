/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import essttp.journey.JourneyConnector
import essttp.journey.model.{Journey, JourneyId}
import testsupport.ItSpec

import scala.concurrent.Future

trait UpdateJourneyControllerSpec { this: ItSpec =>

  lazy val journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  def testUpdateWithoutExistingValue[R](
      initialJourney: Journey,
      newValue:       R
  )(
      doUpdate:           (JourneyId, R) => Future[Journey],
      expectedNewJourney: Journey
  )(context: JourneyItTest): Unit = {
    import context.request

    val journeyId = initialJourney.journeyId

    stubCommonActions()

    context.insertJourneyForTest(initialJourney)

    val result1 = doUpdate(journeyId, newValue).futureValue

    result1 shouldBe expectedNewJourney
    journeyConnector.getJourney(journeyId).futureValue shouldBe expectedNewJourney

    // test an update with the same value doesn't change the journey we just put in
    val result2 = doUpdate(journeyId, newValue).futureValue
    result2 shouldBe expectedNewJourney
    journeyConnector.getJourney(journeyId).futureValue shouldBe expectedNewJourney

    verifyCommonActions(numberOfAuthCalls = 4)
    ()
  }

  def testUpdateWithExistingValue[J <: Journey, R](initialJourney: J)(
      existingJourneyId: J => JourneyId,
      existingValue:     R
  )(
      differentValue:     R,
      doUpdate:           (JourneyId, R) => Future[Journey],
      expectedNewJourney: Journey
  )(context: JourneyItTest): Unit = {
    import context.request

    val journeyId = existingJourneyId(initialJourney)

    stubCommonActions()

    context.insertJourneyForTest(initialJourney)

    // test updating with the same value doesn't change the initial journey
    val result1 = doUpdate(journeyId, existingValue).futureValue
    result1 shouldBe initialJourney
    journeyConnector.getJourney(journeyId).futureValue shouldBe initialJourney

    // test updating with a different value updates the initial journey
    val result2 = doUpdate(journeyId, differentValue).futureValue
    result2 shouldBe expectedNewJourney
    journeyConnector.getJourney(journeyId).futureValue shouldBe expectedNewJourney

    verifyCommonActions(numberOfAuthCalls = 4)
    ()
  }

}
