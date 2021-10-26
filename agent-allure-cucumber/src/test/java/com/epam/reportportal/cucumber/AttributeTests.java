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

package com.epam.reportportal.cucumber;

import com.epam.reportportal.cucumber.integration.TestScenarioReporter;
import com.epam.reportportal.cucumber.integration.TestStepReporter;
import com.epam.reportportal.cucumber.util.TestUtils;
import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.util.test.CommonUtils;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testng.TestNG;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class AttributeTests {

	@CucumberOptions(features = "src/test/resources/features/FeatureTags.feature", glue = {
			"com.epam.reportportal.cucumber.integration.steps" }, plugin = { "pretty",
			"com.epam.reportportal.cucumber.integration.TestStepReporter" })
	public static class FeatureStepReporter extends AbstractTestNGCucumberTests {

	}

	@CucumberOptions(features = "src/test/resources/features/FeatureTags.feature", glue = {
			"com.epam.reportportal.cucumber.integration.steps" }, plugin = { "pretty",
			"com.epam.reportportal.cucumber.integration.TestScenarioReporter" })
	public static class FeatureScenarioReporter extends AbstractTestNGCucumberTests {

	}

	@CucumberOptions(features = "src/test/resources/features/ScenarioTags.feature", glue = {
			"com.epam.reportportal.cucumber.integration.steps" }, plugin = { "pretty",
			"com.epam.reportportal.cucumber.integration.TestStepReporter" })
	public static class ScenarioStepReporter extends AbstractTestNGCucumberTests {

	}

	@CucumberOptions(features = "src/test/resources/features/ScenarioTags.feature", glue = {
			"com.epam.reportportal.cucumber.integration.steps" }, plugin = { "pretty",
			"com.epam.reportportal.cucumber.integration.TestScenarioReporter" })
	public static class ScenarioScenarioReporter extends AbstractTestNGCucumberTests {

	}

	private final String launchId = CommonUtils.namedId("launch_");
	private final String featureId = CommonUtils.namedId("feature_");
	private final String scenarioId = CommonUtils.namedId("scenario_");
	private final List<String> stepIds = Stream.generate(() -> CommonUtils.namedId("step_")).limit(3).collect(Collectors.toList());
	private final List<String> nestedStepIds = Stream.generate(() -> CommonUtils.namedId("nested_step_"))
			.limit(3)
			.collect(Collectors.toList());

	private final ListenerParameters parameters = TestUtils.standardParameters();
	private final ReportPortalClient client = mock(ReportPortalClient.class);
	private final ExecutorService executorService = TestUtils.testExecutor();
	private final ReportPortal reportPortal = ReportPortal.create(client, parameters, executorService);

	@BeforeEach
	public void initLaunch() {
		TestUtils.mockLaunch(client, launchId, featureId, scenarioId, stepIds);
		TestScenarioReporter.RP.set(reportPortal);
		TestStepReporter.RP.set(reportPortal);
		TestUtils.mockLogging(client);
	}

	private static final Set<Pair<String, String>> ATTRIBUTES = new HashSet<Pair<String, String>>() {{
		add(Pair.of("severity", "critical"));
		add(Pair.of("issue", "IS-1234"));
		add(Pair.of("tmsLink", "TMS-4321"));
	}};

	private static final Set<String> SUITE_DESCRIPTION_LINKS = new HashSet<String>() {{
		add("https://example.com/issue/IS-1234");
		add("https://example.com/tms/TMS-4321");
	}};

	@Test
	public void verify_feature_attributes_step_reporter() {
		TestNG result = TestUtils.runTests(FeatureStepReporter.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<StartTestItemRQ> captor = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(captor.capture());
		verify(client).startTestItem(same(featureId), any(StartTestItemRQ.class));
		verify(client, times(3)).startTestItem(same(scenarioId), any(StartTestItemRQ.class));

		StartTestItemRQ featureItem = captor.getValue();
		assertThat(featureItem.getAttributes(), allOf(hasSize(3)));
		Set<Pair<String, String>> attributes = featureItem.getAttributes()
				.stream()
				.map(a -> Pair.of(a.getKey(), a.getValue()))
				.collect(Collectors.toSet());
		ATTRIBUTES.forEach(a -> assertThat(attributes, hasItem(equalTo(a))));

		String description = featureItem.getDescription();
		SUITE_DESCRIPTION_LINKS.forEach(d -> assertThat(description, containsString(d)));
	}

	@Test
	public void verify_feature_attributes_scenario_reporter() {
		TestUtils.mockNestedSteps(client, nestedStepIds.stream().map(s -> Pair.of(stepIds.get(0), s)).collect(Collectors.toList()));
		TestNG result = TestUtils.runTests(FeatureScenarioReporter.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<StartTestItemRQ> captor = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(any(StartTestItemRQ.class));
		verify(client).startTestItem(same(featureId), captor.capture());
		verify(client).startTestItem(same(scenarioId), any(StartTestItemRQ.class));
		verify(client, times(3)).startTestItem(same(stepIds.get(0)), any(StartTestItemRQ.class));

		StartTestItemRQ featureItem = captor.getValue();
		assertThat(featureItem.getAttributes(), allOf(hasSize(3)));
		Set<Pair<String, String>> attributes = featureItem.getAttributes()
				.stream()
				.map(a -> Pair.of(a.getKey(), a.getValue()))
				.collect(Collectors.toSet());
		ATTRIBUTES.forEach(a -> assertThat(attributes, hasItem(equalTo(a))));

		String description = featureItem.getDescription();
		SUITE_DESCRIPTION_LINKS.forEach(d -> assertThat(description, containsString(d)));
	}

	@Test
	public void verify_scenario_attributes_step_reporter() {
		TestNG result = TestUtils.runTests(ScenarioStepReporter.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<StartTestItemRQ> captor = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(any(StartTestItemRQ.class));
		verify(client).startTestItem(same(featureId), captor.capture());
		verify(client, times(3)).startTestItem(same(scenarioId), any(StartTestItemRQ.class));

		StartTestItemRQ scenarioItem = captor.getValue();
		assertThat(scenarioItem.getAttributes(), allOf(hasSize(3)));
		Set<Pair<String, String>> attributes = scenarioItem.getAttributes()
				.stream()
				.map(a -> Pair.of(a.getKey(), a.getValue()))
				.collect(Collectors.toSet());
		ATTRIBUTES.forEach(a -> assertThat(attributes, hasItem(equalTo(a))));

		String description = scenarioItem.getDescription();
		SUITE_DESCRIPTION_LINKS.forEach(d -> assertThat(description, containsString(d)));
	}

	@Test
	public void verify_scenario_attributes_scenario_reporter() {
		TestUtils.mockNestedSteps(client, nestedStepIds.stream().map(s -> Pair.of(stepIds.get(0), s)).collect(Collectors.toList()));
		TestNG result = TestUtils.runTests(ScenarioScenarioReporter.class);
		assertThat(result.getStatus(), equalTo(0));

		ArgumentCaptor<StartTestItemRQ> captor = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(any(StartTestItemRQ.class));
		verify(client).startTestItem(same(featureId), any(StartTestItemRQ.class));
		verify(client).startTestItem(same(scenarioId), captor.capture());
		verify(client, times(3)).startTestItem(same(stepIds.get(0)), any(StartTestItemRQ.class));

		StartTestItemRQ scenarioItem = captor.getValue();
		assertThat(scenarioItem.getAttributes(), allOf(hasSize(3)));
		Set<Pair<String, String>> attributes = scenarioItem.getAttributes()
				.stream()
				.map(a -> Pair.of(a.getKey(), a.getValue()))
				.collect(Collectors.toSet());
		ATTRIBUTES.forEach(a -> assertThat(attributes, hasItem(equalTo(a))));

		String description = scenarioItem.getDescription();
		SUITE_DESCRIPTION_LINKS.forEach(d -> assertThat(description, containsString(d)));
	}
}
