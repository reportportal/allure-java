package com.epam.reportportal.testng.features.inline;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TestDescriptionOverride {
	public static final String DESCRIPTION = "My test description";

	@Test
	@Description("My annotation description")
	public void simpleTest1() {
		Allure.description(DESCRIPTION);
	}
}
