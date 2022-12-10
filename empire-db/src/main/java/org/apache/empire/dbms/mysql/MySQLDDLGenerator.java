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
package org.apache.empire.dbms.mysql;

import java.util.Iterator;

import org.apache.empire.commons.StringUtils;
import org.apache.empire.data.DataType;
import org.apache.empire.db.DBColumn;
import org.apache.empire.db.DBCombinedCmd;
import org.apache.empire.db.DBCommandExpr;
import org.apache.empire.db.DBDDLGenerator;
import org.apache.empire.db.DBDatabase;
import org.apache.empire.db.DBExpr;
import org.apache.empire.db.DBIndex;
import org.apache.empire.db.DBSQLBuilder;
import org.apache.empire.db.DBSQLScript;
import org.apache.empire.db.DBTable;
import org.apache.empire.db.DBTableColumn;
import org.apache.empire.db.DBView;
import org.apache.empire.dbms.DBMSHandlerBase.DBSeqTable;
import org.apache.empire.exceptions.NotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySQLDDLGenerator extends DBDDLGenerator<DBMSHandlerMySQL>
{
	private static final Logger log = LoggerFactory.getLogger(MySQLDDLGenerator.class);
	
    // Data types
    protected String DATATYPE_INT_TINY   = "TINYINT";  // Integer with really small size (1 byte)
    protected String DATATYPE_INT_MEDIUM = "MEDIUMINT";  // Integer with really medium size (3 byte)
	
    public MySQLDDLGenerator(DBMSHandlerMySQL dbms)
    {
        super(dbms);
        // Alter Column Phrase
        alterColumnPhrase  = " MODIFY ";
        // set Oracle specific data types
        initDataTypes();
    }

    /**
     * sets SQL-Server specific data types
     */
    private void initDataTypes()
    {   // Override data types
        DATATYPE_TIMESTAMP  = "DATETIME";
        DATATYPE_CLOB       = "LONGTEXT";
    }

    @Override
    protected boolean appendColumnDataType(DataType type, double size, DBTableColumn c, DBSQLBuilder sql)
    {
        switch (type)
        {
            case AUTOINC:
            { // Auto increment
                super.appendColumnDataType(type, size, c, sql);
                if (dbms.isUseSequenceTable()==false)
                    sql.append(" AUTO_INCREMENT");
                break;
            }
            case INTEGER:
            {   
            	int bytes = Math.abs((int) size);
            	if (bytes > 0 && bytes <= 1)
            	{
            		sql.append(DATATYPE_INT_TINY);
            	}
            	else if (bytes > 0 && bytes <= 2)
            	{
	                sql.append(DATATYPE_INT_SMALL);
            	}
            	else if (bytes > 0 && bytes <= 3)
            	{
	                sql.append(DATATYPE_INT_MEDIUM);
            	}
	            else if (bytes > 0 && bytes <= 4)
	            {
	                sql.append(DATATYPE_INTEGER);
	            }
	            else if (bytes > 0 && bytes <= 8)
	            {
	                sql.append(DATATYPE_INT_BIG);
	            }
	            else 
	            {	// Default
	                sql.append(DATATYPE_INTEGER);  // Default integer length
	            }
            	break;
            }
           default:
                // use default
                return super.appendColumnDataType(type, size, c, sql);
        }
        return true;
    }
     
    @SuppressWarnings("unused")
    @Override
    protected void createDatabase(DBDatabase db, DBSQLScript script)
    {
        // create Database if database name is supplied
        String databaseName = dbms.getDatabaseName();
        if (StringUtils.isNotEmpty(databaseName))
        { 
            // Create Database
            script.addStmt("CREATE DATABASE IF NOT EXISTS " + databaseName + " CHARACTER SET " + dbms.getCharacterSet());
            script.addStmt("USE " + databaseName);
            // appendDDLStmt(db, "SET DATEFORMAT ymd", buf);
            // Sequence Table
            if (dbms.isUseSequenceTable() && db.getTable(dbms.getSequenceTableName())==null)
                new DBSeqTable(dbms.getSequenceTableName(), db);
        }
        // default processing
        super.createDatabase(db, script);
    }

    /**
     * Appends the DDL-Script for creating the given table to an SQL-Script 
     * @param t the table to create
     * @param script the sql script to which to append the dll command(s)
     */
    @Override
    protected void createTable(DBTable t, DBSQLScript script)
    {
        DBSQLBuilder sql = dbms.createSQLBuilder();
        sql.append("-- creating table ");
        sql.append(t.getName());
        sql.append(" --\r\n");
        sql.append("CREATE TABLE ");
        t.addSQL(sql, DBExpr.CTX_FULLNAME);
        sql.append(" (");
        boolean addSeparator = false;
        Iterator<DBColumn> columns = t.getColumns().iterator();
        while (columns.hasNext())
        {
            DBTableColumn c = (DBTableColumn) columns.next();
            // Append column
            sql.append((addSeparator) ? ",\r\n   " : "\r\n   ");
            appendColumnDesc(c, false, sql);
            addSeparator = true;
        }
        // Primary Key
        DBIndex pk = t.getPrimaryKey();
        if (pk != null)
        { // add the primary key
            sql.append(",\r\n PRIMARY KEY (");
            addSeparator = false;
            // columns
            DBColumn[] keyColumns = pk.getColumns();
            for (int i = 0; i < keyColumns.length; i++)
            {
                sql.append((addSeparator) ? ", " : "");
                keyColumns[i].addSQL(sql, DBExpr.CTX_NAME);
                addSeparator = true;
            }
            sql.append(")");
        }
        sql.append(")");
        // Engine
        addSeparator = false;
        if (StringUtils.isNotEmpty(dbms.getEngine())) 
        {   // Set the table engine
            sql.append("\r\n ENGINE = ");
            sql.append(dbms.getEngine());
            addSeparator = true;
        }
        // Comment?
        if (StringUtils.isNotEmpty(t.getComment()))
        {   // Add the table comment
            if (addSeparator)
                sql.append(",");
            sql.append("\r\n COMMENT = '");
            sql.append(t.getComment());
            sql.append("'");
        }
        // Create the table
        addCreateTableStmt(t, sql, script);
        // Create all Indexes
        createTableIndexes(t, pk, script);        
    }
    
    /**
     * Appends the DDL-Script for creating the given view to an SQL-Script 
     * @param v the view to create
     * @param script the sql script to which to append the dll command(s)
     */
    @Override
    protected void createView(DBView v, DBSQLScript script)
    {
        // Create the Command
        DBCommandExpr cmd = v.createCommand();
        if (cmd==null)
        {   // Check whether Error information is available
            log.error("No command has been supplied for view " + v.getName());
            throw new NotSupportedException(this, v.getName() + ".createCommand");
        }
        // Make sure there is no OrderBy
        cmd.clearOrderBy();

        // Build String
        DBSQLBuilder sql = dbms.createSQLBuilder();
        sql.append( "CREATE VIEW ");
        v.addSQL(sql, DBExpr.CTX_FULLNAME);
        sql.append( " (" );
        boolean addSeparator = false;
        for(DBColumn c : v.getColumns())
        {
            if (addSeparator)
                sql.append(", ");
            // Add Column name
            c.addSQL(sql, DBExpr.CTX_NAME);
            // next
            addSeparator = true;
        }
        sql.append(")\r\nAS\r\n");
        if (cmd instanceof DBCombinedCmd)
        {
        	sql.append(cmd.getSelect());
        } else
        {
        	cmd.addSQL( sql, DBExpr.CTX_DEFAULT);
        }
        // done
        addCreateViewStmt(v, sql, script);
    }
    
}
