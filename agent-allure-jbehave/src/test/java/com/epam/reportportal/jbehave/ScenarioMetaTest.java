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

package com.epam.reportportal.jbehave;

import com.epam.reportportal.jbehave.integration.EmptySteps;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.util.test.CommonUtils;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ScenarioMetaTest extends BaseTest {

	private final String storyId = CommonUtils.namedId("story_");
	private final String scenarioId = CommonUtils.namedId("scenario_");
	private final String stepId = CommonUtils.namedId("step_");

	private final ReportPortalClient client = mock(ReportPortalClient.class);
	private final AllureAwareStepFormat format = new AllureAwareStepFormat(ReportPortal.create(client,
			standardParameters(),
			testExecutor()
	));

	@BeforeEach
	public void setupMock() {
		mockLaunch(client, null, storyId, scenarioId, stepId);
		mockBatchLogging(client);
	}

	private static final String STORY_NAME = "ScenarioMetaInfo.story";
	private static final String STORY_PATH = "stories/" + STORY_NAME;
	private static final Set<Pair<String, String>> STEP_ATTRIBUTES = new HashSet<Pair<String, String>>() {{
		add(Pair.of("severity", "critical"));
		add(Pair.of("issue", "IS-1234"));
		add(Pair.of("tmsLink", "TMS-4321"));
	}};

	@Test
	public void verify_a_scenario_with_meta_attributes() {
		run(format, STORY_PATH, new EmptySteps());

		ArgumentCaptor<StartTestItemRQ> startCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);
		verify(client).startTestItem(startCaptor.capture());
		verify(client).startTestItem(same(storyId), startCaptor.capture());
		verify(client).startTestItem(same(scenarioId), any());

		List<StartTestItemRQ> items = startCaptor.getAllValues();

		StartTestItemRQ startSuite = items.get(0);
		assertThat(startSuite.getAttributes(), empty());

		StartTestItemRQ startScenario = items.get(1);
		assertThat(startScenario.getAttributes(), allOf(notNullValue(), hasSize(STEP_ATTRIBUTES.size())));
		Set<ItemAttributesRQ> scenarioAttributes = startScenario.getAttributes();
		scenarioAttributes.forEach(a -> assertThat(STEP_ATTRIBUTES, hasItem(Pair.of(a.getKey(), a.getValue()))));
	}
}
