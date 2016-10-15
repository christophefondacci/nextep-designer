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

import java.util.List;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public abstract class OraclePhysicalPropertiesGenerator extends SQLGenerator {

	/**
	 * Appends the SQL to the specified script if and only if the physical attribute value differ
	 * from source to target
	 * 
	 * @param script script to which the SQL code will be appended
	 * @param src source physical properties
	 * @param tgt target physical properties
	 * @param attr attribute
	 * @return a boolean indicating if something has been appended to the script
	 */
	protected boolean appendAttribute(ISQLScript script, IPhysicalProperties src,
			IPhysicalProperties tgt, PhysicalAttribute attr) {
		Integer srcVal = (Integer) src.getAttribute(attr);
		Integer tgtVal = (Integer) tgt.getAttribute(attr);
		if (srcVal != null) {
			if (!srcVal.equals(tgtVal)) {
				script.appendSQL(attr.getName()).appendSQL(" ").appendSQL(srcVal.toString()) //$NON-NLS-1$
						.appendSQL(" "); //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IPhysicalProperties props = (IPhysicalProperties) model;

		ISQLScript script = new SQLScript("", "", "", ScriptType.TABLE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		if (props.getTablespaceName() != null && !"".equals(props.getTablespaceName())) { //$NON-NLS-1$
			script.appendSQL("TABLESPACE ").appendSQL(props.getTablespaceName()).appendSQL("  "); //$NON-NLS-1$ //$NON-NLS-2$
		}

		for (PhysicalAttribute attr : PhysicalAttribute.values()) {
			final Object val = props.getAttribute(attr);
			if (val != null) {
				if (val instanceof Integer && ((Integer) val).intValue() != 0) {
					script.appendSQL(attr.getName()).appendSQL(" ").appendSQL(val.toString()) //$NON-NLS-1$
							.appendSQL("  "); //$NON-NLS-1$
				}
			}
		}
		script.appendSQL(props.isLogging() ? "LOGGING" : "NOLOGGING").appendSQL(NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$

		appendCustomSQL(script, props);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(props.getType(), props.getParent().getName()),
				script);

		// Returning result
		return r;
	}

	/**
	 * Extend this method to append SQL to the default base properties generation
	 * 
	 * @param script generated properties script
	 * @param props properties being generated
	 */
	protected void appendCustomSQL(ISQLScript script, IPhysicalProperties props) {
	}

	/**
	 * Adds all provided scripts to a source script, using commas to separate one script content
	 * from another. A header and footer may be provided which will encapsulate the script
	 * enumeration. This method is used to append several child scripts to a global ALTER SQL
	 * command.
	 * 
	 * @param source source script which will be modified by appending the scripts list.
	 * @param header header to append to the source script before appending the scripts list
	 * @param footer footer to append to the source script after appending the scripts list
	 * @param scripts scripts list to append to the source, using a comma to separate script
	 *        contents
	 */
	protected void addCommaSeparatedScripts(ISQLScript source, String header, String footer,
			List<ISQLScript> scripts) {
		if (scripts.size() == 0)
			return;
		boolean first = true;
		source.appendSQL(header);
		for (ISQLScript script : scripts) {
			// Adding format & comma on every item but the first
			if (!first) {
				source.appendSQL(","); //$NON-NLS-1$
			} else {
				source.appendSQL(" "); //$NON-NLS-1$
				first = false;
			}
			// Appending script
			source.appendSQL(script.getSql());
		}

		if (footer != null && !"".equals(footer)) { //$NON-NLS-1$
			source.appendSQL(footer);
		} else {
			source.appendSQL(NEWLINE);
		}
	}

}
