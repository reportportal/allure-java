package com.epam.reportportal.junit5.features.links;

import io.qameta.allure.Description;
import io.qameta.allure.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestLinkAndDescriptionBefore {

	public static final String DESCRIPTION = "My before description";

	public static final String LINK_NAME = "Before Link";
	public static final String LINK_URL = "https://example.com/before";

	@Link(name = LINK_URL, url = LINK_NAME)
	@Description(DESCRIPTION)
	@BeforeEach
	public void setup() {
	}

	@Test
	public void simpleTest1() {
	}
}
