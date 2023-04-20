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

import java.time._
import java.time.format.DateTimeFormatter

/**
 * A time machine which allows to travel back and forth in time.
 */

object FrozenTime {

  def fixedClockUTC(fixedAtDate: LocalDate): Clock = Clock.fixed(fixedAtDate.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneId.of("Z"))

  def fixedClockUTC(fixedAtDateTime: LocalDateTime): Clock = Clock.fixed(fixedAtDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("Z"))

  def setTime(fixedAtDate: LocalDate): Unit = {
    val clock = fixedClockUTC(fixedAtDate)
    currentClock = clock
  }

  def setTime(fixedAtDateTime: LocalDateTime): Unit = {
    val clock = fixedClockUTC(fixedAtDateTime)
    currentClock = clock
  }

  def addSeconds(seconds: Long): Unit = {
    val nowPlusSeconds = LocalDateTime.now(testClock).plusSeconds(seconds)
    setTime(nowPlusSeconds)
  }

  def setTime(fixedAtDate: String): Unit = {
    val clock = fixedClockUTC(LocalDate.parse(fixedAtDate))
    currentClock = clock
  }

  def localDate: LocalDate = LocalDate.now(testClock)

  def getClock: Clock = testClock

  def reset(): Unit = setTime(initialLocalDate)

  private val initialLocalDate = {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    //the frozen time has to be in future otherwise the journeys will disappear from mongodb because of expiry index
    LocalDateTime.parse("2057-11-02T16:28:55.185", formatter)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var currentClock: Clock = fixedClockUTC(initialLocalDate)
  private val testClock: Clock = new Clock {
    override def getZone: ZoneId = currentClock.getZone

    override def withZone(zoneId: ZoneId): Clock = currentClock.withZone(zoneId)

    override def instant(): Instant = currentClock.instant()
  }
}
