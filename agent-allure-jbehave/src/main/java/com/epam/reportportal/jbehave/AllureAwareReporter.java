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

import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.apache.commons.lang3.tuple.Pair;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.reportportal.allure.BddUtils.processLinks;
import static com.epam.reportportal.allure.BddUtils.processMuted;

public class AllureAwareReporter {

	private AllureAwareReporter() {
		throw new IllegalStateException("Utility class instantiation");
	}

	private static List<Pair<String, String>> toMap(Meta meta) {
		return meta.getPropertyNames().stream().map(p -> Pair.of(p, meta.getProperty(p))).collect(Collectors.toList());
	}

	public static StartTestItemRQ processStartSuiteRq(Story story, StartTestItemRQ rq) {
		processLinks(rq, toMap(story.getMeta()));
		return rq;
	}

	public static StartTestItemRQ processStartScenarioRq(Scenario scenario, StartTestItemRQ rq) {
		List<Pair<String, String>> properties = toMap(scenario.getMeta());
		processLinks(rq, properties);
		processMuted(rq, properties);
		return rq;
	}
}
