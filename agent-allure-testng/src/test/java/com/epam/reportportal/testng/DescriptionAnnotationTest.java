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

import com.epam.reportportal.allure.AnnotationUtils;
import com.epam.reportportal.listeners.ItemType;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.testng.features.description.TestConfigurationDescription;
import com.epam.reportportal.testng.features.description.TestDescriptionAnnotation;
import com.epam.reportportal.testng.features.description.TestTwoDescription;
import com.epam.reportportal.testng.util.TestNgListener;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.reportportal.testng.util.TestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class DescriptionAnnotationTest {

	private final String suitedUuid = namedUuid("suite_");
	private final String testClassUuid = namedUuid("class_");
	private final List<String> stepUuids = Stream.generate(() -> namedUuid("step_")).limit(3).collect(Collectors.toList());

	private final ReportPortalClient client = mock(ReportPortalClient.class);

	@BeforeEach
	public void initMocks() {
		mockLaunch(client, namedUuid("launchUuid"), suitedUuid, testClassUuid, stepUuids);
		ReportPortal reportPortal = ReportPortal.create(client, standardParameters());
		TestNgListener.REPORT_PORTAL_THREAD_LOCAL.set(reportPortal);
	}

	@Test
	public void test_description_annotation_processing() {
		runTests(Collections.singletonList(TestNgListener.class), TestDescriptionAnnotation.class);

		verify(client).startLaunch(any()); // Start launch
		verify(client).startTestItem(any());  // Start parent suites
		verify(client).startTestItem(same(suitedUuid), any()); // Start test class
		ArgumentCaptor<StartTestItemRQ> startMethodCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(same(testClassUuid), startMethodCapture.capture()); // Start test step

		StartTestItemRQ startMethod = startMethodCapture.getValue();
		assertThat(startMethod.getDescription(), equalTo("My test description"));
	}

	@Test
	public void test_description_conflict_processing() {
		runTests(Collections.singletonList(TestNgListener.class), TestTwoDescription.class);

		verify(client).startLaunch(any()); // Start launch
		verify(client).startTestItem(any());  // Start parent suites
		verify(client).startTestItem(same(suitedUuid), any()); // Start test class
		ArgumentCaptor<StartTestItemRQ> startMethodCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(same(testClassUuid), startMethodCapture.capture()); // Start test step

		StartTestItemRQ startMethod = startMethodCapture.getValue();
		assertThat(
				startMethod.getDescription(),
				equalTo("My test description" + AnnotationUtils.MARKDOWN_DELIMITER + "My description description")
		);
	}

	@Test
	public void test_description_annotation_processing_configuration() {
		runTests(Collections.singletonList(TestNgListener.class), TestConfigurationDescription.class);

		verify(client).startLaunch(any()); // Start launch
		verify(client).startTestItem(any());  // Start parent suites
		verify(client).startTestItem(same(suitedUuid), any()); // Start test class
		ArgumentCaptor<StartTestItemRQ> startMethodCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client, times(3)).startTestItem(same(testClassUuid), startMethodCapture.capture()); // Start test step

		List<StartTestItemRQ> startMethods = startMethodCapture.getAllValues();
		StartTestItemRQ before = startMethods.stream()
				.filter(s -> ItemType.BEFORE_METHOD.name().equals(s.getType()))
				.findAny()
				.orElse(null);
		assertThat(before, notNullValue());
		assertThat(before.getDescription(), equalTo("My before method description"));

		StartTestItemRQ after = startMethods.stream().filter(s -> ItemType.AFTER_METHOD.name().equals(s.getType())).findAny().orElse(null);
		assertThat(after, notNullValue());
		assertThat(after.getDescription(), equalTo("My after method description"));
	}
}
