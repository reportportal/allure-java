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

package com.epam.reportportal.junit5;

import com.epam.reportportal.allure.FormatUtils;
import com.epam.reportportal.allure.RuntimeAspect;
import com.epam.reportportal.listeners.ItemStatus;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.epam.reportportal.allure.AnnotationUtils.*;
import static java.util.Optional.ofNullable;

public class AllureAwareExtension extends ReportPortalExtension {

	private static final Map<ExtensionContext, String> DESCRIPTION_TRACKER = new ConcurrentHashMap<>();

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartConfigurationRq(@Nonnull Method method, @Nonnull ExtensionContext parentContext,
			@Nonnull ExtensionContext context, @Nonnull ItemType itemType) {
		StartTestItemRQ rq = super.buildStartConfigurationRq(method, parentContext, context, itemType);
		processLabels(rq, method);
		processDescription(rq, Thread.currentThread().getContextClassLoader(), method);
		processLinks(rq, method);
		ofNullable(rq.getDescription()).ifPresent(d -> DESCRIPTION_TRACKER.put(context, d));
		return rq;
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartStepRq(@Nonnull final ExtensionContext context, @Nonnull final List<Object> arguments,
			@Nonnull final ItemType itemType, @Nonnull final String description, @Nonnull final Date startTime) {
		StartTestItemRQ rq = super.buildStartStepRq(context, arguments, itemType, description, startTime);
		if (itemType == ItemType.SUITE) {
			context.getTestClass().ifPresent(c -> {
				processLabels(rq, c);
				processLinks(rq, c);
			});
		} else {
			context.getTestMethod().ifPresent(m -> {
				processLabels(rq, m);
				processAllureId(rq, m);
				processDescription(rq, Thread.currentThread().getContextClassLoader(), m);
				processLinks(rq, m);
				processPriority(rq, m);
				processFlaky(rq, m);
				processMuted(rq, m);
			});
			DESCRIPTION_TRACKER.put(context, rq.getDescription());
		}
		return rq;
	}

	@Nonnull
	protected FinishTestItemRQ buildFinishTestItemRq(@Nonnull ExtensionContext context, @Nonnull ItemStatus status) {
		FinishTestItemRQ rq = super.buildFinishTestItemRq(context, status);
		getItemId(context).ifPresent(itemId -> {
			ofNullable(RuntimeAspect.retrieveRuntimeDescription(itemId)).ifPresent(d -> {
				DESCRIPTION_TRACKER.put(context, d);
				rq.setDescription(d);
			});
			Set<Pair<String, String>> links = RuntimeAspect.retrieveRuntimeLinks(itemId);
			ofNullable(links).ifPresent(l -> rq.setDescription(FormatUtils.appendLinks(DESCRIPTION_TRACKER.remove(context), l)));
			rq.setAttributes(RuntimeAspect.retrieveRuntimeLabels(itemId));
		});
		return rq;
	}
}
