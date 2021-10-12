package com.epam.reportportal.testng.features.runtime;

import io.qameta.allure.Allure;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;

public class TestAttributeAdd {
	public static final String EPIC = "my-inline-epic";

	@Test
	@Feature("my-feature")
	public void simpleTest1() {
		Allure.epic(EPIC);
	}
}
