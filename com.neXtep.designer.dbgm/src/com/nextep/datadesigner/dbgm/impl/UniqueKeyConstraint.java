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
package com.nextep.datadesigner.dbgm.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.DBGMMessages;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;

/**
 * @author Christophe Fondacci
 */
public class UniqueKeyConstraint extends IKeyConstraint {

	public static final String TYPE_ID = "UNIQUE_KEY"; //$NON-NLS-1$
	private ConstraintType type = ConstraintType.UNIQUE;
	private final static Log log = LogFactory.getLog(UniqueKeyConstraint.class);

	public UniqueKeyConstraint() {
		super();
	}

	public UniqueKeyConstraint(String name, String description, IBasicTable constrainedTable) {
		super(name, description, constrainedTable);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IKeyConstraint#getConstraintType()
	 */
	@Override
	public ConstraintType getConstraintType() {
		return type;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IKeyConstraint#setConstraintType(com.nextep.datadesigner.dbgm.model.ConstraintType)
	 */
	@Override
	public void setConstraintType(ConstraintType type) {
		if (this.type != type) {
			switch (type) {
			case UNIQUE:
			case PRIMARY:
				this.type = type;
				notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				break;
			default:
				throw new ErrorException(DBGMMessages.getString("uniqueKey.unsupportedType")); //$NON-NLS-1$
			}
		}
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}
}
