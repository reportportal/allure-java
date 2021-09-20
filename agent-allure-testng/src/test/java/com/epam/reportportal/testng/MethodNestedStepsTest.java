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

import com.epam.reportportal.listeners.ItemStatus;
import com.epam.reportportal.listeners.LogLevel;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.testng.features.steps.MethodNamedSingleStep;
import com.epam.reportportal.testng.features.steps.MethodSingleStep;
import com.epam.reportportal.testng.features.steps.MethodStepFailure;
import com.epam.reportportal.testng.features.steps.StaticAnonymousStepFailure;
import com.epam.reportportal.testng.util.TestNgListener;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import okhttp3.MultipartBody;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testng.TestNG;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.reportportal.testng.util.TestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class MethodNestedStepsTest {
	private final String suitedUuid = namedUuid("suite_");
	private final String testUuid = namedUuid("test_");
	private final String stepUuid = namedUuid("step_");
	private final List<String> nestedSteps = Stream.generate(() -> namedUuid("nested_")).limit(2).collect(Collectors.toList());
	private final List<Pair<String, String>> nestedStepLinks = nestedSteps.stream()
			.map(n -> Pair.of(stepUuid, n))
			.collect(Collectors.toList());

	private final ReportPortalClient client = mock(ReportPortalClient.class);

	@BeforeEach
	public void initMocks() {
		mockLaunch(client, namedUuid("launchUuid"), suitedUuid, testUuid, stepUuid);
		ReportPortal reportPortal = ReportPortal.create(client, standardParameters());
		mockLogging(client);
		TestNgListener.REPORT_PORTAL_THREAD_LOCAL.set(reportPortal);
	}

	@Test
	public void test_method_single_step() {
		mockNestedSteps(client, nestedStepLinks.get(0));
		TestNG result = runTests(Collections.singletonList(TestNgListener.class), MethodSingleStep.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<StartTestItemRQ> startNestedStepCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(same(stepUuid), startNestedStepCapture.capture());

		StartTestItemRQ startStep = startNestedStepCapture.getValue();
		assertThat(startStep.getName(), equalTo(MethodSingleStep.TEST_STEP_NAME));
		assertThat(startStep.isHasStats(), equalTo(Boolean.FALSE));

		ArgumentCaptor<FinishTestItemRQ> finishNestedStepCapture = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(nestedSteps.get(0)), finishNestedStepCapture.capture());

		FinishTestItemRQ finishStep = finishNestedStepCapture.getValue();
		assertThat(finishStep.getStatus(), equalTo(ItemStatus.PASSED.name()));
	}

	@Test
	public void test_method_named_single_step() {
		mockNestedSteps(client, nestedStepLinks.get(0));
		TestNG result = runTests(Collections.singletonList(TestNgListener.class), MethodNamedSingleStep.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<StartTestItemRQ> startNestedStepCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(same(stepUuid), startNestedStepCapture.capture());

		StartTestItemRQ startStep = startNestedStepCapture.getValue();
		assertThat(startStep.getName(), equalTo(MethodNamedSingleStep.TEST_STEP_NAME));
		assertThat(startStep.isHasStats(), equalTo(Boolean.FALSE));

		ArgumentCaptor<FinishTestItemRQ> finishNestedStepCapture = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(nestedSteps.get(0)), finishNestedStepCapture.capture());

		FinishTestItemRQ finishStep = finishNestedStepCapture.getValue();
		assertThat(finishStep.getStatus(), equalTo(ItemStatus.PASSED.name()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_static_anonymous_step_failure() {
		mockNestedSteps(client, nestedStepLinks.get(0));
		TestNG result = runTests(MethodStepFailure.class);
		assertThat(result.getStatus(), equalTo(1));

		verify(client).startTestItem(same(stepUuid), any(StartTestItemRQ.class));

		ArgumentCaptor<FinishTestItemRQ> finishNestedStepCapture = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(nestedSteps.get(0)), finishNestedStepCapture.capture());

		FinishTestItemRQ finishStep = finishNestedStepCapture.getValue();
		assertThat(finishStep.getStatus(), equalTo(ItemStatus.FAILED.name()));

		ArgumentCaptor<List<MultipartBody.Part>> logCaptor = ArgumentCaptor.forClass(List.class);
		verify(client, atLeast(1)).log(logCaptor.capture());

		verifyLogged(logCaptor,
				stepUuid,
				LogLevel.ERROR,
				"org.opentest4j.AssertionFailedError: " + MethodStepFailure.FAILURE_MESSAGE
		);
	}

}
