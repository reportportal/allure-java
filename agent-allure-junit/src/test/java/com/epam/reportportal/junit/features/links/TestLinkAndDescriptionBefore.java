package com.epam.reportportal.junit.features.links;

import io.qameta.allure.Description;
import io.qameta.allure.Link;
import org.junit.Before;
import org.junit.Test;

public class TestLinkAndDescriptionBefore {

	public static final String DESCRIPTION = "My before description";

	public static final String LINK_NAME = "Before Link";
	public static final String LINK_URL = "https://example.com/before";

	@Link(name = LINK_NAME, url = LINK_URL)
	@Description(DESCRIPTION)
	@Before
	public void setup() {
	}

	@Test
	public void simpleTest1() {
	}
}
