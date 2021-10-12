package com.epam.reportportal.testng.util;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.listeners.LogLevel;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.util.test.CommonUtils;
import com.epam.reportportal.utils.http.HttpRequestUtils;
import com.epam.ta.reportportal.ws.model.BatchSaveOperatingRS;
import com.epam.ta.reportportal.ws.model.Constants;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.fasterxml.jackson.core.type.TypeReference;
import io.reactivex.Maybe;
import okhttp3.MultipartBody;
import okio.Buffer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.testng.ITestNGListener;
import org.testng.TestNG;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class TestUtils {

	public static final String TEST_NAME = "TestContainer";

	private static TestNG getTestNg(List<Class<? extends ITestNGListener>> listeners) {
		TestNG testNG = new TestNG(true);
		testNG.setListenerClasses(listeners);
		testNG.setDefaultTestName(TEST_NAME);
		testNG.setExcludedGroups("optional");
		return testNG;
	}

	public static TestNG runTests(Class<?>... classes) {
		return runTests(Collections.singletonList(TestNgListener.class), classes);
	}

	public static TestNG runTests(List<Class<? extends ITestNGListener>> listeners, Class<?>... classes) {
		final TestNG testNG = getTestNg(listeners);
		testNG.setTestClasses(classes);
		testNG.run();
		return testNG;
	}

	public static TestNG runTests(String xmlPath) {
		return runTests(Collections.singletonList(TestNgListener.class), xmlPath);
	}

	public static TestNG runTests(List<Class<? extends ITestNGListener>> listeners, String xmlPath) {
		final TestNG testNG = getTestNg(listeners);
		ofNullable(Thread.currentThread()
				.getContextClassLoader()
				.getResource(xmlPath)).ifPresent(uri -> testNG.setTestSuites(Collections.singletonList(uri.toString())));
		testNG.run();
		return testNG;
	}

	public static <T> Maybe<T> createMaybe(T id) {
		return Maybe.create(emitter -> {
			emitter.onSuccess(id);
			emitter.onComplete();
		});
	}

	public static void mockLaunch(ReportPortalClient client, String launchUuid, String suiteUuid, String testClassUuid,
			String testMethodUuid) {
		mockLaunch(client, launchUuid, suiteUuid, testClassUuid, Collections.singleton(testMethodUuid));
	}

	public static String namedUuid(String name) {
		return name + UUID.randomUUID().toString().substring(name.length());
	}

	@SuppressWarnings("unchecked")
	public static void mockLaunch(ReportPortalClient client, String launchUuid, String suiteUuid, String testClassUuid,
			Collection<String> testMethodUuidList) {
		when(client.startLaunch(any())).thenReturn(TestUtils.createMaybe(new StartLaunchRS(launchUuid, 1L)));

		Maybe<ItemCreatedRS> suiteMaybe = TestUtils.createMaybe(new ItemCreatedRS(suiteUuid, suiteUuid));
		when(client.startTestItem(any())).thenReturn(suiteMaybe);

		Maybe<ItemCreatedRS> testClassMaybe = TestUtils.createMaybe(new ItemCreatedRS(testClassUuid, testClassUuid));
		when(client.startTestItem(eq(suiteUuid), any())).thenReturn(testClassMaybe);

		List<Maybe<ItemCreatedRS>> responses = testMethodUuidList.stream()
				.map(uuid -> TestUtils.createMaybe(new ItemCreatedRS(uuid, uuid)))
				.collect(Collectors.toList());
		Maybe<ItemCreatedRS> first = responses.get(0);
		Maybe<ItemCreatedRS>[] other = responses.subList(1, responses.size()).toArray(new Maybe[0]);
		when(client.startTestItem(eq(testClassUuid), any())).thenReturn(first, other);
		new HashSet<>(testMethodUuidList).forEach(testMethodUuid -> when(client.finishTestItem(eq(testMethodUuid), any())).thenReturn(
				TestUtils.createMaybe(new OperationCompletionRS())));

		Maybe<OperationCompletionRS> testClassFinishMaybe = TestUtils.createMaybe(new OperationCompletionRS());
		when(client.finishTestItem(eq(testClassUuid), any())).thenReturn(testClassFinishMaybe);

		Maybe<OperationCompletionRS> suiteFinishMaybe = TestUtils.createMaybe(new OperationCompletionRS());
		when(client.finishTestItem(eq(suiteUuid), any())).thenReturn(suiteFinishMaybe);

		when(client.finishLaunch(eq(launchUuid), any())).thenReturn(TestUtils.createMaybe(new OperationCompletionRS()));
	}

	public static void mockNestedSteps(ReportPortalClient client, Pair<String, String> parentNestedPair) {
		mockNestedSteps(client, Collections.singletonList(parentNestedPair));
	}

	@SuppressWarnings("unchecked")
	public static void mockNestedSteps(final ReportPortalClient client, final List<Pair<String, String>> parentNestedPairs) {
		Map<String, List<String>> responseOrders = parentNestedPairs.stream()
				.collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toList())));
		responseOrders.forEach((k, v) -> {
			List<Maybe<ItemCreatedRS>> responses = v.stream()
					.map(uuid -> TestUtils.createMaybe(new ItemCreatedRS(uuid, uuid)))
					.collect(Collectors.toList());

			Maybe<ItemCreatedRS> first = responses.get(0);
			Maybe<ItemCreatedRS>[] other = responses.subList(1, responses.size()).toArray(new Maybe[0]);
			when(client.startTestItem(same(k), any())).thenReturn(first, other);
		});
		parentNestedPairs.forEach(p -> when(client.finishTestItem(
				same(p.getValue()),
				any()
		)).thenAnswer((Answer<Maybe<OperationCompletionRS>>) invocation -> TestUtils.createMaybe(new OperationCompletionRS())));
	}

	public static ListenerParameters standardParameters() {
		ListenerParameters result = new ListenerParameters();
		result.setClientJoin(false);
		result.setBatchLogsSize(1);
		result.setBaseUrl("http://localhost:8080");
		result.setLaunchName("My-test-launch" + CommonUtils.generateUniqueId());
		result.setProjectName("test-project");
		result.setEnable(true);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static void mockLogging(ReportPortalClient client) {
		when(client.log(any(List.class))).thenReturn(createMaybe(new BatchSaveOperatingRS()));
	}

	public static List<SaveLogRQ> extractJsonParts(List<MultipartBody.Part> parts) {
		return parts.stream()
				.filter(p -> ofNullable(p.headers()).map(headers -> headers.get("Content-Disposition"))
						.map(h -> h.contains(Constants.LOG_REQUEST_JSON_PART))
						.orElse(false))
				.map(MultipartBody.Part::body)
				.map(b -> {
					Buffer buf = new Buffer();
					try {
						b.writeTo(buf);
					} catch (IOException ignore) {
					}
					return buf.readByteArray();
				})
				.map(b -> {
					try {
						return HttpRequestUtils.MAPPER.readValue(b, new TypeReference<List<SaveLogRQ>>() {
						});
					} catch (IOException e) {
						return Collections.<SaveLogRQ>emptyList();
					}
				})
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public static List<SaveLogRQ> filterLogs(ArgumentCaptor<List<MultipartBody.Part>> logCaptor, Predicate<SaveLogRQ> filter) {
		return logCaptor.getAllValues().stream().flatMap(l -> extractJsonParts(l).stream()).filter(filter).collect(Collectors.toList());
	}

	public static SaveLogRQ verifyLogged(@Nonnull final ArgumentCaptor<List<MultipartBody.Part>> logCaptor, @Nullable final String itemId,
			@Nonnull final LogLevel level, @Nonnull final String message) {
		return verifyLogged(logCaptor, Collections.singletonList(Triple.of(itemId, level, message))).get(0);
	}

	public static List<SaveLogRQ> verifyLogged(@Nonnull final ArgumentCaptor<List<MultipartBody.Part>> logCaptor,
			Collection<Triple<String, LogLevel, String>> messages) {
		List<SaveLogRQ> logList = filterLogs(
				logCaptor,
				l -> messages.stream()
						.anyMatch(e -> e.getMiddle().name().equals(l.getLevel()) && l.getMessage() != null && l.getMessage()
								.contains(e.getRight()))
		);
		assertThat(logList, hasSize(messages.size()));
		assertThat(
				logList.stream().map(SaveLogRQ::getItemUuid).collect(Collectors.toList()),
				containsInAnyOrder(messages.stream().map(Triple::getLeft).toArray())
		);
		return logList;
	}
}
