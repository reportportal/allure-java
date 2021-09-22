package com.epam.reportportal.testng.features.inline;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TestNoUpdatesDescription {
	public static final String DESCRIPTION = "My test description";

	@Test
	@Description("My test description")
	public void simpleTest1() {
	}
}
