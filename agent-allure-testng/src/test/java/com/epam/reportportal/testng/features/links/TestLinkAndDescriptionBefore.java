package com.epam.reportportal.testng.features.links;

import io.qameta.allure.Description;
import io.qameta.allure.Link;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestLinkAndDescriptionBefore {

	public static final String DESCRIPTION = "My before description";

	public static final String LINK_NAME = "Before Link";
	public static final String LINK_URL = "https://example.com/before";

	@Link(name = "Before Link", url = "https://example.com/before")
	@Description("My before description")
	@BeforeMethod
	public void setup() {
	}

	@Test
	public void simpleTest1() {
	}
}
