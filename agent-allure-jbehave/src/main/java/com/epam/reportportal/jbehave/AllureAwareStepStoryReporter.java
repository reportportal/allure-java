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

import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.tree.TestItemTree;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.epam.reportportal.jbehave.AllureAwareReporter.processStartScenarioRq;
import static com.epam.reportportal.jbehave.AllureAwareReporter.processStartSuiteRq;

public class AllureAwareStepStoryReporter extends ReportPortalStepStoryReporter {
	private static final Map<Object, String> DESCRIPTION_TRACKER = new ConcurrentHashMap<>();

	public AllureAwareStepStoryReporter(Supplier<Launch> launchSupplier, TestItemTree testItemTree) {
		super(launchSupplier, testItemTree);
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartStoryRq(@Nonnull Story story, @Nonnull String codeRef, @Nullable final Date startTime) {
		StartTestItemRQ rq = processStartSuiteRq(story, super.buildStartStoryRq(story, codeRef, startTime));
		DESCRIPTION_TRACKER.put(story, rq.getDescription());
		return rq;
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartScenarioRq(@Nonnull Scenario scenario, @Nonnull String codeRef, @Nullable final Date startTime) {
		StartTestItemRQ rq = processStartScenarioRq(scenario, super.buildStartScenarioRq(scenario, codeRef, startTime));
		DESCRIPTION_TRACKER.put(scenario, rq.getDescription());
		return rq;
	}
}
