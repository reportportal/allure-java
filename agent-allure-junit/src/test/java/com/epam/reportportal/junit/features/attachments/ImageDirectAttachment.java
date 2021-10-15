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

package com.epam.reportportal.junit.features.attachments;

import io.qameta.allure.Allure;
import org.junit.Test;

import static java.util.Optional.ofNullable;

public class ImageDirectAttachment {
	public static final String ATTACHMENT_NAME = "Lucky Pug";

	@Test
	public void stepTest1() {
		Allure.addAttachment(
				ATTACHMENT_NAME,
				ofNullable(getClass().getClassLoader().getResourceAsStream("pug/lucky.jpg")).orElseThrow(() -> new IllegalStateException(
						"Image file not found"))
		);
	}
}
