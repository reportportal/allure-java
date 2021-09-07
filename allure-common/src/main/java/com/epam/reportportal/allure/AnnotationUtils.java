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

import com.epam.reportportal.annotations.TestCaseId;
import com.epam.reportportal.service.item.TestCaseIdEntry;
import com.epam.reportportal.utils.TestCaseIdUtils;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import io.qameta.allure.util.ResultsUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class AnnotationUtils {
	public static final String MARKDOWN_DELIMITER = "\n\n---\n\n";
	public static final String LINK_PREFIX = "Links:\n";
	public static final String LINK_MARKDOWN = "[%s](%s)\n";

	public static void processLinks(@Nonnull StartTestItemRQ rq, @Nullable AnnotatedElement source) {
		ofNullable(source).map(io.qameta.allure.util.AnnotationUtils::getLinks).filter(l -> !l.isEmpty()).ifPresent(links -> {
			StringBuilder builder = ofNullable(rq.getDescription()).filter(d -> !d.isEmpty()).map(d -> {
				StringBuilder sb = new StringBuilder(d);
				sb.append(MARKDOWN_DELIMITER);
				return sb;
			}).orElseGet(StringBuilder::new);
			builder.append(LINK_PREFIX);
			links.forEach(l -> builder.append(l.getUrl() == null ?
					String.format(LINK_MARKDOWN, l.getName(), l.getName()) :
					String.format(LINK_MARKDOWN, l.getName(), l.getUrl())));
			rq.setDescription(builder.toString());
		});
	}

	public static void processLabels(@Nonnull StartTestItemRQ rq, @Nullable AnnotatedElement source) {
		ofNullable(source).map(io.qameta.allure.util.AnnotationUtils::getLabels).filter(l -> !l.isEmpty()).ifPresent(labels -> {
			Set<ItemAttributesRQ> attributes = ofNullable(rq.getAttributes()).map(HashSet::new).orElseGet(HashSet::new);
			rq.setAttributes(attributes);
			labels.forEach(l -> attributes.add(new ItemAttributesRQ(l.getName(), l.getValue())));
		});
	}

	public static void processAllureId(@Nonnull StartTestItemRQ rq, @Nullable AnnotatedElement source) {
		rq.getAttributes().stream().filter(a -> ResultsUtils.ALLURE_ID_LABEL_NAME.equals(a.getKey())).findAny().ifPresent(id -> {
			if (ofNullable(source).map(s -> s.getAnnotation(TestCaseId.class)).isEmpty()) {
				rq.setTestCaseId(ofNullable(TestCaseIdUtils.getTestCaseId(id.getValue(),
						ofNullable(rq.getParameters()).map(params->params.stream().map(ParameterResource::getValue).collect(Collectors.toList())).orElse(null)
				)).map(TestCaseIdEntry::getId).orElse(null));
			}
		});
	}
}
