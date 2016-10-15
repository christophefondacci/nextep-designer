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
package com.nextep.designer.dbgm.mysql.impl;

import com.nextep.datadesigner.dbgm.impl.VersionedTable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.dbgm.mysql.model.IMySQLTable;

/**
 * @author Christophe Fondacci
 */
public class MySQLTable extends VersionedTable implements IMySQLTable {

	private String engine = "InnoDB"; //$NON-NLS-1$
	private String charset = "utf8"; //$NON-NLS-1$
	private String collation = ""; //$NON-NLS-1$

	/**
	 * @see com.nextep.designer.dbgm.mysql.model.IMySQLTable#getEngine()
	 */
	@Override
	public String getEngine() {
		return engine;
	}

	/**
	 * @see com.nextep.designer.dbgm.mysql.model.IMySQLTable#setEngine(java.lang.String)
	 */
	@Override
	public void setEngine(String engine) {
		final String old = this.engine;
		this.engine = engine;
		notifyIfChanged(old, engine, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public String getCharacterSet() {
		return charset;
	}

	@Override
	public void setCharacterSet(String charset) {
		final String old = this.charset;
		this.charset = charset;
		notifyIfChanged(old, charset, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setCollation(String collation) {
		final String old = this.collation;
		this.collation = collation;
		notifyIfChanged(old, this.collation, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public String getCollation() {
		return collation;
	}

}
