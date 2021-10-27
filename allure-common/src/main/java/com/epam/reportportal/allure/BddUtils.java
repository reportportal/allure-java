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

package com.epam.reportportal.allure;

import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class BddUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(BddUtils.class);

	private static volatile Map<String, String> allureProperties;

	public static final String MUTED_TAG = "muted";
	public static final String ISSUE_TAG = "issue";
	public static final String TMS_LINK_TAG = "tmsLink";

	public static final String ISSUE_PATTERN_PROPERTY = "allure.link.issue.pattern";
	public static final String TMS_PATTERN_PROPERTY = "allure.link.tms.pattern";

	private BddUtils() {
		throw new IllegalStateException("Utility class instantiation");
	}

	private static Map<String, String> getProperties() {
		if (allureProperties == null) {
			synchronized (BddUtils.class) {
				if (allureProperties == null) {
					Properties properties = new Properties();
					ofNullable(Thread.currentThread().getContextClassLoader().getResourceAsStream("allure.properties")).ifPresent(is -> {
						try {
							properties.load(is);
						} catch (IOException e) {
							LOGGER.warn("Unable to load pattern property file");
						}
					});
					Map<String, String> propertyMap = new HashMap<>();
					properties.forEach((k, v) -> propertyMap.put(k.toString(), v.toString()));
					allureProperties = Collections.unmodifiableMap(propertyMap);
				}
			}
		}
		return allureProperties;
	}

	public static void processLinks(@Nonnull StartTestItemRQ rq, @Nonnull Collection<Pair<String, String>> properties) {
		List<Pair<String, String>> links = properties.stream().map(p -> {
			if (ISSUE_TAG.equals(p.getKey())) {
				String issue = p.getValue();
				return ofNullable(getProperties().get(ISSUE_PATTERN_PROPERTY)).map(pattern -> pattern.replace("{}", issue)).orElse(issue);
			}
			if (TMS_LINK_TAG.equals(p.getKey())) {
				String tms = p.getValue();
				return ofNullable(getProperties().get(TMS_PATTERN_PROPERTY)).map(pattern -> pattern.replace("{}", tms)).orElse(tms);
			}
			return null;
		}).filter(Objects::nonNull).map(l -> Pair.of(l, l)).collect(Collectors.toList());
		rq.setDescription(FormatUtils.appendLinks(rq.getDescription(), links));
	}

	public static void processMuted(@Nonnull StartTestItemRQ rq, @Nonnull Collection<Pair<String, String>> properties) {
		properties.stream().filter(p -> MUTED_TAG.equals(p.getKey())).findAny().ifPresent(p -> rq.setHasStats(false));
	}

	public static void splitKeyValueAttributes(@Nonnull StartTestItemRQ rq, @Nonnull Set<String> attributesToSplit, char keyValueDelimiter) {
		rq.setAttributes(ofNullable(rq.getAttributes()).orElse(Collections.emptySet()).stream().map(a -> {
			String value = a.getValue();
			int valueDelimiterIdx = ofNullable(value).map(av -> av.indexOf(keyValueDelimiter)).orElse(-1);
			if (valueDelimiterIdx > 0) {
				String key = value.substring(1, valueDelimiterIdx);
				if (attributesToSplit.contains(key)) {
					return new ItemAttributesRQ(key, value.substring(valueDelimiterIdx + 1));
				}
			}
			return a;
		}).collect(Collectors.toSet()));
	}
}
