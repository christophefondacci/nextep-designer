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
import com.nextep.datadesigner.dbgm.impl.SynchedVersionable;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IVariable;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.sqlgen.model.IPackage;

/**
 * @author Christophe Fondacci
 */
public abstract class Package extends SynchedVersionable<IPackage> implements IPackage {

	private String sourceCode;
	private String specCode;
	protected Collection<IVariable> variables;

	public Package(String name) {
		nameHelper.setFormatter(IFormatter.UPPERCASE);
		setName(name);
		setBodySourceCode("");
		variables = new ArrayList<IVariable>();
	}

	public Package() {
		nameHelper.setFormatter(IFormatter.UPPERCASE);
		variables = new ArrayList<IVariable>();
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IPackage#getProcedures()
	 */
	@Override
	public abstract Collection<IProcedure> getProcedures();

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IPackage#getBodySourceCode()
	 */
	@Override
	public String getBodySourceCode() {
		return sourceCode;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IPackage#setBodySourceCode(java.lang.String)
	 */
	@Override
	public void setBodySourceCode(String sourceCode) {
		final String before = this.sourceCode;
		this.sourceCode = sourceCode;
		notifyIfChanged(before, sourceCode, ChangeEvent.SOURCE_CHANGED, sourceCode);
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IPackage#addVariable(com.nextep.datadesigner.dbgm.model.IVariable)
	 */
	@Override
	public void addVariable(IVariable var) {
		variables.add(var);
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IPackage#getVariables()
	 */
	@Override
	public Collection<IVariable> getVariables() {
		if (!isParsed()) {
			DBGMHelper.parse(this);
		}
		return variables;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IPackage#getSpecSourceCode()
	 */
	@Override
	public String getSpecSourceCode() {
		return specCode;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IPackage#setSpecSourceCode(java.lang.String)
	 */
	@Override
	public void setSpecSourceCode(String spec) {
		final String before = this.specCode;
		this.specCode = spec;
		notifyIfChanged(before, spec, ChangeEvent.SOURCE_CHANGED, spec);
	}

	@Override
	public void clearVariables() {
		variables.clear();
	}
}
