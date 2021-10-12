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

package com.epam.reportportal.testng.features.attachments;

import com.epam.reportportal.utils.files.Utils;
import io.qameta.allure.Allure;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

public class ByteDirectAsyncAttachmentTyped {
	public static final String ATTACHMENT_NAME = "Lucky Pug";

	@Test
	public void stepTest1() throws IOException {
		Allure.link("https://example.com"); // allure null-pointer bug workaround
		Supplier<byte[]> attachmentSupplier = () -> {
			try {
				return ofNullable(Utils.readInputStreamToBytes(Objects.requireNonNull(getClass().getClassLoader()
						.getResourceAsStream("pug/lucky.jpg")))).orElseThrow(() -> new IllegalStateException("Image file not found"));
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		};
		Allure.addByteAttachmentAsync(ATTACHMENT_NAME, "image/jpeg", attachmentSupplier);
	}
}
