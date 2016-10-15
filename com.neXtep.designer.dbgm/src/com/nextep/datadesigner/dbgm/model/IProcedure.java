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
package com.nextep.datadesigner.dbgm.model;

import java.util.List;

/**
 * This class represents a database procedure.
 * 
 * @author Christophe Fondacci
 */
public interface IProcedure extends IDatabaseObject<IProcedure>, IVariableContainer, IParseable,
		ISqlBased {

	static final String TYPE_ID = "PROCEDURE"; //$NON-NLS-1$

	/**
	 * @return the collection of procedure's parameters
	 */
	List<IProcedureParameter> getParameters();

	/**
	 * Defines the collection of procedure's parameters
	 * 
	 * @param parameters
	 */
	void setParameters(List<IProcedureParameter> parameters);

	void addParameter(IProcedureParameter param);

	/**
	 * @return the datatype returned by this procedure (if a function) or <code>null</code> for a
	 *         standalone procedure.
	 */
	IDatatype getReturnType();

	/**
	 * Defines the datatype of this procedure.
	 * 
	 * @param datatype procedure returned datatype
	 */
	void setReturnType(IDatatype datatype);

	/**
	 * @return the header of this procedure (declaration).
	 */
	String getHeader();

	/**
	 * Defines the header of this procedure.
	 * 
	 * @param header new procedure header (may require a parse)
	 */
	void setHeader(String header);

	/**
	 * @return the full SQL source code of this procedure
	 */
	String getSQLSource();

	/**
	 * Defines the full SQL source code of this procedure
	 * 
	 * @param sql source code of this procedure
	 */
	void setSQLSource(String sql);

	/**
	 * Defines the language in which is written the source code of this procedure.
	 * 
	 * @param language a language type enumeration
	 */
	void setLanguageType(LanguageType language);

	/**
	 * @return the language in which the source code of this procedure is written.
	 */
	LanguageType getLanguageType();

	/**
	 * Defines the parent of this {@link IProcedure}, should there be any. This method should
	 * typically be called when a procedure is inside a package.
	 * 
	 * @param parent parent {@link IProcedureContainer}
	 */
	void setParent(IProcedureContainer parent);

	/**
	 * Retrieves the parent container of this procedure. A procedure has a parent when located
	 * within a package or user-type, etc.
	 * 
	 * @return the parent {@link IProcedureContainer} of this procedure or <code>null</code> if none
	 */
	IProcedureContainer getParent();
}
