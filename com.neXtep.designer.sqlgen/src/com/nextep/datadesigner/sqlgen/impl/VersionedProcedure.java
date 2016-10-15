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
import com.nextep.datadesigner.dbgm.impl.SynchedVersionable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IProcedureContainer;
import com.nextep.datadesigner.dbgm.model.IProcedureParameter;
import com.nextep.datadesigner.dbgm.model.IVariable;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;

/**
 * This class represents a database stored procedure which is versioned in the repository.
 * 
 * @author Christophe
 */
public class VersionedProcedure extends SynchedVersionable<IProcedure> implements IProcedure,
		IParseable, IDatabaseObject<IProcedure> {

	private List<IProcedureParameter> params;
	private List<IVariable> vars;
	private IDatatype returnType;
	private String sql;
	private boolean isParsed = false;
	private IParseData parseData;
	private LanguageType language = LanguageType.STANDARD;
	private String header;
	private IProcedureContainer parent;

	public VersionedProcedure() {
		super();
		params = new ArrayList<IProcedureParameter>();
		vars = new ArrayList<IVariable>();
		nameHelper.setFormatter(IFormatter.NOFORMAT); // DBGMHelper.getCurrentVendor().getNameFormatter());
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(IProcedure.TYPE_ID);
	}

	@Override
	public void addParameter(IProcedureParameter param) {
		params.add(param);
	}

	@Override
	public String getHeader() {
		return header;
	}

	@Override
	public List<IProcedureParameter> getParameters() {
		return params;
	}

	@Override
	public IDatatype getReturnType() {
		return returnType;
	}

	@Override
	public String getSQLSource() {
		return sql;
	}

	@Override
	public void setParameters(List<IProcedureParameter> parameters) {
		this.params = parameters;
	}

	@Override
	public void setReturnType(IDatatype datatype) {
		this.returnType = datatype;
	}

	@Override
	public void setSQLSource(String sql) {
		this.sql = sql;
		this.isParsed = false;
		notifyListeners(ChangeEvent.SOURCE_CHANGED, null);
	}

	@Override
	public void addVariable(IVariable var) {
		vars.add(var);
	}

	@Override
	public Collection<IVariable> getVariables() {
		return vars;
	}

	@Override
	public IParseData getParseData() {
		return parseData;
	}

	@Override
	public boolean isParsed() {
		return isParsed;
	}

	@Override
	public LanguageType getLanguageType() {
		return language;
	}

	@Override
	public void setLanguageType(LanguageType language) {
		this.language = language;
	}

	@Override
	public void setParsed(boolean parsed) {
		this.isParsed = parsed;
	}

	@Override
	public void setParseData(IParseData parseData) {
		this.parseData = parseData;

	}

	@Override
	public void setHeader(String header) {
		this.header = header;
	}

	@Override
	public String getSql() {
		return getSQLSource();
	}

	@Override
	public IProcedureContainer getParent() {
		return parent;
	}

	@Override
	public void setParent(IProcedureContainer parent) {
		this.parent = parent;
	}

	@Override
	public void clearVariables() {
		vars.clear();
	}

	@Override
	public void setSql(String sql) {
		setSQLSource(sql);
	}
}
