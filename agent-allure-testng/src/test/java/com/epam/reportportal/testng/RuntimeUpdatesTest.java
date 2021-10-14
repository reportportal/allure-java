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
import com.epam.reportportal.listeners.ItemStatus;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.testng.features.attributes.TestAttributeAdd;
import com.epam.reportportal.testng.features.description.*;
import com.epam.reportportal.testng.features.links.TestLinkAddDescription;
import com.epam.reportportal.testng.features.links.TestLinkAddNoDescription;
import com.epam.reportportal.testng.features.links.TestLinkAddNoName;
import com.epam.reportportal.testng.util.TestNgListener;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testng.TestNG;

import static com.epam.reportportal.testng.util.TestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RuntimeUpdatesTest {
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
	public void test_no_updates_produces_no_additional_data() {
		TestNG result = runTests(TestNoUpdatesDescription.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<FinishTestItemRQ> finishCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(stepUuid), finishCaptor.capture());

		FinishTestItemRQ finishItemRequest = finishCaptor.getValue();
		assertThat(finishItemRequest.getDescription(), nullValue());
		assertThat(finishItemRequest.getAttributes(), nullValue());
		assertThat(finishItemRequest.getStatus(), equalTo(ItemStatus.PASSED.name()));
	}

	@Test
	public void test_attribute_add() {
		mockLogging(client);
		TestNG result = runTests(TestAttributeAdd.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<FinishTestItemRQ> finishCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(stepUuid), finishCaptor.capture());

		FinishTestItemRQ finishItemRequest = finishCaptor.getValue();
		assertThat(finishItemRequest.getAttributes(), hasSize(1));
		ItemAttributesRQ attribute = finishItemRequest.getAttributes().iterator().next();
		assertThat(attribute.getKey(), equalTo("epic"));
		assertThat(attribute.getValue(), equalTo(TestAttributeAdd.EPIC));
	}

	@Test
	public void test_description_add() {
		mockLogging(client);
		TestNG result = runTests(TestDescriptionAdd.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<FinishTestItemRQ> finishCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(stepUuid), finishCaptor.capture());

		FinishTestItemRQ finishItemRequest = finishCaptor.getValue();
		assertThat(finishItemRequest.getDescription(), equalTo(TestDescriptionAdd.DESCRIPTION));
	}

	@Test
	public void test_description_html_add() {
		mockLogging(client);
		TestNG result = runTests(TestDescriptionHtmlAdd.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<FinishTestItemRQ> finishCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(stepUuid), finishCaptor.capture());

		FinishTestItemRQ finishItemRequest = finishCaptor.getValue();
		assertThat(finishItemRequest.getDescription(), equalTo(TestDescriptionHtmlAdd.DESCRIPTION));
	}

	@Test
	public void test_description_html_override() {
		mockLogging(client);
		TestNG result = runTests(TestDescriptionHtmlOverride.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<FinishTestItemRQ> finishCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(stepUuid), finishCaptor.capture());

		FinishTestItemRQ finishItemRequest = finishCaptor.getValue();
		assertThat(finishItemRequest.getDescription(), equalTo(TestDescriptionHtmlOverride.DESCRIPTION_HTML));
	}

	@Test
	public void test_description_override() {
		mockLogging(client);
		TestNG result = runTests(TestDescriptionOverride.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<FinishTestItemRQ> finishCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(stepUuid), finishCaptor.capture());

		FinishTestItemRQ finishItemRequest = finishCaptor.getValue();
		assertThat(finishItemRequest.getDescription(), equalTo(TestDescriptionOverride.DESCRIPTION));
	}

	@Test
	public void test_link_with_annotation_description_set() {
		mockLogging(client);
		TestNG result = runTests(TestLinkAddDescription.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<FinishTestItemRQ> finishCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(stepUuid), finishCaptor.capture());

		FinishTestItemRQ finishItemRequest = finishCaptor.getValue();
		assertThat(finishItemRequest.getDescription(),
				equalTo(TestLinkAddDescription.DESCRIPTION + FormatUtils.MARKDOWN_DELIMITER + FormatUtils.LINK_PREFIX + String.format(
						FormatUtils.LINK_MARKDOWN,
						TestLinkAddDescription.LINK_NAME,
						TestLinkAddDescription.LINK_URL
				))
		);
	}

	@Test
	public void test_link_with_no_description_set() {
		mockLogging(client);
		TestNG result = runTests(TestLinkAddNoDescription.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<FinishTestItemRQ> finishCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(stepUuid), finishCaptor.capture());

		FinishTestItemRQ finishItemRequest = finishCaptor.getValue();
		assertThat(finishItemRequest.getDescription(),
				equalTo(FormatUtils.LINK_PREFIX + String.format(
						FormatUtils.LINK_MARKDOWN,
						TestLinkAddNoDescription.LINK_NAME,
						TestLinkAddNoDescription.LINK_URL
				))
		);
	}

	@Test
	public void test_link_with_no_name_set() {
		mockLogging(client);
		TestNG result = runTests(TestLinkAddNoName.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<FinishTestItemRQ> finishCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(stepUuid), finishCaptor.capture());

		FinishTestItemRQ finishItemRequest = finishCaptor.getValue();
		assertThat(finishItemRequest.getDescription(),
				equalTo(FormatUtils.LINK_PREFIX + String.format(
						FormatUtils.LINK_MARKDOWN,
						TestLinkAddNoName.LINK_URL,
						TestLinkAddNoName.LINK_URL
				))
		);
	}
}
