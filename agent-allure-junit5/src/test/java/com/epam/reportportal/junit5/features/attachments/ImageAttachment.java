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

package com.epam.reportportal.junit5.features.attachments;

import com.epam.reportportal.utils.files.Utils;
import io.qameta.allure.Attachment;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.util.Optional.ofNullable;

public class ImageAttachment {

	@Attachment
	public byte[] attach() throws IOException {
		return Utils.readInputStreamToBytes(ofNullable(getClass().getClassLoader()
				.getResourceAsStream("pug/lucky.jpg")).orElseThrow(() -> new IllegalStateException("Image file not found")));
	}

	@Test
	public void stepTest1() throws IOException {
		attach();
	}

}
