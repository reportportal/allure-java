package com.epam.reportportal.testng.features.inline;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TestLinkAddDescription {
	public static final String DESCRIPTION = "My test description";
	public static final String LINK_NAME = "test";
	public static final String LINK_URL = "https://example.com/test";

	@Test
	@Description("My test description")
	public void simpleTest1() {
		Allure.link(LINK_NAME, LINK_URL);
	}
}
