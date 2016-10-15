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
package com.nextep.datadesigner.dbgm.impl;

import java.util.ArrayList;
import java.util.List;

import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.LengthType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.impl.NamedObjectHelper;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.designer.vcs.VCSPlugin;

public class Datatype extends Observable implements IDatatype {

	private NamedObjectHelper helper = null;
	private Integer length = -1;
	private Integer precision = 0;
	private boolean unsigned = false;
	private Integer charLength;
	private LengthType lengthType = LengthType.UNDEFINED;

	public static IDatatype getDefaultDatatype() {
		return DBGMHelper.getDatatypeProvider(
				VCSPlugin.getViewService().getCurrentWorkspace().getDBVendor())
				.getDefaultDatatype();
	}

	public static List<String> getTypes() {
		List<String> types = new ArrayList<String>();
		types.add("VARCHAR2");
		types.add("NUMBER");
		types.add("DATE");
		types.add("TIMESTAMP");
		return types;
	}

	public Datatype(String name, int length, int precision) {
		helper = new NamedObjectHelper(name, null, isEnum(name) ? IFormatter.NOFORMAT
				: IFormatter.UPPERSTRICT);
		this.length = length;
		this.precision = precision;
	}

	public Datatype(String name, int length) {
		this.length = length;
		precision = 0;
		helper = new NamedObjectHelper(name, null, isEnum(name) ? IFormatter.NOFORMAT
				: IFormatter.UPPERSTRICT);
	}

	public Datatype(String name) {
		length = 0;
		precision = 0;
		helper = new NamedObjectHelper(name, null, isEnum(name) ? IFormatter.NOFORMAT
				: IFormatter.UPPERSTRICT);
	}

	public Datatype(IDatatype t) {
		this();
		if (t != null) {
			length = t.getLength();
			precision = t.getPrecision();
			setUnsigned(t.isUnsigned());
			setName(t.getName());
			setDescription(t.getDescription());
			setLengthType(t.getLengthType());
			// helper = new NamedObjectHelper(t.getName(),t.getDescription());
		} else {
			// helper = new NamedObjectHelper("","");
		}
	}

	/**
	 * Hibernate private empty constructor
	 */
	private Datatype() {
		helper = new NamedObjectHelper(null, null, IFormatter.UPPERSTRICT);
	}

	@Override
	public Integer getLength() {
		return length;
	}

	@Override
	public void setLength(Integer size) {
		final Integer oldSize = getLength();
		this.length = size;
		notifyIfChanged(oldSize, size, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public String getDescription() {
		return helper.getDescription();
	}

	@Override
	public String getName() {
		return helper.getName();
	}

	@Override
	public void setDescription(String description) {
		final String oldDesc = getDescription();
		helper.setDescription(description);
		notifyIfChanged(oldDesc, description, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setName(String name) {
		final String oldName = getName();
		if (isEnum(name)) {
			helper.setFormatter(IFormatter.NOFORMAT);
		}
		helper.setName(name);
		notifyIfChanged(oldName, name, ChangeEvent.MODEL_CHANGED);
	}

	private boolean isEnum(String name) {
		return (name != null && name.trim().toUpperCase().startsWith("ENUM")); //$NON-NLS-1$
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IDatatype) {
			IDatatype d = (IDatatype) obj;
			// Should not happen (only via hibernate)
			if (helper.getName() == null) {
				return d.getName() == null;
			} else {
				final int lengthInt = length == null ? 0 : length.intValue();
				final int otherLengthInt = d.getLength() == null ? 0 : d.getLength().intValue();
				final int precisionInt = precision == null ? 0 : precision.intValue();
				final int otherPrecisionInt = d.getPrecision() == null ? 0 : d.getPrecision()
						.intValue();
				return getName().equals(d.getName()) && lengthInt == otherLengthInt
						&& precisionInt == otherPrecisionInt && lengthType == d.getLengthType();
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return getName()
				+ (length > 0 ? " (" + length + (precision > 0 ? "," + precision : "") + ")" : "")
				+ (isUnsigned() ? " UNSIGNED" : "");
	}

	@Override
	public Integer getPrecision() {
		return precision;
	}

	@Override
	public void setPrecision(Integer precision) {
		final Integer oldPrecision = getPrecision();
		this.precision = precision;
		notifyIfChanged(oldPrecision, precision, ChangeEvent.MODEL_CHANGED);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IDatatype#isUnsigned()
	 */
	@Override
	public boolean isUnsigned() {
		return unsigned;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IDatatype#setUnsigned(boolean)
	 */
	@Override
	public void setUnsigned(boolean unsigned) {
		final boolean oldUnsigned = isUnsigned();
		this.unsigned = unsigned;
		notifyIfChanged(oldUnsigned, unsigned, ChangeEvent.MODEL_CHANGED);
	}

	protected String getHibernateUnsigned() {
		return unsigned ? "Y" : "N"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void setHibernateUnsigned(String unsignedFlag) {
		unsigned = "Y".equals(unsignedFlag); //$NON-NLS-1$
	}

	@Override
	public void setLengthType(LengthType lengthType) {
		final LengthType oldLengthType = this.lengthType;
		this.lengthType = lengthType;
		notifyIfChanged(oldLengthType, unsigned, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public LengthType getLengthType() {
		return lengthType;
	}

	@Override
	public Integer getAlternateLength() {
		return charLength;
	}

	@Override
	public void setAlternateLength(Integer charLength) {
		this.charLength = charLength;
	}

	public String getHibernateLengthType() {
		if (lengthType == null) {
			return LengthType.UNDEFINED.databaseCode();
		}
		return lengthType.databaseCode();
	}

	public void setHibernateLengthType(String hibernateLengthType) {
		lengthType = LengthType.fromDatabaseCode(hibernateLengthType);
	}
}
