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
import io.qameta.allure.Description;
import io.qameta.allure.Flaky;
import io.qameta.allure.Muted;
import io.qameta.allure.Severity;
import io.qameta.allure.util.ResultsUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@SuppressWarnings("unused")
public class AnnotationUtils {
	public static final String MARKDOWN_DELIMITER = "\n\n---\n\n";

	private AnnotationUtils() {
		throw new IllegalStateException("Utility class instantiation");
	}

	public static void processLinks(@Nonnull StartTestItemRQ rq, @Nullable AnnotatedElement source) {
		ofNullable(source).map(io.qameta.allure.util.AnnotationUtils::getLinks)
				.filter(l -> !l.isEmpty())
				.ifPresent(links -> rq.setDescription(FormatUtils.appendLinks(rq.getDescription(), links)));
	}

	@Nonnull
	private static Set<ItemAttributesRQ> retrieveAttributes(@Nonnull StartTestItemRQ rq) {
		Set<ItemAttributesRQ> attributes = ofNullable(rq.getAttributes()).map(HashSet::new).orElseGet(HashSet::new);
		rq.setAttributes(attributes);
		return attributes;
	}

	public static void processLabels(@Nonnull StartTestItemRQ rq, @Nullable AnnotatedElement source) {
		ofNullable(source).map(io.qameta.allure.util.AnnotationUtils::getLabels).filter(l -> !l.isEmpty()).ifPresent(labels -> {
			Set<ItemAttributesRQ> attributes = retrieveAttributes(rq);
			labels.forEach(l -> attributes.add(new ItemAttributesRQ(l.getName(), l.getValue())));
		});
	}

	public static void processAllureId(@Nonnull StartTestItemRQ rq, @Nullable AnnotatedElement source) {
		ofNullable(rq.getAttributes()).flatMap(attributes -> attributes.stream()
				.filter(a -> ResultsUtils.ALLURE_ID_LABEL_NAME.equals(a.getKey()))
				.findAny()).ifPresent(id -> {
			if (!ofNullable(source).map(s -> s.getAnnotation(TestCaseId.class)).isPresent()) {
				rq.setTestCaseId(ofNullable(TestCaseIdUtils.getTestCaseId(
						id.getValue(),
						ofNullable(rq.getParameters()).map(params -> params.stream()
								.map(ParameterResource::getValue)
								.collect(Collectors.toList())).orElse(null)
				)).map(TestCaseIdEntry::getId).orElse(null));
			}
		});
	}

	public static void processDescription(@Nonnull StartTestItemRQ rq, @Nonnull ClassLoader classLoader, @Nullable Method source) {
		ofNullable(source).map(s -> s.getAnnotation(Description.class)).flatMap(annotation -> {
			if (annotation.useJavaDoc() || annotation.value().isEmpty()) {
				return ResultsUtils.getJavadocDescription(classLoader, source);
			} else {
				return Optional.of(annotation.value());
			}
		}).map(description -> ofNullable(rq.getDescription()).filter(d -> !d.isEmpty()).map(d -> {
			StringBuilder sb = new StringBuilder(d);
			sb.append(MARKDOWN_DELIMITER);
			return sb;
		}).orElseGet(StringBuilder::new).append(description).toString()).ifPresent(rq::setDescription);
	}

	public static void processPriority(@Nonnull StartTestItemRQ rq, @Nullable Method source) {
		ofNullable(source).ifPresent(s -> {
			Severity annotation = ofNullable(s.getAnnotation(Severity.class)).orElseGet(() -> source.getDeclaringClass()
					.getAnnotation(Severity.class));
			if (annotation != null) {
				// Allure don't know the difference between priority and severity (－‸ლ)
				ItemAttributesRQ attribute = new ItemAttributesRQ("priority", annotation.value().value());
				Set<ItemAttributesRQ> attributes = retrieveAttributes(rq);
				attributes.add(attribute);
			}
		});
	}

	public static void processFlaky(@Nonnull StartTestItemRQ rq, @Nullable Method source) {
		ofNullable(source).ifPresent(s -> {
			Flaky annotation = ofNullable(s.getAnnotation(Flaky.class)).orElseGet(() -> source.getDeclaringClass()
					.getAnnotation(Flaky.class));
			if (annotation != null) {
				ItemAttributesRQ attribute = new ItemAttributesRQ(Flaky.class.getSimpleName().toLowerCase(Locale.ROOT));
				Set<ItemAttributesRQ> attributes = retrieveAttributes(rq);
				attributes.add(attribute);
			}
		});
	}

	public static void processMuted(@Nonnull StartTestItemRQ rq, @Nullable Method source) {
		ofNullable(source).ifPresent(s -> {
			Muted annotation = ofNullable(s.getAnnotation(Muted.class)).orElseGet(() -> source.getDeclaringClass()
					.getAnnotation(Muted.class));
			if (annotation != null) {
				rq.setHasStats(false);
			}
		});
	}
}
