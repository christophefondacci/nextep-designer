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
package com.nextep.designer.dbgm.gef.commands;

import org.eclipse.gef.commands.Command;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.designer.vcs.model.IDiagramItem;

/**
 * A command for reconnecting foreign keys on the graphical diagram
 * 
 * @author Christophe Fondacci
 */
public class ForeignKeyReconnectCommand extends Command {

	private ForeignKeyConstraint fk;
	private IDiagramItem newRemote;
	private IDiagramItem newSource;
	private IKeyConstraint oldPK;

	public ForeignKeyReconnectCommand(ForeignKeyConstraint fk) {
		this.fk = fk;
	}

	/**
	 * Defines the specified diagram item as the new remote source of the foreign key constraint.
	 * 
	 * @param item item representing the remote table
	 */
	public void setNewRemote(IDiagramItem item) {
		this.newRemote = item;
	}

	/**
	 * Defines the specified diagram item as the new constraint parent.
	 * 
	 * @param item item representing the constraint's parent table
	 */
	public void setNewSource(IDiagramItem item) {
		this.newSource = item;
	}

	/**
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	@Override
	public boolean canExecute() {
		if (newRemote != null) {
			IBasicTable t = (IBasicTable) newRemote.getItemModel();
			IKeyConstraint pk = getPK(t);
			if (pk == null)
				return false;
			// Validating pk consistency with constraint
			IKeyConstraint oldRemotePK = fk.getRemoteConstraint();
			try {
				Observable.deactivateListeners();
				fk.setRemoteConstraint(pk);
				fk.checkConsistency();
			} catch (Exception e) {
				return false;
			} finally {
				fk.setRemoteConstraint(oldRemotePK);
				Observable.activateListeners();
			}
			return Designer.checkIsModifiable(fk.getConstrainedTable(), false);
		}
		// TODO Auto-generated method stub
		return super.canExecute();
	}

	/**
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		redo();
	}

	/**
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	@Override
	public void redo() {
		if (newRemote != null) {
			IBasicTable t = (IBasicTable) newRemote.getItemModel();
			IKeyConstraint pk = getPK(t);
			if (oldPK == null) {
				oldPK = fk.getRemoteConstraint();
			}
			fk.setRemoteConstraint(pk);
		}
	}

	/**
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public void undo() {
		if (newRemote != null) {
			if (oldPK != null) {
				fk.setRemoteConstraint(oldPK);
			}
		}
		// TODO Auto-generated method stub

	}

	private IKeyConstraint getPK(IBasicTable t) {
		// Retrieving primary key
		IKeyConstraint pk = null;
		for (IKeyConstraint c : t.getConstraints()) {
			if (c.getConstraintType() == ConstraintType.PRIMARY) {
				pk = c;
				break;
			}
		}
		return pk;
	}
}
