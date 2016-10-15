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
package com.nextep.designer.sqlgen.services;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.model.IDropStrategy;
import com.nextep.datadesigner.sqlgen.model.IGenerationSubmitter;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.ISQLCommandWriter;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.model.ISqlScriptBuilder;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * This interface provides services dedicated to SQL generation.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface IGenerationService {

	/**
	 * Retrieves the {@link ISqlScriptBuilder} to use with the specified vendor.
	 * 
	 * @param vendor
	 *            the {@link DBVendor} to get the builder for
	 * @return a {@link ISqlScriptBuilder} implementation
	 */
	ISqlScriptBuilder getSqlScriptBuilder(DBVendor vendor);

	/**
	 * Generates the SQL script for the given object, if possible, for the given
	 * database vendor.
	 * 
	 * @param vendor
	 *            the database vendor to generate for, as a {@link DBVendor}
	 * @param listener
	 *            the {@link IGenerationListener} to notify about this task's
	 *            termination
	 * @param monitor
	 *            the {@link IProgressMonitor} to report progress to
	 * @param objects
	 *            objects to generate SQL for
	 */
	void generate(DBVendor vendor, IGenerationListener listener, IProgressMonitor monitor,
			ITypedObject... objects);

	/**
	 * Generates the SQL scripts for the given element from the provided initial
	 * version. The resulting script will migrate the element from initial
	 * version to target version.
	 * 
	 * @param vendor
	 *            the {@link DBVendor} to generate for
	 * @param listener
	 *            the {@link IGenerationListener} which should be notified of
	 *            the generation result
	 * @param monitor
	 *            the {@link IProgressMonitor} to report progress to
	 * @param fromVersion
	 *            the initial version to generate from
	 * @param toVersion
	 *            the version to generate to
	 */
	void generateIncrement(DBVendor vendor, IGenerationListener listener, IProgressMonitor monitor,
			IVersionInfo fromVersion, IVersionInfo toVersion);

	/**
	 * Generates increment for default vendor, see complete version
	 * {@link IGenerationService#generateIncrement(DBVendor, IGenerationListener, IProgressMonitor, IVersionInfo, IVersionInfo)}
	 */
	void generateIncrement(IGenerationListener listener, IProgressMonitor monitor,
			IVersionInfo fromVersion, IVersionInfo toVersion);

	/**
	 * Generates the SQL scripts for the given items, should it be database
	 * objects or comparison deltas and sends back (synchronously) the result of
	 * this generation.
	 * 
	 * @param <T>
	 * @param monitor
	 *            the {@link IProgressMonitor} to report progress to
	 * @param vendor
	 *            the database vendor to generate for
	 * @param scriptName
	 *            the name of the script to generate
	 * @param scriptDesc
	 *            the description of the script to generate
	 * @param items
	 *            the collection of {@link ITypedObject} elements to generate
	 * @return the {@link IGenerationResult} with information about generated
	 *         scripts.
	 */
	<T extends ITypedObject> IGenerationResult batchGenerate(IProgressMonitor monitor,
			DBVendor vendor, String scriptName, String scriptDesc, Collection<T> items);

	/**
	 * Generates the SQL script for the given object, if possible. Generated SQL
	 * will be dedicated for current database vendor
	 * 
	 * @param listener
	 *            the {@link IGenerationListener} to notify about this task's
	 *            termination
	 * @param monitor
	 *            the {@link IProgressMonitor} to report progress to
	 * @param objects
	 *            objects to generate SQL for
	 */
	void generate(IGenerationListener listener, IProgressMonitor monitor, ITypedObject... objects);

	/**
	 * This method returns the appropriate generation submitter, according to
	 * the current properties.
	 * 
	 * @param vendor
	 *            database vendor to submit to
	 * @return a {@link IGenerationSubmitter} which can deploy a
	 *         {@link ISQLScript} to any database connection of the specified
	 *         vendor
	 */
	IGenerationSubmitter getGenerationSubmitter(DBVendor vendor);

	/**
	 * Returns an appropriate SQL parser corresponding to the specified database
	 * vendor.
	 * 
	 * @param vendor
	 *            the database vendor for which this method must return a SQL
	 *            parser.
	 * @return a {@link ISQLParser}
	 * @see GeneratorFactory#getSQLParser(DBVendor)
	 */
	ISQLParser getSQLParser(DBVendor vendor);

	/**
	 * A convenience method to retrieve a SQL parser for the current database
	 * vendor.
	 * 
	 * @return a {@link ISQLParser}
	 * @see DBGMHelper#getCurrentVendor()
	 * @see #getSQLParser(DBVendor)
	 */
	ISQLParser getCurrentSQLParser();

	/**
	 * Returns the name of the native client binary corresponding to the
	 * specified database vendor. If no specific binary has been set by the user
	 * in the preferences, returns the database vendor default client name.
	 * 
	 * @return the generator's binary name configured in the preferences for the
	 *         specified database vendor if available, the default vendor client
	 *         name otherwise.
	 * @see DBVendor#getDefaultExecutableName()
	 */
	String getGeneratorBinaryName(DBVendor vendor);

	/**
	 * This methods returns the current drop strategy defined for the specified
	 * element type and current vendor. An exception is thrown when the given
	 * element type has no drop strategy defined.
	 * 
	 * @param type
	 *            type for which we want to know the strategy for drops
	 * @return the drop strategy
	 */
	IDropStrategy getDropStrategy(IElementType type);

	/**
	 * This methods returns the current drop strategy defined for the specified
	 * element type and vendor. An exception is thrown when the given element
	 * type has no drop strategy defined.
	 * 
	 * @param type
	 *            type for which we want to know the strategy for drops
	 * @param vendor
	 *            the database vendor for which the drop strategy needs to be
	 *            retrieved
	 * @return the drop strategy
	 */
	IDropStrategy getDropStrategy(IElementType type, DBVendor vendor);

	/**
	 * Retrieves the exhaustive list of available drop strategies for the
	 * specified type hashed by their id. This is a convenience method which
	 * defaults the database vendor to the default workspace vendor.
	 * 
	 * @param type
	 *            the {@link IElementType} for which the list of available
	 *            strategies should be retrieved
	 * @return a map of {@link IDropStrategy} hashed by their id
	 */
	Map<String, IDropStrategy> getAvailableDropStrategies(IElementType type);

	/**
	 * Retrieves the default drop strategy for the specified element type and
	 * vendor.
	 * 
	 * @param type
	 *            type to retrieve default strategy for
	 * @param vendor
	 *            the vendor to retrieve the default strategy for
	 * @return the default drop strategy
	 */
	IDropStrategy getDefaultDropStrategy(IElementType type, DBVendor vendor);

	/**
	 * Retrieves the default drop strategy for the specified element type and
	 * default vendor.
	 * 
	 * @param type
	 *            type to retrieve default strategy for
	 * @return the default drop strategy
	 */
	IDropStrategy getDefaultDropStrategy(IElementType type);

	/**
	 * Retrieves the exhaustive list of available drop strategies for the
	 * specified type hashed by their id.
	 * 
	 * @param type
	 *            the {@link IElementType} for which the list of available
	 *            strategies should be retrieved
	 * @param vendor
	 *            the vendor for which strategies need to be retrieved
	 * @return a map of {@link IDropStrategy} hashed by their id
	 */
	Map<String, IDropStrategy> getAvailableDropStrategies(IElementType type, DBVendor vendor);

	/**
	 * Returns an appropriate SQL command writer for the specified database
	 * vendor.
	 * 
	 * @param vendor
	 *            the database vendor for which this method must return a SQL
	 *            command writer
	 * @return a {@link ISQLCommandWriter}
	 */
	ISQLCommandWriter getSQLCommandWriter(DBVendor vendor);

	/**
	 * A convenience method to retrieve a SQL command writer for the current
	 * database vendor.
	 * 
	 * @return a {@link ISQLCommandWriter}
	 * @see #getSQLCommandWriter(DBVendor)
	 */
	ISQLCommandWriter getCurrentSQLCommandWriter();

	/**
	 * Provides the character sequences to apply for new lines in every SQL
	 * generation
	 * 
	 * @return the new line character sequence
	 */
	String getNewLine();

}
