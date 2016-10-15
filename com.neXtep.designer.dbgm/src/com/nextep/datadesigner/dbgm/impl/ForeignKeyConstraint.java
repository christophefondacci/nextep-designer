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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.ForeignKeyAction;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;

/**
 * This class represents a foreign key constraint. It extends the default interface IKeyConstraint
 * to add the remote constraint information.
 * 
 * @author Christophe Fondacci
 */
public class ForeignKeyConstraint extends IKeyConstraint {

	public static final String TYPE_ID = "FOREIGN_KEY"; //$NON-NLS-1$
	// private IKeyConstraint remoteConstraint;
	private IReference remoteConstraintRef;
	private ForeignKeyAction onUpdateAction = ForeignKeyAction.NO_ACTION;
	private ForeignKeyAction onDeleteAction = ForeignKeyAction.NO_ACTION;;
	private static final Log log = LogFactory.getLog(ForeignKeyConstraint.class);

	public ForeignKeyConstraint() {
		super();
	}

	/**
	 * Standard constructor. Initializes a new foreign key constraint. By default, the remote
	 * constraint will be set to the fromTable's primary key, if defined.
	 * 
	 * @param name name of this foreign key constraint
	 * @param description initial description of this foreign key
	 * @param table table on which the foreign key will be generated
	 */
	public ForeignKeyConstraint(String name, String description, IBasicTable onTable) {
		super(name, description, onTable);
	}

	/**
	 * Defines the remote constraint of the foreign table which is referenced by this primary key.
	 * 
	 * @param constraint remote constraint to define
	 */
	public void setRemoteConstraint(IKeyConstraint constraint) {
		if (constraint != null) {
			this.remoteConstraintRef = constraint.getReference();
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		} else {
			remoteConstraintRef = null;
		}
	}

	/**
	 * @return the remote constraint referenced by this foreign key
	 */
	public IKeyConstraint getRemoteConstraint() {
		if (remoteConstraintRef == null) {
			return null;
		}
		return (IKeyConstraint) VersionHelper.getReferencedItem(remoteConstraintRef);
	}

	/**
	 * Checks the consistency of this foreign key. A foreign key is consistent if its constrained
	 * columns are as many and of equal type as remote constraint columns. <br>
	 * <br>
	 * This method should be used before generation and/or for error check.
	 * 
	 * @see com.nextep.datadesigner.dbgm.model.IKeyConstraint#checkConsistency()
	 */
	@Override
	public void checkConsistency() throws InconsistentObjectException {
		super.checkConsistency();
		IKeyConstraint remoteConstraint = getRemoteConstraint();
		if (remoteConstraint == null) {
			throw new InconsistentObjectException(
					"A foreign key constraint must refer to an existing remote constraint.");
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IKeyConstraint#getConstraintType()
	 */
	@Override
	public ConstraintType getConstraintType() {
		return ConstraintType.FOREIGN;
	}

	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IKeyConstraint#setConstraintType(com.nextep.datadesigner.dbgm.model.ConstraintType)
	 */
	@Override
	public void setConstraintType(ConstraintType type) {
		// Nonsense, a foreign key will always be and stay a foreign key
	}

	public IReference getRemoteConstraintRef() {
		return remoteConstraintRef;
	}

	public void setRemoteConstraintRef(IReference ref) {
		this.remoteConstraintRef = ref;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IKeyConstraint#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		List<IReference> refs = new ArrayList<IReference>(super.getReferenceDependencies());
		refs.add(remoteConstraintRef);
		if (remoteConstraintRef != null) {
			refs.add(getRemoteConstraint().getConstrainedTable().getReference());
		}
		return refs;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IKeyConstraint#updateReferenceDependencies(com.nextep.datadesigner.model.IReference,
	 *      com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		if (super.updateReferenceDependencies(oldRef, newRef)) {
			return true;
		} else {
			if (oldRef.equals(remoteConstraintRef)) {
				remoteConstraintRef = newRef;
				return true;
			}
		}
		return false;
	}

	/**
	 * Defines the action to be exectued by the "ON UPDATE" clause of a ForeignKey.
	 * 
	 * @param action on update action
	 */
	public void setOnUpdateAction(ForeignKeyAction action) {
		final ForeignKeyAction oldAction = this.onUpdateAction;
		this.onUpdateAction = action;
		notifyIfChanged(oldAction, onUpdateAction, ChangeEvent.MODEL_CHANGED);
	}

	/**
	 * Retrieves the action to execute by the "ON UPDATE" clause of a Foreign key
	 * 
	 * @return the on update action
	 */
	public ForeignKeyAction getOnUpdateAction() {
		return onUpdateAction;
	}

	/**
	 * Defines the action to beexecuted by the "ON DELETE" clause of a foreign key.
	 * 
	 * @param action on delete action
	 */
	public void setOnDeleteAction(ForeignKeyAction action) {
		final ForeignKeyAction oldAction = this.onDeleteAction;
		this.onDeleteAction = action;
		notifyIfChanged(oldAction, onDeleteAction, ChangeEvent.MODEL_CHANGED);
	}

	/**
	 * Retrieves the action to execute on the "ON DELETE" clause of a foreign key
	 * 
	 * @return the on delete action
	 */
	public ForeignKeyAction getOnDeleteAction() {
		return onDeleteAction;
	}
}
