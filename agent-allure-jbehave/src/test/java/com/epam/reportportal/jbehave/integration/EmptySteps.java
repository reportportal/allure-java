/*
 * Copyright 2021 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.jbehave.integration;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptySteps {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmptySteps.class);

	@Given("I have empty step")
	public void i_have_empty_step() {
		LOGGER.info("Inside 'I have empty step'");
	}

	@Then("I have another empty step")
	public void i_have_another_empty_step() {
		LOGGER.info("Inside 'I have another empty step'");
	}

	@When("I have one more empty step")
	public void i_have_one_more_empty_step() {
		LOGGER.info("I have one more empty step'");
	}
}
