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

import com.epam.reportportal.allure.StepAspect;
import com.epam.reportportal.listeners.ItemStatus;
import com.epam.reportportal.listeners.LogLevel;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.testng.features.steps.*;
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
import static org.hamcrest.Matchers.*;
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
	public void test_method_step_failure() {
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

		verifyLogged(logCaptor, stepUuid, LogLevel.ERROR, "org.opentest4j.AssertionFailedError: " + MethodStepFailure.FAILURE_MESSAGE);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_method_step_description() {
		mockNestedSteps(client, nestedStepLinks.get(0));
		TestNG result = runTests(MethodStepDescription.class);
		assertThat(result.getStatus(), equalTo(0));

		verify(client).startTestItem(same(stepUuid), any(StartTestItemRQ.class));

		ArgumentCaptor<FinishTestItemRQ> finishNestedStepCapture = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(nestedSteps.get(0)), finishNestedStepCapture.capture());

		FinishTestItemRQ finishStep = finishNestedStepCapture.getValue();
		assertThat(finishStep.getStatus(), equalTo(ItemStatus.PASSED.name()));

		ArgumentCaptor<List<MultipartBody.Part>> logCaptor = ArgumentCaptor.forClass(List.class);
		verify(client, atLeast(1)).log(logCaptor.capture());

		verifyLogged(logCaptor,
				nestedSteps.get(0),
				LogLevel.INFO,
				StepAspect.STEP_DESCRIPTION + MethodStepDescription.TEST_STEP_DESCRIPTION
		);
	}

	@Test
	public void test_method_two_steps() {
		mockNestedSteps(client, nestedStepLinks);
		TestNG result = runTests(Collections.singletonList(TestNgListener.class), MethodTwoSteps.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<StartTestItemRQ> startNestedStepCapture = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client, times(2)).startTestItem(same(stepUuid), startNestedStepCapture.capture());

		List<StartTestItemRQ> startSteps = startNestedStepCapture.getAllValues();
		startSteps.forEach(s -> {
			assertThat(s.getName(), not(emptyOrNullString()));
			assertThat(s.isHasStats(), equalTo(Boolean.FALSE));
		});

		ArgumentCaptor<FinishTestItemRQ> finishNestedStepCapture = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(nestedSteps.get(0)), finishNestedStepCapture.capture());
		verify(client).finishTestItem(same(nestedSteps.get(1)), finishNestedStepCapture.capture());

		finishNestedStepCapture.getAllValues().forEach(f -> assertThat(f.getStatus(), equalTo(ItemStatus.PASSED.name())));
	}

	@Test
	public void test_two_levels_method_step() {
		mockNestedSteps(client, nestedStepLinks.get(0));
		mockNestedSteps(client, Pair.of(nestedSteps.get(0), nestedSteps.get(1)));
		TestNG result = runTests(MethodTwoLevelsStep.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<StartTestItemRQ> startNestedStepCapture1 = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(same(stepUuid), startNestedStepCapture1.capture());
		ArgumentCaptor<StartTestItemRQ> startNestedStepCapture2 = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(same(nestedSteps.get(0)), startNestedStepCapture2.capture());

		StartTestItemRQ startStep1 = startNestedStepCapture1.getValue();
		assertThat(startStep1.getName(), not(emptyOrNullString()));
		assertThat(startStep1.isHasStats(), equalTo(Boolean.FALSE));

		StartTestItemRQ startStep2 = startNestedStepCapture2.getValue();
		assertThat(startStep2.getName(), not(emptyOrNullString()));
		assertThat(startStep2.isHasStats(), equalTo(Boolean.FALSE));

		ArgumentCaptor<FinishTestItemRQ> finishNestedStepCapture1 = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(nestedSteps.get(0)), finishNestedStepCapture1.capture());

		FinishTestItemRQ finishStep1 = finishNestedStepCapture1.getValue();
		assertThat(finishStep1.getStatus(), equalTo(ItemStatus.PASSED.name()));

		ArgumentCaptor<FinishTestItemRQ> finishNestedStepCapture2 = ArgumentCaptor.forClass(FinishTestItemRQ.class);
		verify(client).finishTestItem(same(nestedSteps.get(1)), finishNestedStepCapture2.capture());

		FinishTestItemRQ finishStep2 = finishNestedStepCapture2.getValue();
		assertThat(finishStep2.getStatus(), equalTo(ItemStatus.PASSED.name()));
	}
}
