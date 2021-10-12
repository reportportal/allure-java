package com.epam.reportportal.testng.features.runtime;

import io.qameta.allure.Allure;
import org.testng.annotations.Test;

public class TestLinkAddNoDescription {
	public static final String LINK_NAME = "test";
	public static final String LINK_URL = "https://example.com/test";

	@Test
	public void simpleTest1() {
		Allure.link(LINK_NAME, LINK_URL);
	}
}
