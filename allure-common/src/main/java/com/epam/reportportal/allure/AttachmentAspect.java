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
import com.epam.reportportal.utils.files.Utils;
import com.google.common.io.ByteSource;
import io.qameta.allure.Attachment;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.util.function.Supplier;

import static io.qameta.allure.util.AspectUtils.getParametersMap;
import static io.qameta.allure.util.NamingUtils.processNameTemplate;

@Aspect
public class AttachmentAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentAspect.class);

	@Pointcut("@annotation(io.qameta.allure.Attachment)")
	public void withAttachmentAnnotation() {
	}

	@Pointcut("execution(* *(..))")
	public void anyMethod() {
	}

	@Pointcut("execution(public static void io.qameta.allure.Allure.addAttachment(java.lang.String, java.io.InputStream))")
	public void attachmentInputStreamShortMethod() {
	}

	@Pointcut("execution(public static void io.qameta.allure.Allure.addAttachment(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String))")
	public void attachmentInputStreamLongMethod() {
	}

	@Pointcut("execution(public static void io.qameta.allure.Allure.addAttachment(java.lang.String, java.lang.String))")
	public void attachmentStringShortMethod() {
	}

	@Pointcut("execution(public static void io.qameta.allure.Allure.addAttachment(java.lang.String, java.lang.String, java.lang.String))")
	public void attachmentStringTypeMethod() {
	}

	@Pointcut("execution(public static void io.qameta.allure.Allure.addAttachment(java.lang.String, java.lang.String, java.lang.String, java.lang.String))")
	public void attachmentStringTypeExtMethod() {
	}

	@Pointcut("execution(public static * io.qameta.allure.Allure.addByteAttachmentAsync(java.lang.String, java.lang.String, java.util.function.Supplier))")
	public void byteAttachmentAsyncMethod() {
	}

	@Pointcut("execution(public static * io.qameta.allure.Allure.addByteAttachmentAsync(java.lang.String, java.lang.String, java.lang.String, java.util.function.Supplier))")
	public void byteAttachmentAsyncMethodType() {
	}

	@Pointcut("execution(public static * io.qameta.allure.Allure.addStreamAttachmentAsync(java.lang.String, java.lang.String, java.util.function.Supplier))")
	public void streamAttachmentAsyncMethod() {
	}

	@Pointcut("execution(public static * io.qameta.allure.Allure.addStreamAttachmentAsync(java.lang.String, java.lang.String, java.lang.String, java.util.function.Supplier))")
	public void streamAttachmentAsyncMethodType() {
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

	private static void logByteData(String name, String type, byte[] data) {
		ByteSource byteSource = ByteSource.wrap(data);
		ReportPortal.emitLog(new ReportPortalMessage(byteSource, type, name), LogLevel.UNKNOWN.name(), Calendar.getInstance().getTime());
	}

	private static InputStream logInputStream(String name, String type, InputStream inputStream) {
		byte[] content;
		try {
			content = Utils.readInputStreamToBytes(inputStream);
		} catch (Exception e) {
			content = new byte[0];
			LOGGER.warn("Unable to read attachment", e);
		}
		logByteData(name, type, content);
		return new ByteArrayInputStream(content);
	}

	private static Supplier<?> wrapSupplier(String name, String type, Supplier<?> supplier) {
		return () -> {
			Object data = supplier.get();
			if (data instanceof InputStream) {
				return logInputStream(name, type, (InputStream) data);
			}
			logByteData(name, type, (byte[]) data);
			return data;
		};
	}

	@Around(value = "byteAttachmentAsyncMethodType() || streamAttachmentAsyncMethodType()", argNames = "joinPoint")
	public Object addAttachmentAsyncExplicitType(ProceedingJoinPoint joinPoint) throws Throwable {
		final Object[] args = joinPoint.getArgs();
		final Supplier<?> supplier = (Supplier<?>) args[3];
		Supplier<?> newSupplier = wrapSupplier(args[0].toString(), args[1].toString(), supplier);
		Object[] newArgs = Arrays.copyOf(args, 4);
		newArgs[3] = newSupplier;
		return joinPoint.proceed(newArgs);
	}

	@Around(value = "attachmentInputStreamShortMethod()", argNames = "joinPoint")
	public Object addAttachmentExplicitInputStreamShort(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		InputStream bufferedStream = logInputStream(args[0].toString(), null, (InputStream) args[1]);
		Object[] newArgs = Arrays.copyOf(args, 2);
		newArgs[1] = bufferedStream;
		return joinPoint.proceed(newArgs);
	}

	@Around(value = "attachmentInputStreamLongMethod()", argNames = "joinPoint")
	public Object addAttachmentExplicitInputStreamLong(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		InputStream bufferedStream = logInputStream(args[0].toString(), args[1].toString(), (InputStream) args[2]);
		Object[] newArgs = Arrays.copyOf(args, 4);
		newArgs[2] = bufferedStream;
		return joinPoint.proceed(newArgs);
	}

	@Before(value = "attachmentStringShortMethod()", argNames = "joinPoint")
	public void addAttachmentExplicitStringShort(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		logByteData(args[0].toString(), "text/plain", args[1].toString().getBytes(StandardCharsets.UTF_8));
	}

	@Before(value = "attachmentStringTypeMethod()", argNames = "joinPoint")
	public void addAttachmentExplicitStringType(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		logByteData(args[0].toString(), args[1].toString(), args[2].toString().getBytes(StandardCharsets.UTF_8));
	}

	@Before(value = "attachmentStringTypeExtMethod()", argNames = "joinPoint")
	public void addAttachmentExplicitStringTypeExt(JoinPoint joinPoint) {
		addAttachmentExplicitStringType(joinPoint);
	}
}
