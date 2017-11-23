/*
 * Copyright 2017. the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Christoph Strobl
 * @since 2017/11
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
class PersonRepositoryTests {

	@Autowired PersonRepository repository;

	@BeforeEach
	void beforeEach() {
		repository.save(new Person("luke"));
	}

	@Test
	@DisplayName("Just return the found object - all fine.")
	void allFineWhenWeFindSomething() {
		assertThat(repository.findByFirstname("luke")).isNotNull();
	}

	@Test
	@DisplayName("EmptyResultDataAccessException expected for NonNullable api returning null.")
	void weExpectAnExceptionWhenNothingWasFound() {

		assertThat(catchThrowable(() -> repository.findByFirstname("han")))
				.isInstanceOf(EmptyResultDataAccessException.class);
	}

	@Test
	@DisplayName("Nullable api may just return null. No Exception to be thrown.")
	void weExpectNullWhenNothingFoundAndMethodIsAnnotatedWithNullable() {
		assertThat(repository.findNullableByFirstname("han")).isNull();
	}

}
