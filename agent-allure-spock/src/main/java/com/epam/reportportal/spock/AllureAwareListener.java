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

package com.epam.reportportal.spock;

import com.epam.reportportal.allure.FormatUtils;
import com.epam.reportportal.allure.RuntimeAspect;
import com.epam.reportportal.listeners.ItemStatus;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import io.reactivex.Maybe;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.epam.reportportal.allure.AnnotationUtils.*;
import static java.util.Optional.ofNullable;

public class AllureAwareListener extends ReportPortalSpockListener {

	private static final Map<Maybe<String>, String> DESCRIPTION_TRACKER = new ConcurrentHashMap<>();

	@Override
	@Nonnull
	protected Maybe<String> startSpec(@Nonnull StartTestItemRQ rq) {
		Maybe<String> id = super.startSpec(rq);
		DESCRIPTION_TRACKER.put(id, rq.getDescription());
		return id;
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildSpecItemRq(@Nonnull SpecInfo spec) {
		StartTestItemRQ rq = super.buildSpecItemRq(spec);
		AnnotatedElement c = spec.getReflection();
		processLabels(rq, c);
		processLinks(rq, c);
		return rq;
	}

	@Override
	@Nonnull
	protected Maybe<String> startFixture(@Nonnull Maybe<String> parentId, @Nonnull StartTestItemRQ rq) {
		Maybe<String> id = super.startFixture(parentId, rq);
		DESCRIPTION_TRACKER.put(id, rq.getDescription());
		return id;
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildFixtureItemRq(@Nonnull FeatureInfo feature, @Nonnull MethodInfo fixture, boolean inherited) {
		StartTestItemRQ rq = super.buildFixtureItemRq(feature, fixture, inherited);
		Method method = fixture.getReflection();
		processLabels(rq, method);
		processDescription(rq, Thread.currentThread().getContextClassLoader(), method);
		processLinks(rq, method);
		return rq;
	}

	@Override
	@Nonnull
	protected Maybe<String> startFeature(@Nonnull Maybe<String> parentId, @Nonnull StartTestItemRQ rq) {
		Maybe<String> id = super.startFeature(parentId, rq);
		DESCRIPTION_TRACKER.put(id, rq.getDescription());
		return id;
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildFeatureItemRq(@Nonnull FeatureInfo featureInfo) {
		StartTestItemRQ rq = super.buildFeatureItemRq(featureInfo);
		Method m = featureInfo.getFeatureMethod().getReflection();
		processLabels(rq, m);
		processAllureId(rq, m);
		processDescription(rq, Thread.currentThread().getContextClassLoader(), m);
		processLinks(rq, m);
		processPriority(rq, m);
		processFlaky(rq, m);
		processMuted(rq, m);
		return rq;
	}

	@Override
	@Nonnull
	protected Maybe<String> startIteration(@Nonnull Maybe<String> parentId, @Nonnull StartTestItemRQ rq) {
		Maybe<String> id = super.startIteration(parentId, rq);
		DESCRIPTION_TRACKER.put(id, rq.getDescription());
		return id;
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildIterationItemRq(@Nonnull IterationInfo iteration) {
		StartTestItemRQ rq = super.buildIterationItemRq(iteration);
		AnnotatedElement m = iteration.getReflection();
		processLabels(rq, m);
		processAllureId(rq, m);
		processLinks(rq, m);
		return rq;
	}

	@Override
	@Nonnull
	protected FinishTestItemRQ buildFinishTestItemRq(@Nonnull Maybe<String> itemId, @Nullable ItemStatus status) {
		FinishTestItemRQ rq = super.buildFinishTestItemRq(itemId, status);
		ofNullable(RuntimeAspect.retrieveRuntimeDescription(itemId)).ifPresent(d -> {
			DESCRIPTION_TRACKER.put(itemId, d);
			rq.setDescription(d);
		});
		Set<Pair<String, String>> links = RuntimeAspect.retrieveRuntimeLinks(itemId);
		ofNullable(links).ifPresent(l -> rq.setDescription(FormatUtils.appendLinks(DESCRIPTION_TRACKER.remove(itemId), l)));
		rq.setAttributes(RuntimeAspect.retrieveRuntimeLabels(itemId));
		return rq;
	}
}
