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

import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.testng.util.TestNgListener;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testng.TestNG;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.reportportal.testng.util.TestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class MutedAnnotationTest {

	private final String suitedUuid = namedUuid("suite_");
	private final String testClassUuid = namedUuid("class_");
	private final List<String> stepUuids = Stream.generate(() -> namedUuid("step_")).limit(3).collect(Collectors.toList());

	private final ReportPortalClient client = mock(ReportPortalClient.class);

	@BeforeEach
	public void initMocks() {
		mockLaunch(client, namedUuid("launchUuid"), suitedUuid, testClassUuid, stepUuids);
		mockLogging(client);
		ReportPortal reportPortal = ReportPortal.create(client, standardParameters());
		TestNgListener.REPORT_PORTAL_THREAD_LOCAL.set(reportPortal);
	}

	@Test
	public void test_muted_annotation_method_level_processing() {
		TestNG result = runTests("muted_method_tests.xml");
		assertThat(result.getStatus(), equalTo(0));

		verify(client).startLaunch(any()); // Start launch
		verify(client).startTestItem(any());  // Start parent suites
		verify(client).startTestItem(same(suitedUuid), any()); // Start test class
		ArgumentCaptor<StartTestItemRQ> startMethodCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client, times(2)).startTestItem(same(testClassUuid), startMethodCapture.capture()); // Start test step

		List<StartTestItemRQ> startMethods = startMethodCapture.getAllValues()
				.stream()
				.filter(rq -> rq.getName().contains("muted"))
				.collect(Collectors.toList());
		assertThat(startMethods, hasSize(1));
		assertThat(startMethods.get(0).isHasStats(), equalTo(Boolean.FALSE));
	}

	@Test
	public void test_muted_annotation_class_level_processing() {
		TestNG result = runTests("muted_class_tests.xml");
		assertThat(result.getStatus(), equalTo(0));

		verify(client).startLaunch(any()); // Start launch
		verify(client).startTestItem(any());  // Start parent suites
		verify(client).startTestItem(same(suitedUuid), any()); // Start test class
		ArgumentCaptor<StartTestItemRQ> startMethodCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client, times(2)).startTestItem(same(testClassUuid), startMethodCapture.capture()); // Start test step

		List<StartTestItemRQ> startMethods = startMethodCapture.getAllValues()
				.stream()
				.filter(rq -> rq.getName().contains("muted"))
				.collect(Collectors.toList());
		assertThat(startMethods, hasSize(1));
		assertThat(startMethods.get(0).isHasStats(), equalTo(Boolean.FALSE));
	}
}
