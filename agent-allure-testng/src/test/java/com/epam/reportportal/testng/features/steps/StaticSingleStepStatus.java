/*
 * Copyright 2021 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.testng.features.steps;

import io.qameta.allure.model.Status;
import org.testng.annotations.Test;

import static io.qameta.allure.Allure.step;

public class StaticSingleStepStatus {
	public static final String TEST_STEP_NAME = StaticSingleStepStatus.class.getSimpleName() + " test step";

	@Test
	public void stepTest1() {
		step(TEST_STEP_NAME, Status.BROKEN);
	}
}
