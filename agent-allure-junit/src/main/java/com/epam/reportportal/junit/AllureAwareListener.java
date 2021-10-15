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

package com.epam.reportportal.junit;

import com.epam.reportportal.allure.FormatUtils;
import com.epam.reportportal.allure.RuntimeAspect;
import com.epam.reportportal.listeners.ItemStatus;
import com.epam.reportportal.listeners.ItemType;
import com.epam.reportportal.service.tree.TestItemTree;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.nordstrom.automation.junit.LifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.epam.reportportal.allure.AnnotationUtils.*;
import static java.util.Optional.ofNullable;

public class AllureAwareListener extends ReportPortalListener {

	private static final Map<FrameworkMethod, String> DESCRIPTION_TRACKER = new ConcurrentHashMap<>();

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartStepRq(@Nonnull final Object runner, @Nullable final Description description,
			@Nonnull final FrameworkMethod method, @Nonnull final ReflectiveCallable callable, @Nullable final Date startTime) {
		StartTestItemRQ rq = super.buildStartStepRq(runner, description, method, callable, startTime);
		ItemType itemType = ItemType.valueOf(rq.getType());
		if (itemType == ItemType.STEP) {
			ofNullable(method.getMethod()).ifPresent(m -> {
				processLabels(rq, m);
				processAllureId(rq, m);
				processDescription(rq, Thread.currentThread().getContextClassLoader(), m);
				processLinks(rq, m);
				processPriority(rq, m);
				processFlaky(rq, m);
				processMuted(rq, m);
			});
		} else {
			ofNullable(method.getMethod()).ifPresent(m -> {
				processLabels(rq, m);
				processDescription(rq, Thread.currentThread().getContextClassLoader(), m);
				processLinks(rq, m);
			});
		}
		DESCRIPTION_TRACKER.put(method, rq.getDescription());
		return rq;
	}

	@Nonnull
	protected StartTestItemRQ buildStartTestItemRq(@Nonnull final Object runner, @Nullable final Date startTime) {
		StartTestItemRQ rq = super.buildStartTestItemRq(runner, startTime);
		TestClass testClass = LifecycleHooks.getTestClassOf(runner);
		processLabels(rq, testClass.getJavaClass());
		processLinks(rq, testClass.getJavaClass());
		return rq;
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartSuiteRq(@Nonnull final Object runner, @Nullable final Date startTime) {
		StartTestItemRQ rq = super.buildStartSuiteRq(runner, startTime);
		TestClass testClass = LifecycleHooks.getTestClassOf(runner);
		processLabels(rq, testClass.getJavaClass());
		processLinks(rq, testClass.getJavaClass());
		return rq;
	}

	@Override
	@Nonnull
	protected FinishTestItemRQ buildFinishStepRq(@Nonnull final Object runner, @Nullable final FrameworkMethod method,
			@Nonnull final ReflectiveCallable callable, @Nonnull final ItemStatus status) {
		FinishTestItemRQ rq = super.buildFinishStepRq(runner, method, callable, status);
		ofNullable(method).map(m -> getLeaf(runner, m, callable)).map(TestItemTree.TestItemLeaf::getItemId).ifPresent(id->{
			ofNullable(RuntimeAspect.retrieveRuntimeDescription(id)).ifPresent(d -> {
				DESCRIPTION_TRACKER.put(method, d);
				rq.setDescription(d);
			});
			Set<Pair<String, String>> links = RuntimeAspect.retrieveRuntimeLinks(id);
			ofNullable(links).ifPresent(l -> rq.setDescription(FormatUtils.appendLinks(DESCRIPTION_TRACKER.remove(method), l)));
			rq.setAttributes(RuntimeAspect.retrieveRuntimeLabels(id));
		});
		return rq;
	}
}
