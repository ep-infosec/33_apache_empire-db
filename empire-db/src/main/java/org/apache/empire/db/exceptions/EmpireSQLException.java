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
package org.apache.empire.db.exceptions;

import java.sql.SQLException;

import org.apache.empire.commons.ErrorType;
import org.apache.empire.db.DBObject;
import org.apache.empire.dbms.DBMSHandler;
import org.apache.empire.exceptions.EmpireException;

public class EmpireSQLException extends EmpireException
{
    private static final long serialVersionUID = 1L;
    
    public static final ErrorType errorType = new ErrorType("error.db.sqlException", "The database operation failed. Native error is: {0}");
    
    private final String nativeErrorMessage;

    protected static String messageFromSQLException(DBMSHandler dbms, SQLException sqle)
    {   // Set the error Message
        return (dbms!=null ? dbms.extractErrorMessage(sqle) : sqle.getMessage());
    }

	protected static DBMSHandler handlerFromObject(DBObject obj)
    {   // Set the error Message
        return (obj.getDatabase()!=null ? obj.getDatabase().getDbms() : (DBMSHandler)null);
    }
    
    public EmpireSQLException(DBMSHandler dbms, SQLException cause)
    {
        super(errorType, new String[] { messageFromSQLException(dbms, cause) }, cause );
        nativeErrorMessage = this.getErrorParams()[0];
    }
    
    public EmpireSQLException(DBObject obj, SQLException cause)
    {
        this(handlerFromObject(obj), cause);
    }
    
    // Derived classes only
    protected EmpireSQLException(ErrorType type, String[] params, int nativeErrorIndex, SQLException cause)
    {
        super(type, params, cause);
        nativeErrorMessage = this.getErrorParams()[nativeErrorIndex];
    }

    public String getNativeErrorMessage() 
    {
		return nativeErrorMessage;
	}
    
}
