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

import io.qameta.allure.model.Link;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class FormatUtils {
	public static final String MARKDOWN_DELIMITER = "\n\n---\n\n";
	public static final String LINK_PREFIX = "Links:\n";
	public static final String LINK_MARKDOWN = "[%s](%s)\n";

	@Nullable
	public static String appendLinks(@Nullable String description, @Nullable Collection<Pair<String, String>> links) {
		if (links == null || links.isEmpty()) {
			return description;
		}
		StringBuilder builder = ofNullable(description).filter(d -> !d.isEmpty()).map(d -> {
			StringBuilder sb = new StringBuilder(d);
			sb.append(MARKDOWN_DELIMITER);
			return sb;
		}).orElseGet(StringBuilder::new);
		builder.append(LINK_PREFIX);
		links.forEach(l -> builder.append(l.getValue() == null ?
				String.format(LINK_MARKDOWN, l.getKey(), l.getKey()) :
				String.format(LINK_MARKDOWN, l.getKey(), l.getValue())));
		return builder.toString();
	}

	@Nullable
	public static String appendLinks(@Nullable String description, @Nullable Set<Link> links) {
		return ofNullable(links).map(ll -> appendLinks(description,
				ll.stream().map(l -> Pair.of(l.getName(), l.getUrl())).collect(Collectors.toList())
		)).orElse(description);
	}

	@Nullable
	public static String appendLink(@Nullable String description, @Nullable String name, @Nullable String url) {
		return appendLinks(description, Collections.singleton(Pair.of(name, url)));
	}

}
