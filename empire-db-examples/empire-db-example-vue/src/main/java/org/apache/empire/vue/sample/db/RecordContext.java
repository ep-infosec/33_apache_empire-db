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
package org.apache.empire.vue.sample.db;

import java.sql.Connection;

import org.apache.empire.db.DBContext;
import org.apache.empire.rest.app.TextResolver;

/**
 * Provide access to info and resources for a record instance
 * Add more to 
 * @author doebele
 *
 */
public interface RecordContext extends DBContext
{
    SampleDB getDb();
    /**
     * provide a JDBC connection for DB operations 
     * @return the JDBC connection
     */
    @Override
    Connection getConnection();

    /**
     * provides access to the text resolver
     * @return the TextResolver
     */
    TextResolver getTextResolver();
    
    /**
     * provide access to the current user
     * @return the user
     */
    // User getUser()
}
