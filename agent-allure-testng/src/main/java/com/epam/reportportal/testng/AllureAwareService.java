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

package com.epam.reportportal.testng;

import com.epam.reportportal.service.ReportPortal;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import io.qameta.allure.util.AnnotationUtils;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class AllureAwareService extends TestNGService {

	public static final String MARKDOWN_DELIMITER = "\n\n---\n\n";
	public static final String LINK_MARKDOWN = "[%s](%s)";

	public AllureAwareService() {
		super();
	}

	public AllureAwareService(@Nonnull final ReportPortal reportPortal) {
		super(reportPortal);
	}

	@Override
	protected StartTestItemRQ buildStartTestItemRq(ITestContext testContext) {
		StartTestItemRQ rq = super.buildStartTestItemRq(testContext);
		return rq;
	}

	@Nonnull
	private Optional<Method> getMethod(@Nullable final ITestNGMethod method) {
		return Optional.ofNullable(method).map(ITestNGMethod::getConstructorOrMethod).map(ConstructorOrMethod::getMethod);
	}

	protected StartTestItemRQ buildStartStepRq(final @Nonnull ITestResult testResult, final @Nonnull TestMethodType type) {
		StartTestItemRQ rq = super.buildStartStepRq(testResult, type);
		getMethod(testResult.getMethod()).map(AnnotationUtils::getLinks).filter(l -> !l.isEmpty()).ifPresent(links -> {
			StringBuilder builder = ofNullable(rq.getDescription()).filter(d -> !d.isEmpty()).map(d -> {
				StringBuilder sb = new StringBuilder(d);
				sb.append(MARKDOWN_DELIMITER);
				return sb;
			}).orElseGet(StringBuilder::new);
			builder.append("Links:\n");
			links.forEach(l -> builder.append(l.getUrl() == null ?
					String.format(LINK_MARKDOWN, l.getName(), l.getName()) :
					String.format(LINK_MARKDOWN, l.getName(), l.getUrl())).append("\n"));
			rq.setDescription(builder.toString());
		});
		return rq;
	}
}
