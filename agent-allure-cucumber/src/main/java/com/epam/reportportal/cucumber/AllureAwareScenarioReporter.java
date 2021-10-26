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

import com.epam.reportportal.listeners.ItemStatus;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import io.reactivex.Maybe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.epam.reportportal.cucumber.AllureAwareReporter.*;

public class AllureAwareScenarioReporter extends ScenarioReporter {

	private static final Map<Maybe<String>, String> DESCRIPTION_TRACKER = new ConcurrentHashMap<>();

	@Override
	@Nonnull
	protected Maybe<String> startFeature(@Nonnull StartTestItemRQ startFeatureRq) {
		Maybe<String> id = super.startFeature(startFeatureRq);
		DESCRIPTION_TRACKER.put(id, startFeatureRq.getDescription());
		return id;
	}

	@Override
	@Nonnull
	protected Maybe<String> startScenario(@Nonnull Maybe<String> featureId, @Nonnull StartTestItemRQ startScenarioRq) {
		Maybe<String> id = super.startScenario(featureId, startScenarioRq);
		DESCRIPTION_TRACKER.put(id, startScenarioRq.getDescription());
		return id;
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartFeatureRequest(@Nonnull Feature feature, @Nonnull String uri) {
		return processStartFeatureRequest(feature, uri, super.buildStartFeatureRequest(feature, uri));
	}

	@Nonnull
	protected StartTestItemRQ buildStartScenarioRequest(@Nonnull Scenario scenario, @Nonnull String uri) {
		return processStartScenarioRequest(scenario, uri, super.buildStartScenarioRequest(scenario, uri));
	}

	@Nonnull
	protected FinishTestItemRQ buildFinishTestItemRequest(@Nonnull Maybe<String> itemId, @Nullable ItemStatus status) {
		FinishTestItemRQ rq = super.buildFinishTestItemRequest(itemId, status);
		return processFinishDescription(itemId, rq, DESCRIPTION_TRACKER);
	}
}
