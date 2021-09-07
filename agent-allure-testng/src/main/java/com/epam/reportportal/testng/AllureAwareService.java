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
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;
import org.testng.xml.XmlTest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.epam.reportportal.allure.AnnotationUtils.*;
import static java.util.Optional.ofNullable;

public class AllureAwareService extends TestNGService {

	public AllureAwareService() {
		super();
	}

	public AllureAwareService(@Nonnull final ReportPortal reportPortal) {
		super(reportPortal);
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartTestItemRq(ITestContext testContext) {
		StartTestItemRQ rq = super.buildStartTestItemRq(testContext);
		ofNullable(testContext.getCurrentXmlTest()).map(XmlTest::getClasses).ifPresent(xmlClasses -> xmlClasses.forEach(c -> {
			processLabels(rq, c.getSupportClass());
			processLinks(rq, c.getSupportClass());
		}));
		return rq;
	}

	@Nonnull
	private Optional<Method> getMethod(@Nullable final ITestNGMethod method) {
		return ofNullable(method).map(ITestNGMethod::getConstructorOrMethod).map(ConstructorOrMethod::getMethod);
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartConfigurationRq(@Nonnull ITestResult testResult, @Nullable TestMethodType type) {
		StartTestItemRQ rq = super.buildStartConfigurationRq(testResult, type);
		getMethod(testResult.getMethod()).ifPresent(m -> {
			processLabels(rq, m);
			processLinks(rq, m);
		});
		return rq;
	}

	@Override
	@Nonnull
	protected StartTestItemRQ buildStartStepRq(final @Nonnull ITestResult testResult, final @Nonnull TestMethodType type) {
		StartTestItemRQ rq = super.buildStartStepRq(testResult, type);
		getMethod(testResult.getMethod()).ifPresent(m -> {
			processLabels(rq, m);
			processLinks(rq, m);
			processAllureId(rq, m);
		});
		return rq;
	}
}
