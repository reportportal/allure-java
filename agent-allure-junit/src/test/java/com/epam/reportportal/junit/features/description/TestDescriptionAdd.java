package com.epam.reportportal.junit.features.description;

import io.qameta.allure.Allure;
import org.junit.Test;

public class TestDescriptionAdd {
	public static final String DESCRIPTION = "My test description";

	@Test
	public void simpleTest1() {
		Allure.description(DESCRIPTION);
	}
}
