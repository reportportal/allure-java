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

import com.epam.reportportal.service.Launch;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import io.reactivex.Maybe;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

@Aspect
public class RuntimeAspect {
	private static final Map<Maybe<String>, Set<ItemAttributesRQ>> ATTRIBUTE_MAP = new ConcurrentHashMap<>();
	private static final Map<Maybe<String>, Set<Pair<String, String>>> LINK_MAP = new ConcurrentHashMap<>();
	private static final Map<Maybe<String>, String> DESCRIPTION_MAP = new ConcurrentHashMap<>();
	private static final Map<Maybe<String>, String> DESCRIPTION_HTML_MAP = new ConcurrentHashMap<>();

	@Pointcut(value = "execution(public static void io.qameta.allure.Allure.link(java.lang.String, java.lang.String, java.lang.String))")
	public void linkMethod() {
	}

	@Before(value = "linkMethod()")
	public void addLink(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		String url = args[2].toString();
		String name = ofNullable(args[0]).map(Object::toString).orElse(url);
		ofNullable(Launch.currentLaunch()).map(l -> l.getStepReporter().getParent())
				.ifPresent(p -> LINK_MAP.computeIfAbsent(p, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
						.add(Pair.of(name, url)));
	}

	@Nullable
	public static Set<Pair<String, String>> retrieveRuntimeLinks(@Nonnull Maybe<String> itemId) {
		return LINK_MAP.remove(itemId);
	}

	@Pointcut(value = "execution(public static void io.qameta.allure.Allure.description(..))")
	public void descriptionMethod() {
	}

	@Pointcut(value = "execution(public static void io.qameta.allure.Allure.descriptionHtml(..))")
	public void descriptionHtmlMethod() {
	}

	@Before(value = "descriptionMethod()")
	public void addDescription(JoinPoint joinPoint) {
		String description = (String) joinPoint.getArgs()[0];
		ofNullable(Launch.currentLaunch()).map(l -> l.getStepReporter().getParent())
				.ifPresent(p -> DESCRIPTION_MAP.putIfAbsent(p, description));
	}

	@Before(value = "descriptionHtmlMethod()")
	public void addDescriptionHtml(JoinPoint joinPoint) {
		String descriptionHtml = (String) joinPoint.getArgs()[0];
		ofNullable(Launch.currentLaunch()).map(l -> l.getStepReporter().getParent())
				.ifPresent(p -> DESCRIPTION_HTML_MAP.put(p, descriptionHtml));
	}

	@Nullable
	public static String retrieveRuntimeDescription(@Nonnull Maybe<String> itemId) {
		String description = DESCRIPTION_MAP.remove(itemId);
		return ofNullable(DESCRIPTION_HTML_MAP.remove(itemId)).orElse(description);
	}

	@Pointcut(value = "execution(public static void io.qameta.allure.Allure.label(..))")
	public void labelMethod() {
	}

	@Before(value = "labelMethod()")
	public void addLabel(JoinPoint joinPoint) {
		String name = (String) joinPoint.getArgs()[0];
		String value = (String) joinPoint.getArgs()[1];
		ofNullable(Launch.currentLaunch()).map(l -> l.getStepReporter().getParent())
				.ifPresent(p -> ATTRIBUTE_MAP.computeIfAbsent(p, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
						.add(new ItemAttributesRQ(name, value)));
	}

	@Nullable
	public static Set<ItemAttributesRQ> retrieveRuntimeLabels(@Nonnull Maybe<String> itemId) {
		return ATTRIBUTE_MAP.remove(itemId);
	}
}
