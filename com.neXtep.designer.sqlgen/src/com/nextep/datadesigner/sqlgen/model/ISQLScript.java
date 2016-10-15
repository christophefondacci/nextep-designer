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
package com.nextep.datadesigner.sqlgen.model;

import java.math.BigDecimal;
import com.nextep.datadesigner.dbgm.model.ISqlBased;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.model.ICheckedObject;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface ISQLScript extends ITypedObject, IdentifiedObject, INamedObject, IObservable,
		IReferenceable, ICheckedObject, ISqlBased {

	public static final String TYPE_ID = "SQL_SCRIPT"; //$NON-NLS-1$

	/**
	 * Appends the specified SQL text to the existing script content.
	 * 
	 * @param s SQL text to add to the script.
	 * @return this script.
	 */
	public ISQLScript appendSQL(String s);

	/**
	 * Appends the specified char to the existing script content.
	 * 
	 * @param c a char to add to the script.
	 * @return this script.
	 */
	public ISQLScript appendSQL(char c);

	/**
	 * Appends the specified <code>BigDecimal</code> to the existing script content.
	 * 
	 * @param d a <code>BigDecimal</code> to add to the script.
	 * @return this script.
	 */
	public ISQLScript appendSQL(BigDecimal d);

	/**
	 * Appends the specified <code>Long</code> to the existing script content.
	 * 
	 * @param l a <code>Long</code> to add to the script.
	 * @return this script.
	 */
	public ISQLScript appendSQL(Long l);

	/**
	 * Appends the specified int to the existing script content.
	 * 
	 * @param i a int to add to the script.
	 * @return this script.
	 */
	public ISQLScript appendSQL(int i);

	/**
	 * Appends the specified script to this script contents.
	 * 
	 * @param script script to append to the current one
	 */
	public void appendScript(ISQLScript script);

	/**
	 * @return the script type
	 * @see ScriptType
	 */
	public ScriptType getScriptType();

	/**
	 * Defines the script type
	 * 
	 * @param scriptType script type
	 * @see ScriptType
	 */
	public void setScriptType(ScriptType scriptType);

	/**
	 * Returns the filename of this SQL script, which corresponds to the concatenation of the name
	 * of this script with its file extension separated by a dot.
	 * 
	 * @return the filename of this script
	 */
	public String getFilename();

	/**
	 * @return the file directory (only for external scripts)
	 */
	public String getDirectory();

	/**
	 * Returns the absolute pathname of this SQL script, which corresponds to the concatenation the
	 * directory of this script with its filename separated by the system-dependent default
	 * name-separator character.
	 * 
	 * @return the absolute pathname of this script (only for external scripts)
	 */
	public String getAbsolutePathname();

	/**
	 * Sets the directory of this SQLScript
	 * 
	 * @param directory file directory
	 */
	public void setDirectory(String directory);

	/**
	 * @return <code>true</code> for filesystem scripts, <code>false</code> for repository scripts
	 */
	public boolean isExternal();

	/**
	 * Defines if the script comes from the filesystem (external = <code>true</code>) or from the
	 * repository (external = <code>false</code>)
	 * 
	 * @param isExternal externality of this script
	 */
	public void setExternal(boolean isExternal);

	public void setId(long id);

	public long getId();
}
