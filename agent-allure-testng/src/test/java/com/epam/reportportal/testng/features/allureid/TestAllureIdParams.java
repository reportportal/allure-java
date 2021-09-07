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

package com.epam.reportportal.testng.features.allureid;

import io.qameta.allure.AllureId;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Iterator;

@Feature("My feature")
public class TestAllureIdParams {

	@DataProvider(name = "data")
	public Iterator<Object[]> testData() {
		return Collections.singletonList(new Object[] { "Data step name" }).iterator();
	}

	@Test(dataProvider = "data")
	@Story("My story 1")
	@AllureId("My ID")
	public void simpleTest1(String stepName) {
		System.out.printf("step '%s'%n", stepName);
	}
}
