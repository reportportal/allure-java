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

import io.qameta.allure.Step;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class StepAspect {

	@Pointcut("@annotation(step)")
	public void withStepAnnotation(Step step) {

	}

	@Pointcut("execution(* *.*(..))")
	public void anyMethod() {

	}

	@Pointcut("execution(public static void Allure.step(..))")
	public void stepMethod() {

	}

	@Before(value = "!stepMethod() && anyMethod() && withStepAnnotation(step)", argNames = "joinPoint,step")
	public void startNestedStepAnnotation(JoinPoint joinPoint, Step step) {
	}

	@Before(value = "stepMethod()", argNames = "joinPoint")
	public void startNestedStepExplicit(JoinPoint joinPoint) {

	}
}
