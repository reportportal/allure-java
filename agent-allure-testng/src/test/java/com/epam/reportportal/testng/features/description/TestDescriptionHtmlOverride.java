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

package com.epam.reportportal.testng.features.description;

import io.qameta.allure.Allure;
import org.testng.annotations.Test;

public class TestDescriptionHtmlOverride {
	public static final String DESCRIPTION = "My test description";
	public static final String DESCRIPTION_HTML = "My test description HTML";

	@Test
	public void simpleTest1() {
		Allure.descriptionHtml(DESCRIPTION);
		Allure.descriptionHtml(DESCRIPTION_HTML);
	}
}
