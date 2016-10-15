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
/**
 *
 */
package com.nextep.designer.dbgm.model.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.dbgm.model.IColumnValue;
import com.nextep.designer.dbgm.model.IDataLine;
import com.nextep.designer.dbgm.model.IDataSet;

/**
 * Default implementation of a data line.
 * 
 * @see IDataLine
 * @author Christophe Fondacci
 */
public class DataLine extends Observable implements IDataLine {

	private IDataSet parent;
	private Set<IColumnValue> values;
	private Map<IReference, IColumnValue> columnsMap;
	private long position;
	private UID id;
	private long versionTag;

	public DataLine(IDataSet dataSet) {
		this.parent = dataSet;
		values = new HashSet<IColumnValue>();
		columnsMap = new HashMap<IReference, IColumnValue>();
		// Initializing position
		position = dataSet.getCurrentRowId() + 1;
		dataSet.setCurrentRowId(position);
		// Initializing column empty values from data set
		for (IBasicColumn c : parent.getColumns()) {
			addColumnValue(new ColumnValue(this, c, c.getDefaultExpr()));
		}
	}

	public DataLine() {
		values = new HashSet<IColumnValue>();
		columnsMap = new HashMap<IReference, IColumnValue>();
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataLine#addColumnValue(com.nextep.designer.dbgm.model.IColumnValue)
	 */
	@Override
	public void addColumnValue(IColumnValue columnValue) {
		values.add(columnValue);
		columnsMap.put(columnValue.getColumnRef(), columnValue);
		columnValue.setDataLine(this);
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataLine#getColumnValue(int)
	 */
	@Override
	public IColumnValue getColumnValue(int linePosition) {
		IReference colRef = parent.getColumnsRef().get(linePosition);
		return getColumnValue(colRef);
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataLine#getColumnValue(com.nextep.datadesigner.dbgm.model.IBasicColumn)
	 */
	@Override
	public IColumnValue getColumnValue(IReference colRef) {
		return columnsMap.get(colRef);
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataLine#getColumnValues()
	 */
	@Override
	public Set<IColumnValue> getColumnValues() {
		return values;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataLine#removeColumnValue(com.nextep.designer.dbgm.model.IColumnValue)
	 */
	@Override
	public boolean removeColumnValue(IColumnValue columnValue) {
		columnValue.setDataLine(null);
		columnsMap.remove(columnValue.getColumnRef());
		return values.remove(columnValue);
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#getUID()
	 */
	@Override
	public UID getUID() {
		return id;
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#setUID(com.nextep.datadesigner.model.UID)
	 */
	@Override
	public void setUID(UID id) {
		this.id = id;
	}

	/**
	 * Hibernate id getter
	 * 
	 * @return the identifier
	 */
	protected long getId() {
		if (id == null) {
			return 0;
		} else {
			return id.rawId();
		}
	}

	/**
	 * Hibernate id setter
	 * 
	 * @param id new data line id
	 */
	protected void setId(long id) {
		this.id = new UID(id);
	}

	@Override
	public void setDataSet(IDataSet parent) {
		this.parent = parent;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataLine#getDataSet()
	 */
	@Override
	public IDataSet getDataSet() {
		return parent;
	}

	@Override
	public long getRowId() {
		return position;
	}

	@Override
	public void setRowId(long position) {
		this.position = position;
	}

	/**
	 * Hibernate values setter
	 */
	protected void setColumnValues(Set<IColumnValue> values) {
		this.values = values;
		// Updating our columns map
		for (IColumnValue v : values) {
			columnsMap.put(v.getColumnRef(), v);
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

}
