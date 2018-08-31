/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
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
package io.zeebe.broker.it.startup;

import io.zeebe.broker.Broker;
import io.zeebe.util.sched.clock.ControlledActorClock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BrokerInvalidCfgTest {

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void shouldThrowExceptionWithNegativePartitions() {
    // given
    final String path =
        BrokerInvalidCfgTest.class.getResource("/invalidCfgs/negativePartitions.toml").getPath();

    // expected
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Partition count must not be smaller then 1.");

    // when
    new Broker(path, "", new ControlledActorClock());
  }
}
