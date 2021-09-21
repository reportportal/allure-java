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

import com.epam.reportportal.allure.FormatUtils;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.testng.features.TestMyFirstFeature;
import com.epam.reportportal.testng.features.links.TestLinkAndDescription;
import com.epam.reportportal.testng.util.TestNgListener;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.epam.reportportal.testng.util.TestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LinkAnnotationTest {

	private final String suitedUuid = namedUuid("suite_");
	private final String testClassUuid = namedUuid("class_");
	private final String stepUuid = namedUuid("test_");

	private final ReportPortalClient client = mock(ReportPortalClient.class);

	@BeforeEach
	public void initMocks() {
		mockLaunch(client, namedUuid("launchUuid"), suitedUuid, testClassUuid, stepUuid);
		ReportPortal reportPortal = ReportPortal.create(client, standardParameters());
		TestNgListener.REPORT_PORTAL_THREAD_LOCAL.set(reportPortal);
	}

	@Test
	public void test_description_should_contain_attached_links() {
		runTests(Collections.singletonList(TestNgListener.class), TestMyFirstFeature.class);

		verify(client).startLaunch(any()); // Start launch
		verify(client).startTestItem(any());  // Start parent suites
		ArgumentCaptor<StartTestItemRQ> startTestCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(same(suitedUuid), startTestCapture.capture()); // Start test class
		ArgumentCaptor<StartTestItemRQ> startMethodCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(same(testClassUuid), startMethodCapture.capture()); // Start test step

		StartTestItemRQ startItem = startTestCapture.getValue();
		List<StartTestItemRQ> items = Arrays.asList(startItem, startMethodCapture.getValue());

		AtomicInteger counter = new AtomicInteger();
		items.forEach(i -> {
			String description = i.getDescription();
			assertThat(description, allOf(not(emptyOrNullString()), startsWith(FormatUtils.LINK_PREFIX)));
			List<String> links = Arrays.asList(description.substring(
					description.indexOf(FormatUtils.LINK_PREFIX) + FormatUtils.LINK_PREFIX.length()).split("\n"));
			assertThat(links, hasSize(counter.incrementAndGet()));

			links.forEach(l -> assertThat(l, containsString("(https://example.com")));
		});
	}

	@Test
	public void test_description_should_not_override_attached_links() {
		runTests(TestLinkAndDescription.class);

		ArgumentCaptor<StartTestItemRQ> startMethodCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(same(testClassUuid), startMethodCapture.capture()); // Start test step

		StartTestItemRQ startRequest = startMethodCapture.getValue();
		assertThat(startRequest.getDescription(),
				equalTo(TestLinkAndDescription.DESCRIPTION + FormatUtils.MARKDOWN_DELIMITER + FormatUtils.LINK_PREFIX + String.format(
						FormatUtils.LINK_MARKDOWN,
						TestLinkAndDescription.LINK_NAME,
						TestLinkAndDescription.LINK_URL
				))
		);
	}
}
