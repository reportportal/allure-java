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

import com.epam.reportportal.listeners.ItemStatus;
import com.epam.reportportal.listeners.ItemType;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.tree.TestItemTree;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import io.reactivex.Maybe;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.epam.reportportal.jbehave.AllureAwareReporter.*;

public class AllureAwareStepStoryReporter extends ReportPortalStepStoryReporter {
	private static final Map<Maybe<String>, String> DESCRIPTION_TRACKER = new ConcurrentHashMap<>();

	public AllureAwareStepStoryReporter(Supplier<Launch> launchSupplier, TestItemTree testItemTree) {
		super(launchSupplier, testItemTree);
	}

	@Override
	@Nonnull
	protected TestItemTree.TestItemLeaf createLeaf(@Nonnull final ItemType type, @Nonnull final StartTestItemRQ rq,
			@Nullable final TestItemTree.TestItemLeaf parent) {
		TestItemTree.TestItemLeaf result = super.createLeaf(type, rq, parent);
		if (ItemType.STORY == type || ItemType.SCENARIO == type) {
			DESCRIPTION_TRACKER.put(result.getItemId(), rq.getDescription());
		}
		return result;
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartStoryRq(@Nonnull final Story story, @Nonnull String codeRef, @Nullable final Date startTime) {
		return processStartSuiteRq(story, super.buildStartStoryRq(story, codeRef, startTime));
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartScenarioRq(@Nonnull final Scenario scenario, @Nonnull String codeRef,
			@Nullable final Date startTime) {
		return processStartScenarioRq(scenario, super.buildStartScenarioRq(scenario, codeRef, startTime));
	}

	@Override
	@Nonnull
	protected FinishTestItemRQ buildFinishTestItemRequest(@Nonnull final Maybe<String> id, @Nullable final ItemStatus status,
			@Nullable Issue issue) {
		return processFinishDescription(id, super.buildFinishTestItemRequest(id, status, issue), DESCRIPTION_TRACKER);
	}
}
