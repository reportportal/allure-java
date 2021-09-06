package com.epam.reportportal.testng.features;

import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static io.qameta.allure.Allure.step;

@Link("https://example.com/class")
@Feature("My feature")
public class TestMyFirstFeature {

	@Test
	@Link("https://example.com")
	@Link(name = "Test Case Link", url = "https://example.com/test/case")
	@Story("My story 1")
	public void simpleTest1() {
		step("step 1");
		step("step 2");
	}
}
