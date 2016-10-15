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
package com.nextep.designer.dbgm.oracle.impl;

import java.util.ArrayList;
import java.util.Collection;
import com.nextep.datadesigner.dbgm.impl.UserType;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;

public class OracleUserType extends UserType implements IOracleUserType {

	private String body;
	private IParseData parseData;
	private boolean parsed = false;
	private Collection<IProcedure> procedures;

	public OracleUserType() {
		super();
		procedures = new ArrayList<IProcedure>();
	}

	@Override
	public String getTypeBody() {
		return body;
	}

	@Override
	public void setTypeBody(String bodySQL) {
		this.body = bodySQL;
		setParsed(false);
		notifyListeners(ChangeEvent.SOURCE_CHANGED, bodySQL);
	}

	@Override
	public IParseData getParseData() {
		return parseData;
	}

	@Override
	public boolean isParsed() {
		return parsed;
	}

	@Override
	public void setParseData(IParseData parseData) {
		this.parseData = parseData;
	}

	@Override
	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}

	@Override
	public void addProcedure(IProcedure procedure) {
		procedure.setParent(this);
		procedures.add(procedure);
	}

	@Override
	public void clearProcedures() {
		procedures.clear();
	}

	@Override
	public Collection<IProcedure> getProcedures() {
		if (!isParsed()) {
			DBGMHelper.parse(this);
		}
		return procedures;
	}

	@Override
	public void setProcedures(Collection<IProcedure> procedures) {
		this.procedures = procedures;
	}

	@Override
	public String getSql() {
		return body;
	}

	@Override
	public void setSql(String sql) {
		setTypeBody(sql);
	}

}
