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
package com.nextep.designer.sqlgen.oracle.generator;

import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.oracle.model.IOracleSynonym;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * An Oracle specific generator to generate different kinds of scripts in order
 * to create, alter and drop a synonym in an Oracle database.
 * 
 * @see <a
 *      href="http://download.oracle.com/docs/cd/B19306_01/server.102/b14200/statements_7001.htm#SQLRF01401">Oracle
 *      10gR2 Database SQL Reference: CREATE SYNONYM</a>
 * @author Bruno Gautier
 */
public class OracleSynonymGenerator extends SQLGenerator {

	private static final String OPE_ALTER = "Altering"; //$NON-NLS-1$
	private static final String OPE_CREATE = "Creating"; //$NON-NLS-1$

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		IOracleSynonym oldSyn = (IOracleSynonym) result.getTarget();
		IOracleSynonym newSyn = (IOracleSynonym) result.getSource();

		/*
		 * FIXME [BGA]: The OR REPLACE clause cannot be used for a type synonym
		 * that has any dependent tables or dependent valid user-defined object
		 * types. The model of a synonym should be changed to define the type of
		 * the database object that is referenced (by reference or by value).
		 * With this information, the ALTER script can be replaced by a
		 * DROP/CREATE script when needed.
		 */

		IGenerationResult diffResult = null;
		/*
		 * If the accessibility (public/private) of the synonym has changed, we
		 * need to generate a DROP/CREATE script, otherwise we generate an alter
		 * script.
		 */
		if (oldSyn.isPublic() ^ newSyn.isPublic()) {
			// Generate the DROP script.
			diffResult = doDrop(oldSyn);
			// Generate a CREATE script and append it to the previous script.
			diffResult.integrate(generateFullSQL(newSyn));
		} else {
			/*
			 * Whatever changes have been made to the synonym, we must generate
			 * a full "CREATE OR REPLACE" script to apply those changes to the
			 * synonym definition.
			 */
			ISQLScript alterScript = generateCreateOrReplaceScript(newSyn, OPE_ALTER);

			diffResult = GenerationFactory.createGenerationResult();
			diffResult.addUpdateScript(new DatabaseReference(newSyn.getType(), newSyn.getName()),
					alterScript);
		}

		return diffResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IOracleSynonym syn = (IOracleSynonym) model;
		final String synName = getName(syn);
		final String rawName = syn.getName();

		ISQLScript dropScript = new SQLScript(rawName, syn.getDescription(),
				prompt("Dropping synonym '" + rawName + "'..."), ScriptType.SYNONYM); //$NON-NLS-1$ //$NON-NLS-2$

		dropScript.appendSQL("DROP "); //$NON-NLS-1$

		// Dropping a public synonym requires the DROP PUBLIC SYNONYM system
		// privilege.
		if (syn.isPublic())
			dropScript.appendSQL("PUBLIC "); //$NON-NLS-1$

		dropScript.appendSQL("SYNONYM ").appendSQL(synName); //$NON-NLS-1$

		closeLastStatement(dropScript);

		IGenerationResult generation = GenerationFactory.createGenerationResult();
		generation.addDropScript(new DatabaseReference(syn.getType(), rawName), dropScript);

		return generation;
	}

	/*
	 * Creating a private synonym requires the CREATE SYNONYM system privilege.
	 */
	@Override
	public IGenerationResult generateFullSQL(Object model) {
		IOracleSynonym synonym = (IOracleSynonym) model;

		ISQLScript additionScript = generateCreateOrReplaceScript(synonym, OPE_CREATE);

		IGenerationResult generation = GenerationFactory.createGenerationResult();
		generation.addAdditionScript(new DatabaseReference(synonym.getType(), synonym.getName()),
				additionScript);

		return generation;
	}

	/**
	 * A helper method to generate a "CREATE OR REPLACE" script which can then
	 * be used for update or addition scripts. The specified
	 * <code>operationType</code> is used to define if the "OR REPLACE" clause
	 * is needed, and the label to be appended in the script right after the
	 * <code>Prompt</code> statement.
	 * 
	 * @param synonym
	 *            the synonym to alter or create.
	 * @param operationType
	 *            a string representing the kind of operation realized on the
	 *            synonym, must correspond to one of the two private constants
	 *            OPE_ALTER and OPE_CREATE defined in this Generator.
	 * @return an <code>ISQLScript</code> representing a script to create a
	 *         synonym or alter an existing synonym.
	 */
	private ISQLScript generateCreateOrReplaceScript(IOracleSynonym synonym, String operationType) {
		String synName = getName(synonym);
		String synRefDbObjSchemaName = synonym.getRefDbObjSchemaName();
		String synRefDbObjDbLinkName = synonym.getRefDbObjDbLinkName();

		ISQLScript script = new SQLScript(synName, synonym.getDescription(), prompt(operationType
				+ " synonym '" + synName + "'..."), ScriptType.SYNONYM); //$NON-NLS-1$ //$NON-NLS-2$

		script.appendSQL("CREATE "); //$NON-NLS-1$

		if (operationType.equals(OPE_ALTER))
			script.appendSQL("OR REPLACE "); //$NON-NLS-1$

		// Creating a public synonym requires the CREATE PUBLIC SYNONYM system
		// privilege.
		if (synonym.isPublic())
			script.appendSQL("PUBLIC "); //$NON-NLS-1$

		script.appendSQL("SYNONYM ").appendSQL(synName).appendSQL(NEWLINE); //$NON-NLS-1$

		/*
		 * TODO [BGA]: The indentation of the script could be handled in the
		 * user preferences panel to specify if the generated script should be
		 * indented, the size of the indentation and the type of characters used
		 * (tabs or spaces).
		 */
		script.appendSQL("    FOR "); //$NON-NLS-1$

		if (synRefDbObjSchemaName != null && !"".equals(synRefDbObjSchemaName)) //$NON-NLS-1$
			script.appendSQL(synRefDbObjSchemaName).appendSQL("."); //$NON-NLS-1$

		script.appendSQL(synonym.getRefDbObjName());

		if (synRefDbObjDbLinkName != null && !"".equals(synRefDbObjDbLinkName)) //$NON-NLS-1$
			script.appendSQL("@").appendSQL(synRefDbObjDbLinkName); //$NON-NLS-1$

		closeLastStatement(script);

		return script;
	}

}
