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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.sqlgen.helpers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseRawObject;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ISqlBased;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Provides common facilities useful to generators.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public final class GenerationHelper {

	private static final Log LOGGER = LogFactory.getLog(GenerationHelper.class);

	private GenerationHelper() {
	}

	/**
	 * Adds table precondition to the generation result by analyzing the
	 * provided table dependencies through foreign keys and by resolving the
	 * referenced remote table.
	 * 
	 * @param result
	 *            the {@link IGenerationResult} to add preconditions to
	 * @param table
	 *            the table being generated
	 */
	public static void addForeignKeyPreconditions(IGenerationResult result, IBasicTable table) {
		for (IKeyConstraint key : table.getConstraints()) {
			switch (key.getConstraintType()) {
			case FOREIGN:
				final ForeignKeyConstraint fk = (ForeignKeyConstraint) key;
				// Adds foreign key precondition
				result.addPrecondition(new DatabaseReference(fk.getRemoteConstraint().getType(), fk
						.getRemoteConstraint().getName(), fk.getRemoteConstraint()
						.getConstrainedTable().getName()));
				// Adds enforcing index precondition
				for (IDatabaseRawObject dbObj : fk.getEnforcingIndex()) {
					result.addPrecondition(new DatabaseReference(dbObj.getType(), dbObj.getName()));
				}
				// Adds table precondition
				final IBasicTable remoteTable = DBGMHelper.getRemoteTable(fk);
				if (remoteTable != null) {
					final DatabaseReference remoteTableRef = new DatabaseReference(
							remoteTable.getType(), remoteTable.getName());
					if (!result.getPreconditions().contains(remoteTableRef)) {
						result.addPrecondition(remoteTableRef);
						// Transitivity
						addForeignKeyPreconditions(result, remoteTable);
					}
				}
			}
		}
	}

	/**
	 * Tries to remove from the specified SQL definition the last specified
	 * statement delimiter if present.
	 * 
	 * @param definition
	 *            a <code>String</code> representing a SQL definition of a
	 *            database object
	 * @param delimiter
	 *            the statement delimiter to remove from the end of the SQL
	 *            definition
	 * @return a <code>String</code> representing the cleaned SQL definition if
	 *         the specified delimiter has been found, the same SQL definition
	 *         otherwise
	 */
	public static String removeLastStatementDelimiter(String definition, String delimiter) {
		if (definition != null) {
			return definition.replaceFirst("\\s*" + delimiter + "\\s*$", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return definition;
	}

	/**
	 * Adds the precondition to a SQL based element being generated so that it
	 * gets all dependencies with other SQL based elements. This allows the
	 * generation service to sort the scripts properly according to their
	 * dependencies.
	 * 
	 * @param result
	 *            the {@link IGenerationResult} to add preconditions to
	 * @param sqlBased
	 *            the {@link ISqlBased} object being generated
	 * @param types
	 *            the type of objects for dependencies
	 * @return the list of {@link DatabaseReference} elements indicating the
	 *         objects that this SQL-based object depends to
	 */
	public static void addSqlGenerationPreconditions(IGenerationResult result, String name,
			ISqlBased sqlBased, IElementType... types) {

		// Extracting SQL
		final String sql = sqlBased.getSql();

		// Preparing resulting structure
		final List<DatabaseReference> dependencies = new ArrayList<DatabaseReference>();

		// Preparing default types if needed
		try {
			List<IVersionable<?>> elementsToCheck = new ArrayList<IVersionable<?>>();

			// Getting all element of workspace matching the type list
			for (IElementType type : types) {
				final List<IVersionable<?>> elements = VersionHelper.getAllVersionables(VCSPlugin
						.getViewService().getCurrentWorkspace(), type);
				elementsToCheck.addAll(elements);
			}

			for (IVersionable<?> element : elementsToCheck) {

				// Getting the name of the element
				String elementName = element.getName();

				// We process only if not current object
				if (!name.equals(elementName)) {

					// CLEANUP: Removing any suffixing arguments from name
					int index = elementName.indexOf('(');
					if (index >= 0) {
						elementName = elementName.substring(0, index);
					}

					if (sql.contains(elementName.trim())) {
						dependencies
								.add(new DatabaseReference(element.getType(), element.getName()));
					}
				}
			}
		} catch (RuntimeException e) {
			LOGGER.warn("Unable to compute preconditions for element " + name, e);
		}

		// Adding dependencies as preconditions
		for (DatabaseReference dependency : dependencies) {
			result.addPrecondition(dependency);
		}
	}
}
