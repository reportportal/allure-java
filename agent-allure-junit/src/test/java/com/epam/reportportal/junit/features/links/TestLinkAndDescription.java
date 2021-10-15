package com.epam.reportportal.junit.features.links;

import io.qameta.allure.Description;
import io.qameta.allure.Link;
import org.junit.Test;

public class TestLinkAndDescription {

	public static final String DESCRIPTION = "My test description";

	public static final String LINK_NAME = "Test Case Link";
	public static final String LINK_URL = "https://example.com/test/case";

	@Test
	@Link(name = "Test Case Link", url = "https://example.com/test/case")
	@Description("My test description")
	public void simpleTest1() {
	}
}
