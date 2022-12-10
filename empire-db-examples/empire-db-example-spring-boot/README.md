/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
# Empire-DB Spring Boot Example

This example uses the convenience configuration options provided by Spring Boot.
Instead of `config.xml` all configuration can be found in `application.yml`.

This example builds an executable JAR you can build and execute from this folder like this:

```sh
$ mvn clean install
$ java -jar target/empire-db-example-spring-boot-3.0.0-SNAPSHOT.jar
```

An embedded hsqldb is used by default.