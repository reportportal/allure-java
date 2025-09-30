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

import com.epam.reportportal.aspect.StepNameUtils;
import com.epam.reportportal.listeners.ItemStatus;
import com.epam.reportportal.listeners.LogLevel;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.step.StepRequestUtils;
import com.epam.reportportal.utils.formatting.templating.TemplateConfiguration;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.model.Status;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Optional.ofNullable;

@Aspect
public class StepAspect {

	private static final Map<Status, ItemStatus> STATUS_MAPPER = Map.of(
			Status.PASSED,
			ItemStatus.PASSED,
			Status.FAILED,
			ItemStatus.FAILED,
			Status.BROKEN,
			ItemStatus.INTERRUPTED,
			Status.SKIPPED,
			ItemStatus.SKIPPED
	);

	private static final Map<String, AtomicLong> STEP_COUNTERS = new ConcurrentHashMap<>();

	public static final String STEP_DESCRIPTION = "Step description: ";

	@Pointcut("@annotation(step)")
	public void withStepAnnotation(Step step) {
	}

	@Pointcut("execution(* *.*(..))")
	public void anyMethod() {
	}

	@Pointcut("execution(public static * io.qameta.allure.Allure.step(..))")
	public void stepMethod() {
	}

	@Before(value = "!stepMethod() && (anyMethod() && withStepAnnotation(step))", argNames = "joinPoint,step")
	public void startNestedStepAnnotation(JoinPoint joinPoint, Step step) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String name = step.value().trim().isEmpty() ?
				signature.getName() :
				StepNameUtils.getStepName(step.value(), new TemplateConfiguration(), signature, joinPoint);
		StartTestItemRQ startStepRequest = com.epam.reportportal.aspect.StepRequestUtils.buildStartStepRequest(
				name,
				ofNullable(signature.getMethod().getAnnotation(Description.class)).map(Description::value)
						.filter(d -> !d.isEmpty())
						.orElse(null),
				Instant.now(),
				signature
		);
		//noinspection ReactiveStreamsUnusedPublisher
		ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().startNestedStep(startStepRequest));
		ofNullable(startStepRequest.getDescription()).ifPresent(d -> ReportPortal.emitLog(
				STEP_DESCRIPTION + d,
				LogLevel.INFO.name(),
				Instant.now()
		));
	}

	@AfterReturning(value = "!stepMethod() && (anyMethod() && withStepAnnotation(step))", argNames = "step")
	public void finishNestedStep(Step step) {
		ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().finishNestedStep());
	}

	@SuppressWarnings("ReactiveStreamsUnusedPublisher")
	@AfterThrowing(value = "!stepMethod() && (anyMethod() && withStepAnnotation(step))", argNames = "step")
	public void failedNestedStep(Step step) {
		ofNullable(Launch.currentLaunch()).ifPresent(l -> {
			FinishTestItemRQ rq = StepRequestUtils.buildFinishTestItemRequest(ItemStatus.FAILED, Instant.now());
			l.getStepReporter().finishNestedStep(rq);
		});
	}

	@Before(value = "stepMethod()", argNames = "joinPoint")
	public void startNestedStepExplicit(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		int argLength = args.length;
		switch (argLength) {
			case 1:
				if (args[0].getClass().getName().startsWith("io.qameta.allure.Allure")) {
					return;
				}
				if (args[0] instanceof Allure.ThrowableContextRunnable || args[0] instanceof Allure.ThrowableContextRunnableVoid) {
					String className = args[0].getClass().getName();
					int innerClassSignIndex = className.indexOf('$');
					String parentClassName = innerClassSignIndex > 0 ? className.substring(0, innerClassSignIndex) : className;
					String parentSimpleName = parentClassName.substring(parentClassName.lastIndexOf('.') + 1);
					AtomicLong stepCounter = STEP_COUNTERS.computeIfAbsent(parentClassName, k -> new AtomicLong());
					String stepName = innerClassSignIndex > 0 ?
							parentSimpleName + " anonymous step " + stepCounter.incrementAndGet() :
							parentSimpleName + " step " + stepCounter.incrementAndGet();
					StartTestItemRQ rq = com.epam.reportportal.service.step.StepRequestUtils.buildStartStepRequest(
							stepName,
							null,
							Instant.now()
					);
					//noinspection ReactiveStreamsUnusedPublisher
					ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().startNestedStep(rq));
				}
				return;
			case 2:
				if (args[1] instanceof Status) {
					StartTestItemRQ rq = com.epam.reportportal.service.step.StepRequestUtils.buildStartStepRequest(
							args[0].toString(),
							null,
							Instant.now()
					);
					//noinspection ReactiveStreamsUnusedPublisher
					ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().startNestedStep(rq));
					return;
				}
				if (args[1] instanceof Allure.ThrowableRunnable || args[1] instanceof Allure.ThrowableRunnableVoid
						|| args[1] instanceof Allure.ThrowableContextRunnableVoid || args[1] instanceof Allure.ThrowableContextRunnable) {
					StartTestItemRQ rq = com.epam.reportportal.service.step.StepRequestUtils.buildStartStepRequest(
							args[0].toString(),
							null,
							Instant.now()
					);
					//noinspection ReactiveStreamsUnusedPublisher
					ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().startNestedStep(rq));
				}
		}
	}

	@SuppressWarnings("ReactiveStreamsUnusedPublisher")
	@AfterReturning(value = "stepMethod()", argNames = "joinPoint")
	public void finishNestedStep(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		int argLength = args.length;
		switch (argLength) {
			case 1:
				if (args[0].getClass().getName().startsWith("io.qameta.allure.Allure")) {
					return;
				}
				if (args[0] instanceof Allure.ThrowableContextRunnable || args[0] instanceof Allure.ThrowableContextRunnableVoid) {
					ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().finishNestedStep());
				}
				return;
			case 2:
				if (args[1] instanceof Status) {
					FinishTestItemRQ rq = com.epam.reportportal.service.step.StepRequestUtils.buildFinishTestItemRequest(
							ofNullable(STATUS_MAPPER.get((Status) args[1])).orElseGet(() -> {
								ReportPortal.emitLog(
										"Unable to convert item status: " + args[1].toString(),
										LogLevel.ERROR.name(),
										Instant.now()
								);
								return ItemStatus.FAILED;
							}), Instant.now()
					);
					ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().finishNestedStep(rq));
					return;
				}
				if (args[1] instanceof Allure.ThrowableRunnable || args[1] instanceof Allure.ThrowableRunnableVoid
						|| args[1] instanceof Allure.ThrowableContextRunnableVoid || args[1] instanceof Allure.ThrowableContextRunnable) {
					ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().finishNestedStep());
				}
		}
	}

	@SuppressWarnings("ReactiveStreamsUnusedPublisher")
	@AfterThrowing(value = "stepMethod()", argNames = "joinPoint")
	public void finishNestedStepFailure(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		int argLength = args.length;
		switch (argLength) {
			case 1:
				if (args[0].getClass().getName().startsWith("io.qameta.allure.Allure")) {
					return;
				}
				if (args[0] instanceof Allure.ThrowableContextRunnableVoid || args[0] instanceof Allure.ThrowableContextRunnable) {
					FinishTestItemRQ rq = StepRequestUtils.buildFinishTestItemRequest(ItemStatus.FAILED, Instant.now());
					ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().finishNestedStep(rq));
				}
				return;
			case 2:
				if (args[1] instanceof Allure.ThrowableRunnable || args[1] instanceof Allure.ThrowableRunnableVoid
						|| args[1] instanceof Allure.ThrowableContextRunnableVoid || args[1] instanceof Allure.ThrowableContextRunnable) {
					FinishTestItemRQ rq = StepRequestUtils.buildFinishTestItemRequest(ItemStatus.FAILED, Instant.now());
					ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().finishNestedStep(rq));
				}
		}
	}
}
