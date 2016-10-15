/*******************************************************************************
 * Copyright (c) 2011 neXtep Software and contributors.
 * All rights reserved.
 *
 * This file is part of neXtep designer.
 *
 * NeXtep designer is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 *
 * NeXtep designer is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.sqlgen.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.text.IAutoEditStrategy;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.sqlgen.model.IPrototype;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface ISQLParser {

	static final String DDL = "__sql_ddl"; //$NON-NLS-1$
	static final String LANG = "__sql_lang"; //$NON-NLS-1$
	static final String DML = "__sql_dml"; //$NON-NLS-1$
	static final String FUNC = "__sql_func"; //$NON-NLS-1$
	static final String VAR = "__sql_var"; //$NON-NLS-1$
	static final String DATATYPE = "__sql_datatype"; //$NON-NLS-1$
	static final String SPECIAL = "__sql_special"; //$NON-NLS-1$

	/**
	 * @return a collection of all tokens handled by the parser hashed by their token types
	 *         referring the type definition of the SQLScanner.
	 */
	Map<String, List<String>> getTypedTokens();

	/**
	 * @return the sequence of characters which precedes variables referenced in SQL text
	 */
	String getVarSeparator();

	/**
	 * @return the character which starts and ends a new string
	 */
	char getStringDelimiter();

	/**
	 * @return the String which can concatenate 2 strings for this vendor
	 */
	String getStringConcatenator();

	/**
	 * @return the edit strategies which should be activated when parsing with this vendor.
	 */
	Collection<IAutoEditStrategy> getAutoEditStrategies();

	/**
	 * @return a list of prototypes available from this parser.
	 */
	List<IPrototype> getPrototypes();

	/**
	 * Returns the vendor specific command to display comments in SQL script's output.
	 * 
	 * @return the prompt command to pass to SQL client for displaying comments
	 */
	String getPromptCommand();

	/**
	 * Returns the sequence of characters that marks the end of a SQL statement.
	 * 
	 * @return the SQL statement separator (generally ";")
	 */
	String getStatementDelimiter();

	/**
	 * @return the character string to escape reserved column names
	 */
	String getColumnDefinitionEscaper();

	/**
	 * Retrieves the vendor specific tag which could be used to invoke SQL scripts within other SQL
	 * scripts.
	 * 
	 * @return the vendor-specific tag to invoke other scripts
	 */
	String getScriptCallerTag();

	/**
	 * Returns the vendor specific command to exit from a SQL script.
	 * 
	 * @return the vendor specific command to exit a SQL script.
	 */
	String getExitCommand();

	/**
	 * Returns the vendor specific start-comment sequence in a SQL script.
	 * 
	 * @return the vendor specific start-comment sequence.
	 */
	String getCommentStartSequence();

	/**
	 * Returns the vendor specific command to show the errors that resulted from the last executed
	 * statement.
	 * 
	 * @return the vendor specific command to show the errors.
	 */
	String getShowErrorsCommand();

	/**
	 * Formats a value as a string which can be added to a regular SQL script. The returned string
	 * might be an expression which can generate the appropriate value. For example, if
	 * <code>type</code> is a date and value a string, the returned string might look like (for
	 * Oracle) :<br>
	 * <br>
	 * <code>to_date('01/02/2003','DD/MM/YYYY')</code><br>
	 * <br>
	 * as this expression could be added to a SQL statement to generate a date.
	 * 
	 * @param type the expected datatype which should be generated
	 * @param value the value expressed as an object
	 * @return a SQL-expression which can generate a SQL expression that provides the expected
	 *         datatype from the given value
	 */
	String formatSqlScriptValue(IDatatype type, Object value);

}
