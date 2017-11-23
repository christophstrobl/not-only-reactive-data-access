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

import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Query.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Christoph Strobl
 * @since 2017/11
 */
@ExtendWith(SpringExtension.class)
@DataMongoTest
class ReactiveFluentMongoAPITests {

	@Autowired ReactiveMongoTemplate template;

	Person luke = new Person("luke");
	Person han = new Person("han");

	@BeforeEach
	void beforeEach() {
		StepVerifier.create(template.save(luke)).expectNextCount(1).verifyComplete();
		StepVerifier.create(template.save(han)).expectNextCount(1).verifyComplete();
	}

	@AfterEach
	void after() {
		StepVerifier.create(template.dropCollection(Person.class)).verifyComplete();
	}

	@Test
	@DisplayName("Reactive anyone?")
	void findLukeOptional() {

		Mono<Jedi> person = template.query(Person.class) //
				.as(Jedi.class).matching(query(where("firstname").is("luke"))) //
				.first();

		StepVerifier.create(person).expectNext(new Jedi("luke")).verifyComplete();
	}

	@Data
	@Document(collection = "star-wars")
	static class Person {

		@Id String id;
		@Field("first_name") String firstname;

		public Person(String firstname) {
			this.firstname = firstname;
		}
	}

	@Data
	@AllArgsConstructor
	static class Jedi {

		@Field("first_name") String name;
	}
}
