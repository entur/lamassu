/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Clock;
import java.time.Instant;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class CacheUtilTest {

    private MockedStatic<Clock> clockMock;

    // 2021-12-20T11:33:20Z
    private final int now = 1640000000;

    @BeforeEach
    public void setup() {
        mockInstant(now);
    }

    @AfterEach
    public void destroy() {
        clockMock.close();
    }

    private void mockInstant(long expected) {
        Clock spyClock = spy(Clock.class);
        clockMock = mockStatic(Clock.class);
        clockMock.when(Clock::systemUTC).thenReturn(spyClock);
        when(spyClock.instant()).thenReturn(Instant.ofEpochSecond(expected));
    }

    @Test
    void getTtlReturnsMinimumTtlWhenExpired() {
        int expected = 3600;
        Assertions.assertEquals(expected, CacheUtil.getTtl(now - 3600, 10, expected));
    }

    @Test
    void getTtlReturnsMinimumTtlWhenLessThanMinimumTtl() {
        int expected = 3600;
        Assertions.assertEquals(expected, CacheUtil.getTtl(now - 20, 10, expected));
    }

    @Test
    void getTtlReturnsCalculatedTtlWhenLargerThanMinimumTtl() {
        Assertions.assertEquals(20, CacheUtil.getTtl(now - 10, 30, 10));
    }
}
