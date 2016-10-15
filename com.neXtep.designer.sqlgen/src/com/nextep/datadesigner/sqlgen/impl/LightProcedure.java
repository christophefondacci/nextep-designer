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
package com.nextep.datadesigner.sqlgen.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IProcedureContainer;
import com.nextep.datadesigner.dbgm.model.IProcedureParameter;
import com.nextep.datadesigner.dbgm.model.IVariable;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.SynchStatus;
import com.nextep.datadesigner.model.UID;

/**
 * The procedure default implementation which can represent either a procedure or a function.
 * 
 * @author Christophe Fondacci
 */
public class LightProcedure extends NamedObservable implements IProcedure {

	/** Procedure's parameters */
	private List<IProcedureParameter> parameters;
	/** Procedure variables */
	private List<IVariable> variables;
	/** Procedure's returned type (for functions */
	private IDatatype returnedType;
	/** Procedure's parsed header */
	private String header = null;
	/** Parent container of this procedure */
	private IProcedureContainer parent;

	public LightProcedure(String name, IDatatype returnedType) {
		nameHelper.setFormatter(IFormatter.UPPERCASE);
		setName(name);
		setReturnType(returnedType);
		// Initializing collections
		parameters = new ArrayList<IProcedureParameter>();
		variables = new ArrayList<IVariable>();
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedure#getHeader()
	 */
	@Override
	public String getHeader() {
		if (header != null)
			return header;
		StringBuffer b = new StringBuffer();
		boolean isFunction = (getReturnType() != null);
		b.append((isFunction ? "function " : "procedure ") + getName() + " ("); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		boolean isFirst = true;
		for (IProcedureParameter p : getParameters()) {
			if (!isFirst) {
				b.append(", "); //$NON-NLS-1$
			} else {
				isFirst = false;
			}
			b.append(p.getName());
			b.append(" " + p.getParameterType().name() + " "); //$NON-NLS-1$ //$NON-NLS-2$
			b.append(p.getDatatype().getName());
		}
		if (isFunction) {
			b.append(") return " + getReturnType().getName() + ";"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			b.append(");"); //$NON-NLS-1$
		}

		return b.toString();
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedure#getParameters()
	 */
	@Override
	public List<IProcedureParameter> getParameters() {
		return parameters;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedure#getReturnType()
	 */
	@Override
	public IDatatype getReturnType() {
		return returnedType;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedure#setParameters(java.util.List)
	 */
	@Override
	public void setParameters(List<IProcedureParameter> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedure#setReturnType(com.nextep.datadesigner.dbgm.model.IDatatype)
	 */
	@Override
	public void setReturnType(IDatatype datatype) {
		this.returnedType = datatype;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedure#addParameter(com.nextep.datadesigner.dbgm.model.IProcedureParameter)
	 */
	@Override
	public void addParameter(IProcedureParameter param) {
		parameters.add(param);

	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IVariableContainer#addVariable(com.nextep.datadesigner.dbgm.model.IVariable)
	 */
	@Override
	public void addVariable(IVariable var) {
		variables.add(var);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IVariableContainer#getVariables()
	 */
	@Override
	public Collection<IVariable> getVariables() {
		return variables;
	}

	@Override
	public String getSQLSource() {
		throw new ErrorException("Unsupported: lightProcedure does not handle source code"); //$NON-NLS-1$
	}

	@Override
	public void setSQLSource(String sql) {
		throw new ErrorException("Unsupported: lightProcedure does not handle source code"); //$NON-NLS-1$
	}

	@Override
	public void setSql(String sql) {
		throw new ErrorException("Unsupported: lightProcedure does not handle source code"); //$NON-NLS-1$
	}

	@Override
	public LanguageType getLanguageType() {
		throw new ErrorException("Unsupported: lightProcedure does not handle language types"); //$NON-NLS-1$
	}

	@Override
	public void setLanguageType(LanguageType language) {
		throw new ErrorException("Unsupported: lightProcedure does not handle language types"); //$NON-NLS-1$
	}

	@Override
	public IParseData getParseData() {
		throw new ErrorException("Unsupported: lightProcedure does not handle parsing"); //$NON-NLS-1$
	}

	@Override
	public boolean isParsed() {
		return false;
	}

	@Override
	public void setParsed(boolean parsed) {
		throw new ErrorException("Unsupported: lightProcedure does not handle parsing"); //$NON-NLS-1$
	}

	@Override
	public void setParseData(IParseData parseData) {

	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(IProcedure.TYPE_ID);
	}

	@Override
	public void setHeader(String header) {
		this.header = header;
	}

	@Override
	public SynchStatus getSynchStatus() {
		return SynchStatus.UNKNOWN;
	}

	@Override
	public void setSynched(SynchStatus synched) {
	}

	@Override
	public UID getUID() {
		throw new ErrorException("Unsupported: lightProcedure does not handle persistency"); //$NON-NLS-1$
	}

	@Override
	public void setUID(UID id) {
		throw new ErrorException("Unsupported: lightProcedure does not handle persistency"); //$NON-NLS-1$

	}

	@Override
	public String getSql() {
		return getSQLSource();
	}

	@Override
	public IProcedure getModel() {
		return this;
	}

	@Override
	public void lockUpdates() {
	}

	@Override
	public void unlockUpdates() {
	}

	@Override
	public boolean updatesLocked() {
		return false;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LightProcedure) {
			final LightProcedure p = (LightProcedure) obj;
			return getName().equals(p.getName())
					&& getParameters().size() == p.getParameters().size();
		}
		return false;
	}

	@Override
	public void setParent(IProcedureContainer parent) {
		this.parent = parent;
	}

	@Override
	public IProcedureContainer getParent() {
		return parent;
	}

	@Override
	public void clearVariables() {
		variables.clear();
	}
}
