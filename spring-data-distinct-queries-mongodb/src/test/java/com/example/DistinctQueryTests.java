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

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Christoph Strobl
 * @since 2017/11
 */
@ExtendWith(SpringExtension.class)
@DataMongoTest
public class DistinctQueryTests {

	@Autowired MongoTemplate template;

	Person anakin = new Person("anakin", "skywalker");
	Person luke = new Person("luke", "skywalker");
	Person han = new Person("han", "solo");

	@BeforeEach
	void beforeEach() {

		anakin.setSpecialForce(42);
		template.save(anakin);

		luke.setSpecialForce(new Force("jedi tricks"));
		luke.setFather(anakin);
		template.save(luke);

		han.setSpecialForce("hell of a smuggler");
		template.save(han);
	}

	@AfterEach
	void after() {
		template.dropCollection(Person.class);
	}

	@Test
	@DisplayName("Find distinct values for simple type")
	void justDistinctValues() {

		List<String> lastnames = template.query(Person.class) //
				.distinct("lastname") //
				.as(String.class) //
				.all();

		assertThat(lastnames).containsExactlyInAnyOrder("skywalker", "solo");
	}

	@Test
	@DisplayName("Find distinct values for simple type")
	void mixedTypeValues() {

		List<Object> specialForces = template.query(Person.class) //
				.distinct("specialForce") //
				.all();

		assertThat(specialForces).containsExactlyInAnyOrder(anakin.getSpecialForce(), han.getSpecialForce(),
				luke.getSpecialForce());
	}

	@Test
	@DisplayName("Anyone using DBRefs?")
	void resolveDBRefs() {

		List<Person> specialForces = template.query(Person.class) //
				.distinct("father") //
				.as(Person.class).all();

		assertThat(specialForces).containsExactlyInAnyOrder(anakin);
	}

	@Data
	@Document(collection = "star-wars")
	static class Person {

		@Id String id;
		@Field("first_name") String firstname;
		@Field("last_name") String lastname;
		Object specialForce;
		@DBRef Person father;

		public Person(String firstname, String lastname) {

			this.firstname = firstname;
			this.lastname = lastname;
		}
	}

	@Data
	@AllArgsConstructor
	static class Force {
		String use;
	}
}
