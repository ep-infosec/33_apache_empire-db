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
package org.apache.empire.samples.db.advanced.records;

import org.apache.empire.db.generic.TRecord;
import org.apache.empire.samples.db.advanced.SampleContext;
import org.apache.empire.samples.db.advanced.db.CarSalesDB;

public class BrandRecord extends TRecord<SampleContext, CarSalesDB.Brand>
{
    private static final long serialVersionUID = 1L;

    public BrandRecord(SampleContext context)
    {
        super(context, context.getDatabase().BRAND);
    }
    
    /**
     * Insert a new brand
     * @param wmi
     * @param name
     * @param country
     * @return the BrandRecord itself (returned to allow instruction chaining)
     */
    public BrandRecord insert(String wmi, String name, String country)
    {
        // T = RowSet (Table/View)
        create();
        set(T.WMI,     wmi);
        set(T.NAME,    name);
        set(T.COUNTRY, country);
        update();
        return this;
    }

}
