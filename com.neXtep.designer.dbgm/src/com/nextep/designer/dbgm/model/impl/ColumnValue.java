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

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.model.IColumnValue;
import com.nextep.designer.dbgm.model.IDataLine;

/**
 * @author Christophe Fondacci
 */
public class ColumnValue extends Observable implements IColumnValue {

	private IDataLine line;
	private IReference columnRef;
	private Object value;
	private UID id;

	// private static final Log log = LogFactory.getLog(ColumnValue.class);

	public ColumnValue(IDataLine line, IBasicColumn column, Object value) {
		this.line = line;
		this.columnRef = column.getReference();
		this.value = value;
	}

	/**
	 * Hibernate empty constructor
	 */
	public ColumnValue() {
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IColumnValue#getColumn()
	 */
	@Override
	public IBasicColumn getColumn() {
		return (IBasicColumn) VersionHelper.getReferencedItem(columnRef);
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IColumnValue#getStringValue()
	 */
	@Override
	public String getStringValue() {
		if (value != null) {
			return value.toString();
		}
		return null;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IColumnValue#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IColumnValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		this.value = value;
	}

	protected void setStringValue(String value) {
		this.value = value;
		// try {
		// this.value = new String(value.getBytes(),"UTF-8");
		// } catch(UnsupportedEncodingException e) {
		// log.error("Cannot store UTF8, storing in default charset",e);
		// this.value = value;
		// }
	}

	@Override
	public void setColumn(IBasicColumn column) {
		this.columnRef = column.getReference();
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

	protected void setId(long id) {
		setUID(new UID(id));
	}

	protected long getId() {
		if (id == null) {
			return 0;
		} else {
			return id.rawId();
		}
	}

	@Override
	public IDataLine getDataLine() {
		return line;
	}

	@Override
	public void setDataLine(IDataLine line) {
		this.line = line;
	}

	/**
	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IColumnValue#getColumnRef()
	 */
	@Override
	public IReference getColumnRef() {
		return columnRef;
	}

	@Override
	public void setColumnRef(IReference ref) {
		this.columnRef = ref;
	}
}
