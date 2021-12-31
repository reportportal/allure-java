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

package com.epam.reportportal.testng;

import com.epam.reportportal.listeners.LogLevel;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.testng.features.attachments.*;
import com.epam.reportportal.testng.util.TestNgListener;
import com.epam.reportportal.util.test.CommonUtils;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import okhttp3.MultipartBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testng.TestNG;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.epam.reportportal.testng.util.TestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

public class AttachmentsTest {
	private final String launchUuid = namedUuid("launchUuid_");
	private final String suitedUuid = namedUuid("suite_");
	private final String testClassUuid = namedUuid("class_");
	private final String stepUuid = namedUuid("test_");

	private final ReportPortalClient client = mock(ReportPortalClient.class);
	private final ExecutorService executor = CommonUtils.testExecutor();

	@BeforeEach
	public void initMocks() {
		mockLaunch(client, launchUuid, suitedUuid, testClassUuid, stepUuid);
		mockLogging(client);
		ReportPortal reportPortal = ReportPortal.create(client, standardParameters(), executor);
		TestNgListener.REPORT_PORTAL_THREAD_LOCAL.set(reportPortal);
	}

	@AfterEach
	public void tearDown() {
		CommonUtils.shutdownExecutorService(executor);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_annotation_attachment() {
		TestNG result = runTests(Collections.singletonList(TestNgListener.class), ImageAttachment.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<List<MultipartBody.Part>> logCaptor = ArgumentCaptor.forClass(List.class);
		verify(client, timeout(1000).atLeast(1)).log(logCaptor.capture());

		SaveLogRQ log = verifyLogged(logCaptor, stepUuid, LogLevel.UNKNOWN, "attach");
		assertThat(log.getLaunchUuid(), equalTo(launchUuid));
		assertThat(log.getFile().getName(), notNullValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_annotation_named_attachment() {
		TestNG result = runTests(Collections.singletonList(TestNgListener.class), ImageNamedAttachment.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<List<MultipartBody.Part>> logCaptor = ArgumentCaptor.forClass(List.class);
		verify(client, timeout(1000).atLeast(1)).log(logCaptor.capture());

		SaveLogRQ log = verifyLogged(logCaptor, stepUuid, LogLevel.UNKNOWN, ImageNamedAttachment.ATTACHMENT_NAME);
		assertThat(log.getLaunchUuid(), equalTo(launchUuid));
		assertThat(log.getFile().getName(), notNullValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_direct_named_attachment() {
		TestNG result = runTests(Collections.singletonList(TestNgListener.class), ImageDirectAttachment.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<List<MultipartBody.Part>> logCaptor = ArgumentCaptor.forClass(List.class);
		verify(client, timeout(1000).atLeast(1)).log(logCaptor.capture());

		SaveLogRQ log = verifyLogged(logCaptor, stepUuid, LogLevel.UNKNOWN, ImageDirectAttachment.ATTACHMENT_NAME);
		assertThat(log.getLaunchUuid(), equalTo(launchUuid));
		assertThat(log.getFile().getName(), notNullValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_direct_named_text_attachment() {
		TestNG result = runTests(Collections.singletonList(TestNgListener.class), TextDirectAttachment.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<List<MultipartBody.Part>> logCaptor = ArgumentCaptor.forClass(List.class);
		verify(client, timeout(1000).atLeast(1)).log(logCaptor.capture());

		SaveLogRQ log = verifyLogged(logCaptor, stepUuid, LogLevel.UNKNOWN, TextDirectAttachment.ATTACHMENT_NAME);
		assertThat(log.getLaunchUuid(), equalTo(launchUuid));
		assertThat(log.getFile().getName(), notNullValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_direct_named_typed_text_attachment() {
		TestNG result = runTests(Collections.singletonList(TestNgListener.class), TextDirectAttachmentTyped.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<List<MultipartBody.Part>> logCaptor = ArgumentCaptor.forClass(List.class);
		verify(client, timeout(1000).atLeast(1)).log(logCaptor.capture());

		SaveLogRQ log = verifyLogged(logCaptor, stepUuid, LogLevel.UNKNOWN, TextDirectAttachmentTyped.ATTACHMENT_NAME);
		assertThat(log.getLaunchUuid(), equalTo(launchUuid));
		assertThat(log.getFile().getName(), notNullValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_direct_named_typed_extension_text_attachment() {
		TestNG result = runTests(Collections.singletonList(TestNgListener.class), TextDirectAttachmentTypedExt.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<List<MultipartBody.Part>> logCaptor = ArgumentCaptor.forClass(List.class);
		verify(client, timeout(1000).atLeast(1)).log(logCaptor.capture());

		SaveLogRQ log = verifyLogged(logCaptor, stepUuid, LogLevel.UNKNOWN, TextDirectAttachmentTypedExt.ATTACHMENT_NAME);
		assertThat(log.getLaunchUuid(), equalTo(launchUuid));
		assertThat(log.getFile().getName(), notNullValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_direct_named_typed_extension_image_attachment() {
		TestNG result = runTests(Collections.singletonList(TestNgListener.class), ImageDirectAttachmentTypedExt.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<List<MultipartBody.Part>> logCaptor = ArgumentCaptor.forClass(List.class);
		verify(client, timeout(1000).atLeast(1)).log(logCaptor.capture());

		SaveLogRQ log = verifyLogged(logCaptor, stepUuid, LogLevel.UNKNOWN, ImageDirectAttachmentTypedExt.ATTACHMENT_NAME);
		assertThat(log.getLaunchUuid(), equalTo(launchUuid));
		assertThat(log.getFile().getName(), notNullValue());
	}
}
