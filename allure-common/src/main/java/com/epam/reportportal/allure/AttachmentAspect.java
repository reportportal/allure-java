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

import com.epam.reportportal.listeners.LogLevel;
import com.epam.reportportal.message.ReportPortalMessage;
import com.epam.reportportal.service.ReportPortal;
import com.google.common.io.ByteSource;
import io.qameta.allure.Attachment;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Objects;

import static io.qameta.allure.util.AspectUtils.getParametersMap;
import static io.qameta.allure.util.NamingUtils.processNameTemplate;

@Aspect
public class AttachmentAspect {
	@Pointcut("@annotation(io.qameta.allure.Attachment)")
	public void withAttachmentAnnotation() {
		//pointcut body, should be empty
	}

	@Pointcut("execution(* *(..))")
	public void anyMethod() {
		//pointcut body, should be empty
	}

	@AfterReturning(pointcut = "anyMethod() && withAttachmentAnnotation()", returning = "result")
	public void attachment(final JoinPoint joinPoint, final Object result) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Attachment attachment = methodSignature.getMethod().getAnnotation(Attachment.class);
		byte[] bytes = (result instanceof byte[]) ? (byte[]) result : Objects.toString(result).getBytes(StandardCharsets.UTF_8);
		ByteSource byteSource = ByteSource.wrap(bytes);
		String name = attachment.value().isEmpty() ?
				methodSignature.getName() :
				processNameTemplate(attachment.value(), getParametersMap(joinPoint));
		String type = attachment.type();
		ReportPortal.emitLog(new ReportPortalMessage(byteSource, type, name), LogLevel.UNKNOWN.name(), Calendar.getInstance().getTime());
	}
}
