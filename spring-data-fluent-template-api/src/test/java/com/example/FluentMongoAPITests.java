/*
 * Copyright 2017 the original author or authors.
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
import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Query.*;
import static org.springframework.data.mongodb.core.query.Update.*;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ExecutableFindOperation.TerminatingFind;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mongodb.client.result.UpdateResult;

/**
 * @author Christoph Strobl
 * @since 2017/11
 */
@ExtendWith(SpringExtension.class)
@DataMongoTest
class FluentMongoAPITests {

	@Autowired MongoTemplate template;

	Person luke = new Person("luke");
	Person han = new Person("han");

	@BeforeEach
	void beforeEach() {
		template.save(luke);
		template.save(han);
	}

	@AfterEach
	void after() {
		template.dropCollection(Person.class);
	}

	@Test
	@DisplayName("Optional by default")
	void findLukeOptional() {

		Optional<Person> person = template.query(Person.class) //
				.matching(query(where("firstname").is("luke"))) //
				.first();

		assertThat(person).contains(luke);
	}

	@Test
	@DisplayName("Don't like Optional, that's fine.")
	void findLuke() {

		Person person = template.query(Person.class) //
				.matching(query(where("firstname").is("luke"))) //
				.firstValue();

		assertThat(person).isEqualTo(luke);
	}

	@Test
	@DisplayName("Make sure queries return unique results.")
	void uniqueResultsPlease() {

		TerminatingFind<Person> findLuke = template.query(Person.class) //
				.matching(query(where("firstname").is("luke")));


		assertThat(findLuke.oneValue()).isEqualTo(luke);

		template.save(new Person("luke"));

		assertThat(catchThrowable(() -> findLuke.oneValue())) //
				.isInstanceOf(IncorrectResultSizeDataAccessException.class);
	}

	@Test
	@DisplayName("Want to map things to another type? Projections to the rescue.")
	void projectionSupport() {

		Jedi jedi = template.query(Person.class) //
				.as(Jedi.class) //
				.matching(query(where("firstname").is("luke"))) //
				.firstValue();

		assertThat(jedi).isEqualTo(new Jedi("luke"));
	}

	@Test
	@DisplayName("Not only for queries, but also other operations")
	void updateSupport() {

		UpdateResult updateResult = template.update(Person.class) //
				.matching(query(byExample(han))) //
				.apply(update("firstname", "yoda")) //
				.all();

		assertThat(updateResult.getModifiedCount()).isEqualTo(1);
	}

	@Data
	@Document(collection = "star-wars")
	static class Person {

		@Id String id;
		@Field("first_name") String firstname;

		Person(String firstname) {
			this.firstname = firstname;
		}
	}

	@Data
	@AllArgsConstructor
	static class Jedi {

		@Field("first_name") String name;
	}
}
