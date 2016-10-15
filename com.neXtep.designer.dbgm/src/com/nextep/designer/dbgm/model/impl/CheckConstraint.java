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
package com.nextep.designer.dbgm.model.impl;

import com.nextep.datadesigner.dbgm.impl.SynchronizableNamedObservable;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.dbgm.model.ICheckConstraint;

public class CheckConstraint extends SynchronizableNamedObservable implements ICheckConstraint,
		IParentable<IBasicTable> {

	/** Checked condition */
	private String condition;
	/** Constrained table */
	private IBasicTable parent;

	public CheckConstraint() {
		setReference(new Reference(getType(), null, this));
		nameHelper.setFormatter(IFormatter.UPPERCASE);
	}

	@Override
	public String getCondition() {
		return condition;
	}

	@Override
	public void setCondition(String condition) {
		if (this.condition != condition) {
			this.condition = condition;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public ICheckConstraint getModel() {
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
		return getConstrainedTable().updatesLocked();
	}

	@Override
	public IBasicTable getConstrainedTable() {
		return parent;
	}

	@Override
	public void setConstrainedTable(IBasicTable t) {
		this.parent = t;
	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		super.checkConsistency();
		if (getCondition() == null || "".equals(getCondition())) {
			throw new InconsistentObjectException(
					"A condition must be defined and not empty for this check constraint.");
		}
	}

	@Override
	public IBasicTable getParent() {
		return getConstrainedTable();
	}

	@Override
	public void setParent(IBasicTable parent) {
		setConstrainedTable(parent);
	}
}
