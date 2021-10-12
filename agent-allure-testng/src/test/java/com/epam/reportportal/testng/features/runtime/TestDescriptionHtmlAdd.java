package com.epam.reportportal.testng.features.runtime;

import io.qameta.allure.Allure;
import org.testng.annotations.Test;

public class TestDescriptionHtmlAdd {
	public static final String DESCRIPTION = "My test description HTML";

	@Test
	public void simpleTest1() {
		Allure.descriptionHtml(DESCRIPTION);
	}
}
