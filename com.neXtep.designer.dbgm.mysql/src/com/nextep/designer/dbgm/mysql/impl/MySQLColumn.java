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

import com.nextep.datadesigner.dbgm.impl.BasicColumn;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.mysql.model.IMySQLColumn;

/**
 * @author Christophe Fondacci
 */
public class MySQLColumn extends BasicColumn implements IMySQLColumn {

	/** Column auto-increment flag */
	private boolean autoIncrement = false;
	private String charset;
	private String collation;

	/**
	 * Full constructor.
	 * 
	 * @param name column name
	 * @param description description
	 * @param type datatype
	 * @param rank rank in table's column
	 */
	public MySQLColumn(String name, String description, IDatatype type, int rank) {
		super(name, description, type, rank);
	}

	/**
	 * Empty hibernate constructor
	 */
	protected MySQLColumn() {
		super();
	}

	/**
	 * @see com.nextep.designer.dbgm.mysql.model.IMySQLColumn#isAutoIncremented()
	 */
	@Override
	public boolean isAutoIncremented() {
		return autoIncrement;
	}

	/**
	 * @see com.nextep.designer.dbgm.mysql.model.IMySQLColumn#setAutoIncremented(boolean)
	 */
	@Override
	public void setAutoIncremented(boolean autoIncremented) {
		this.autoIncrement = autoIncremented;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.datadesigner.impl.NamedObservable#checkConsistency()
	 */
	@Override
	public void checkConsistency() throws InconsistentObjectException {
		super.checkConsistency();
		if (isAutoIncremented()) {
			boolean columnInPK = false;
			if (getParent() instanceof IBasicTable) {
				for (IKeyConstraint key : ((IBasicTable) getParent()).getConstraints()) {
					if (key.getConstraintType() == ConstraintType.PRIMARY
							|| key.getConstraintType() == ConstraintType.UNIQUE) {
						for (IReference keyColRef : key.getConstrainedColumnsRef()) {
							if (keyColRef.equals(this.getReference())) {
								columnInPK = true;
								break;
							}
						}
					}
				}
				if (!columnInPK) {
					for (IIndex index : ((IBasicTable) getParent()).getIndexes()) {
						if (index.getIndexType() == IndexType.UNIQUE) {
							for (IReference r : index.getIndexedColumnsRef()) {
								if (this.getReference().equals(r)) {
									columnInPK = true;
									break;
								}
							}
						}
					}
				}
			}
			if (!columnInPK) {
				throw new InconsistentObjectException(
						"Column "
								+ getParent().getName()
								+ "."
								+ getName()
								+ " is set as auto-incremented but is not the primary key or in a unique index of this table.");
			}
		}
	}

	protected String getHibernateAutoIncremented() {
		return autoIncrement ? "Y" : "N";
	}

	protected void setHibernateAutoIncremented(String val) {
		autoIncrement = "Y".equals(val);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.impl.BasicColumn#copy()
	 */
	@Override
	public IBasicColumn copy() {
		MySQLColumn c = new MySQLColumn(this.getName(), this.getDescription(), new Datatype(
				this.getDatatype()), this.getRank());
		c.setDefaultExpr(this.getDefaultExpr());
		c.setNotNull(this.isNotNull());
		c.setReference(this.getReference());
		c.setParent(this.getParent());
		c.setAutoIncremented(this.isAutoIncremented());
		c.setCharacterSet(this.getCharacterSet());
		c.setCollation(this.getCollation());
		return c;
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
