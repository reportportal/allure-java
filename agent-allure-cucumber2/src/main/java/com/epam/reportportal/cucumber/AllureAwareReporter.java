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
import cucumber.api.TestCase;
import gherkin.ast.Feature;
import gherkin.ast.Tag;
import gherkin.pickles.PickleTag;
import io.reactivex.Maybe;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class AllureAwareReporter {

	private static final char ALLURE_VALUE_DELIMITER = '=';

	private static final Set<String> ATTRIBUTE_REPLACE_SET = Collections.unmodifiableSet(new HashSet<String>() {{
		add("severity");
		add("issue");
		add("tmsLink");
	}});

	private static List<Pair<String, String>> toMap(List<String> tags) {
		return tags.stream().map(t -> {
			int valueDelimiterIdx = t.indexOf(ALLURE_VALUE_DELIMITER);
			if (valueDelimiterIdx > 0) {
				String key = t.substring(1, valueDelimiterIdx);
				return Pair.of(key, t.substring(valueDelimiterIdx + 1));
			}
			return Pair.of(t, (String) null);
		}).collect(Collectors.toList());
	}

	private static List<Pair<String, String>> toMapTags(List<Tag> tags) {
		return toMap(tags.stream().map(Tag::getName).collect(Collectors.toList()));
	}

	private static List<Pair<String, String>> toMapPickleTags(List<PickleTag> tags) {
		return toMap(tags.stream().map(PickleTag::getName).collect(Collectors.toList()));
	}

	@Nonnull
	public static StartTestItemRQ processStartFeatureRequest(@Nonnull Feature feature, @Nonnull StartTestItemRQ rq) {
		BddUtils.processLinks(rq, toMapTags(feature.getTags()));
		BddUtils.splitKeyValueAttributes(rq, ATTRIBUTE_REPLACE_SET, ALLURE_VALUE_DELIMITER);
		return rq;
	}

	@SuppressWarnings("unused")
	@Nonnull
	public static StartTestItemRQ processStartScenarioRequest(@Nonnull TestCase testCase, @Nonnull StartTestItemRQ rq) {
		List<Pair<String, String>> parameters = toMapPickleTags(testCase.getTags());
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
