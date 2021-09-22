package com.epam.reportportal.testng.features.inline;

import io.qameta.allure.Allure;
import org.testng.annotations.Test;

public class TestDescriptionAdd {
	public static final String DESCRIPTION = "My test description";

	@Test
	public void simpleTest1() {
		Allure.description(DESCRIPTION);
	}
}
