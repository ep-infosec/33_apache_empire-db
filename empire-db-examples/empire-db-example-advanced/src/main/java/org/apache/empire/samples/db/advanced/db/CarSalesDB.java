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
package org.apache.empire.samples.db.advanced.db;

import java.sql.SQLException;

import org.apache.empire.commons.EnumValue;
import org.apache.empire.data.DataType;
import org.apache.empire.db.DBContext;
import org.apache.empire.db.DBSQLScript;
import org.apache.empire.db.DBTableColumn;
import org.apache.empire.db.generic.TDatabase;
import org.apache.empire.db.generic.TTable;
import org.apache.empire.db.validation.DBModelChecker;
import org.apache.empire.db.validation.DBModelErrorLogger;
import org.apache.empire.dbms.DBMSHandler;
import org.apache.empire.dbms.postgresql.DBMSHandlerPostgreSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <PRE>
 * This file contains the definition of the data model in Java.
 * The SampleDB data model consists of three tables and a foreign key relation.
 * The tables are defined as nested classes here, but you may put them in separate files if you want to.
 *
 * PLEASE NOTE THE NAMING CONVENTION:
 * Since all tables, views and columns are declared as "final" constants they are all in upper case.
 * We recommend using a prefix of T_ for tables and C_ for columns in order to keep them together
 * when listed in your IDE's code completion.
 * There is no need to stick to this convention but it makes life just another little bit easier.
 *
 * You may declare other database tables or views in the same way.
 * </PRE>
 */
public class CarSalesDB extends TDatabase<CarSalesDB>
{
    // Logger
    private static final Logger log = LoggerFactory.getLogger(CarSalesDB.class);

    /**
     * EngineType enum
     */
    public enum EngineType
    {
        P("Petrol"),
        D("Diesel"),
        H("Hybrid"),
        E("Electric");
        
        private final String title;
        private EngineType(String title)
        {
            this.title = title;
        }
        @Override
        public String toString()
        {
            return title;
        }
    }

    /**
     * EngineType enum
     */
    public enum DealershipType
    {
        B("Brand"),
        I("Independent"),
        F("Franchise"),
        U("Used cars"),
        G("Small garage");
        
        private final String title;
        private DealershipType(String title)
        {
            this.title = title;
        }
        @Override
        public String toString()
        {
            return title;
        }
    }

    /**
     * Dealer Rating
     */
    public enum DealerRating implements EnumValue
    {
        A,
        B,
        C,
        X;

        /**
         * Override to use Minus '-' instead of 'X' in Database
         */
        @Override
        public Object toValue(boolean numeric)
        {
            if (this==X)
                return "-"; 
            return (numeric ? ordinal() : name());
        }

        @Override
        public String toString()
        {
            return name();
        }
    }
    
    /**
     * This class represents the Brand table.
     */
    public static class Brand extends TTable<CarSalesDB>
    {
        public final DBTableColumn WMI;
        public final DBTableColumn NAME;
        public final DBTableColumn COUNTRY;
        public final DBTableColumn UPDATE_TIMESTAMP;
        
        public Brand(CarSalesDB db)
        {
            super("BRAND", db);
            // ID
            WMI             = addColumn("WMI",              DataType.VARCHAR,       3, true); // World Manufacturer Index (see Wikipedia)
            NAME            = addColumn("NAME",             DataType.VARCHAR,      80, true);
            COUNTRY         = addColumn("COUNTRY",          DataType.VARCHAR,      40, false);
            UPDATE_TIMESTAMP= addTimestamp("UPDATE_TIMESTAMP");

            // Set Primary Key
            setPrimaryKey(WMI);
        }
    }

    /**
     * This class represents the Model table.
     */
    public static class Model extends TTable<CarSalesDB>
    {
        public final DBTableColumn ID;
        public final DBTableColumn NAME;
        public final DBTableColumn SPECIFICATION;
        public final DBTableColumn WMI;
        public final DBTableColumn TRIM;
        public final DBTableColumn ENGINE_TYPE;
        public final DBTableColumn ENGINE_POWER;
        public final DBTableColumn BASE_PRICE;
        public final DBTableColumn FIRST_SALE;
        public final DBTableColumn LAST_SALE;
        public final DBTableColumn MODEL_BINARY;
        public final DBTableColumn MODEL_XML;
        public final DBTableColumn SALES_INFO;
        public final DBTableColumn UPDATE_TIMESTAMP;

        public Model(CarSalesDB db)
        {
            super("MODEL", db);
            
            // ID
            ID              = addIdentity  ("ID",               "MODEL_ID_SEQ"); // Optional Sequence name for some DBMS (e.g. Oracle)
            NAME            = addColumn    ("NAME",             DataType.VARCHAR,     20, true);
            SPECIFICATION   = addColumn    ("SPECIFICATION",    DataType.VARCHAR,     40, true);
            WMI             = addForeignKey("WMI",              db.BRAND,                 true);
            TRIM            = addColumn    ("TRIM",             DataType.VARCHAR,     20, true);
            ENGINE_TYPE     = addColumn    ("ENGINE_TYPE",      DataType.CHAR,         1, true, EngineType.class);
            ENGINE_POWER    = addColumn    ("ENGINE_POWER",     DataType.DECIMAL,    4.0, true);
            BASE_PRICE      = addColumn    ("BASE_PRICE",       DataType.DECIMAL,    8.2, false);
            FIRST_SALE      = addColumn    ("FIRST_SALE",       DataType.DATE,         0, true);
            LAST_SALE       = addColumn    ("LAST_SALE",        DataType.DATE,         0, false);
            MODEL_BINARY    = addColumn    ("MODEL_BINARY",     DataType.BLOB,         0, false);
            MODEL_XML       = addColumn    ("MODEL_XML",        DataType.CLOB,         0, false);
            SALES_INFO      = addColumn    ("SALES_INFO",       DataType.VARCHAR,     80, false); 
            UPDATE_TIMESTAMP= addTimestamp ("UPDATE_TIMESTAMP");
            
            // Primary Key (automatically set due to addIdentity()) otherwise use 
            // setPrimaryKey(...);
        }
    }

    /**
     * This class represents the Dealer table.
     */
    public static class Dealer extends TTable<CarSalesDB>
    {
        public final DBTableColumn ID;
        public final DBTableColumn COMPANY_NAME;
        public final DBTableColumn STREET;
        public final DBTableColumn CITY;
        public final DBTableColumn COUNTRY;
        public final DBTableColumn YEAR_FOUNDED;
        public final DBTableColumn RATING;
        public final DBTableColumn UPDATE_TIMESTAMP;

        public Dealer(CarSalesDB db)
        {
            super("DEALER", db);
            
            // ID
            ID              = addIdentity ("ID",               "DEALER_ID_SEQ"); // Optional Sequence name for some DBMS (e.g. Oracle)
            COMPANY_NAME    = addColumn   ("COMPANY_NAME",     DataType.VARCHAR,     40, true);
            STREET          = addColumn   ("ADDRESS",          DataType.VARCHAR,     40, false);
            CITY            = addColumn   ("CITY",             DataType.VARCHAR,     20, true);
            COUNTRY         = addColumn   ("COUNTRY",          DataType.VARCHAR,     40, true);
            YEAR_FOUNDED    = addColumn   ("YEAR_FOUNDED",     DataType.DECIMAL,    4.0, false);
            RATING          = addColumn   ("RATING",           DataType.CHAR,         1, true, DealerRating.X);
            UPDATE_TIMESTAMP= addTimestamp("UPDATE_TIMESTAMP");
            
            // Primary Key (automatically set due to addIdentity()) otherwise use 
            // setPrimaryKey(...);
        }
    }

    /**
     * This class represents the Relationship between Brands and Dealers
     */
    public static class DealerBrands extends TTable<CarSalesDB>
    {
        public final DBTableColumn DEALER_ID;
        public final DBTableColumn WMI;
        public final DBTableColumn DEALERSHIP_TYPE;
        public final DBTableColumn YEAR_BEGIN;

        public DealerBrands(CarSalesDB db)
        {
            super("DEALER_BRANDS", db);
            
            // Key columns
            DEALER_ID       = addForeignKey("DEALER_ID", db.DEALER,  true,  true );  // Delete Cascade on
            WMI             = addForeignKey("WMI",       db.BRAND,   true,  false);  // Delete Cascade off
            // Data columns
            DEALERSHIP_TYPE = addColumn("DEALERSHIP_TYPE",   DataType.CHAR,      1, true, DealershipType.class);
            YEAR_BEGIN      = addColumn("YEAR_BEGIN",        DataType.DECIMAL, 4.0, false);
            // Primary Key
            setPrimaryKey(DEALER_ID, WMI);
        }
    }

    /**
     * This class represents the Sales table.
     */
    public static class Sales extends TTable<CarSalesDB>
    {
        public final DBTableColumn MODEL_ID;
        public final DBTableColumn DEALER_ID;
        public final DBTableColumn YEAR;
        public final DBTableColumn MONTH;
        public final DBTableColumn CAR_COLOR;
        public final DBTableColumn PRICE;

        public Sales(CarSalesDB db)
        {
            super("SALES", db);
            
            // ID
            MODEL_ID        = addForeignKey("MODEL_ID",  db.MODEL,  true, false);  // Delete Cascade off
            DEALER_ID       = addForeignKey("DEALER_ID", db.DEALER, true, true );  // Delete Cascade on
            YEAR            = addColumn("YEAR",             DataType.DECIMAL,    4.0, true);
            MONTH           = addColumn("MONTH",            DataType.DECIMAL,    2.0, true);
            CAR_COLOR       = addColumn("CAR_COLOR",        DataType.VARCHAR,     20, false);
            PRICE           = addColumn("PRICE",            DataType.DECIMAL,    8.2, true);

            /*
             *  No primary key!
             */
        }
    }
    
    // Declare all Tables and Views here
    public final Brand           BRAND             = new Brand(this);
    public final Model           MODEL             = new Model(this);
    public final Dealer          DEALER            = new Dealer(this);
    public final DealerBrands    DEALER_BRANDS     = new DealerBrands(this);
    public final Sales           SALES             = new Sales(this);
    // Views
    public final DealerSalesView DEALER_SALES_VIEW = new DealerSalesView(this);

    // Flag indicating whether Database was newly created
    private boolean wasCreated;

    /**
     * Constructor of the SampleDB data model
     * Put all foreign key relations here, which have not already been defined by addForeignKey()
     */
    public CarSalesDB()
    {
        // Define additional Foreign-Key Relations here
        // e.g. Multicolum etc.
        // addRelation( SALES.MODEL_ID.referenceOn( MODEL.ID )
        //            , SALES.MODEL_ID.referenceOn( target ));
        log.info("CarSalesDB has been created with {} Tables, {} Views and {} Relations", getTables().size(), getViews().size(), getRelations().size());
    }
    
    /**
     * Returns whether or not the Database was created on opening(true) or whether it already existed (false)
     * @return true if the database was newly created or false otherwise
     */
    public boolean wasCreated()
    {
        return wasCreated;
    }

    @Override
    public void open(DBContext context)
    {
        // Check exists
        if (checkExists(context))
        {   // attach to driver
            super.open(context);
            // remember
            wasCreated = false;
            // yes, it exists, then check the model
            checkDataModel(context);
        } 
        else
        {   
        	DBMSHandler dbms = context.getDbms();
        	// PostgreSQL does not support DDL in transaction
            if(dbms instanceof DBMSHandlerPostgreSQL)
                setAutoCommit(context, true);
            // create the database
            createDatabase(context);
            // PostgreSQL does not support DDL in transaction
            if(dbms instanceof DBMSHandlerPostgreSQL)
                setAutoCommit(context, false);
            // attach to driver
            super.open(context);
            // populate
            wasCreated = true;
            // Commit
            context.commit();
        }
    }

    private void createDatabase(DBContext context)
    {
        // create DDL for Database Definition
        DBSQLScript script = new DBSQLScript(context);
        getCreateDDLScript(script);
        // Show DDL Statement
        log.info(script.toString());
        // Execute Script
        script.executeAll(false);
    }
    
    private void checkDataModel(DBContext context)
    {   try {
            DBModelChecker modelChecker = context.getDbms().createModelChecker(this);
            // Check data model   
            log.info("Checking DataModel for {} using {}", getClass().getSimpleName(), modelChecker.getClass().getSimpleName());
            // dbo schema
            DBModelErrorLogger logger = new DBModelErrorLogger();
            modelChecker.checkModel(this, context.getConnection(), logger);
            // show result
            log.info("Data model check done. Found {} errors and {} warnings.", logger.getErrorCount(), logger.getWarnCount());
        } catch(Exception e) {
            log.error("FATAL error when checking data model. Probably not properly implemented by DBMSHandler!");
        }
    }
    
    private void setAutoCommit(DBContext context, boolean enable)
    {   try {
            context.getConnection().setAutoCommit(enable);
        } catch (SQLException e) {
            log.error("Unable to set AutoCommit on Connection", e);
        }
    }   
}
