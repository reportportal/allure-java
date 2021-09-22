package com.epam.reportportal.testng.features.inline;

import io.qameta.allure.Allure;
import org.testng.annotations.Test;

public class TestDescriptionHtmlOverride {
	public static final String DESCRIPTION = "My test description";
	public static final String DESCRIPTION_HTML = "My test description HTML";

	@Test
	public void simpleTest1() {
		Allure.descriptionHtml(DESCRIPTION);
		Allure.descriptionHtml(DESCRIPTION_HTML);
	}
}
