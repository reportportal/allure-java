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

import com.epam.reportportal.allure.BddUtils;
import com.epam.reportportal.allure.FormatUtils;
import com.epam.reportportal.allure.RuntimeAspect;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;
import io.reactivex.Maybe;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public abstract class AllureAwareReporter {

	private static final char ALLURE_VALUE_DELIMITER = '=';

	private static final Set<String> ATTRIBUTE_REPLACE_SET = Collections.unmodifiableSet(new HashSet<String>() {{
		add("severity");
		add("issue");
		add("tmsLink");
	}});

	private static List<Pair<String, String>> toMap(TagStatement tagged) {
		return tagged.getTags().stream().map(Tag::getName).map(t -> {
			int valueDelimiterIdx = t.indexOf(ALLURE_VALUE_DELIMITER);
			if (valueDelimiterIdx > 0) {
				String key = t.substring(1, valueDelimiterIdx);
				return Pair.of(key, t.substring(valueDelimiterIdx + 1));
			}
			return Pair.of(t, (String) null);
		}).collect(Collectors.toList());
	}

	@Nonnull
	public static StartTestItemRQ processStartFeatureRequest(@Nonnull Feature feature, @Nonnull StartTestItemRQ rq) {
		BddUtils.processLinks(rq, toMap(feature));
		BddUtils.splitKeyValueAttributes(rq, ATTRIBUTE_REPLACE_SET, ALLURE_VALUE_DELIMITER);
		return rq;
	}

	@Nonnull
	public static StartTestItemRQ processStartScenarioRequest(@Nonnull Scenario scenario, @Nonnull StartTestItemRQ rq) {
		List<Pair<String, String>> parameters = toMap(scenario);
		BddUtils.processLinks(rq, parameters);
		BddUtils.processMuted(rq, parameters);
		BddUtils.splitKeyValueAttributes(rq, ATTRIBUTE_REPLACE_SET, ALLURE_VALUE_DELIMITER);
		return rq;
	}

	public static FinishTestItemRQ processFinishDescription(@Nonnull final Maybe<String> id, @Nonnull final FinishTestItemRQ rq,
			@Nonnull final Map<Maybe<String>, String> descriptionTracker) {
		ofNullable(RuntimeAspect.retrieveRuntimeDescription(id)).ifPresent(d -> {
			descriptionTracker.put(id, d);
			rq.setDescription(d);
		});
		Set<Pair<String, String>> links = RuntimeAspect.retrieveRuntimeLinks(id);
		ofNullable(links).ifPresent(l -> rq.setDescription(FormatUtils.appendLinks(descriptionTracker.remove(id), l)));
		rq.setAttributes(RuntimeAspect.retrieveRuntimeLabels(id));
		return rq;
	}
}
