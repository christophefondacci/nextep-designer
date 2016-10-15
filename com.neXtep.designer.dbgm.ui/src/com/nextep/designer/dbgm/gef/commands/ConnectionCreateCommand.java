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

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 */
public class ConnectionCreateCommand extends Command {

	private static final Log log = LogFactory.getLog(ConnectionCreateCommand.class);
	IDiagramItem start;
	IDiagramItem end;

	public ConnectionCreateCommand(IDiagramItem start) {
		this.start = start;
	}

	/**
	 * Sets the end of the create connection (foreign key) command.
	 * 
	 * @param end
	 */
	public void setEnd(IDiagramItem end) {
		this.end = end;
	}

	/**
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	@Override
	public boolean canExecute() {
		if (start == null || end == null)
			return false;
		try {
			IReferenceable startModel = start.getItemModel();
			IReferenceable endModel = end.getItemModel();
			return start != null && end != null && start != end
					&& startModel.getReference().getType() == IElementType.getInstance("TABLE")
					&& endModel.getReference().getType() == IElementType.getInstance("TABLE");
		} catch (ErrorException e) {
			log.error(e);
			return false;
		}

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
		IBasicTable source = (IBasicTable) start.getItemModel();
		IBasicTable remote = (IBasicTable) end.getItemModel();
		source = VCSUIPlugin.getVersioningUIService().ensureModifiable(source);
		ITypedObjectUIController fkController = UIControllerFactory.getController(IElementType
				.getInstance(ForeignKeyConstraint.TYPE_ID));
		ForeignKeyConstraint fk = new ForeignKeyConstraint(); // (ForeignKeyConstraint)fkController.emptyInstance(source.getShortName()
		// + "_" + remote.getShortName() +
		// "_FK",source);
		fk.setName(source.getShortName() + "_" + remote.getShortName() + "_FK");
		fk.setConstrainedTable(source);
		// Retrieving remote unique key
		IKeyConstraint remoteUk = null;
		for (IKeyConstraint c : remote.getConstraints()) {
			if (c.getConstraintType() == ConstraintType.PRIMARY) {
				remoteUk = c;
				break;
			}
		}
		// Connecting to foreign key
		if (remoteUk != null) {
			fk.setRemoteConstraint(remoteUk);
			fk.setName(source.getShortName() + "_" + remote.getShortName() + "_FK");
		}
		adjustForeignKeyColumns(fk, true);
		// If no enforcing index found, query the user to create one
		if ((fk.getEnforcingIndex() == null || fk.getEnforcingIndex().isEmpty())
				&& !fk.getConstrainedColumnsRef().isEmpty()) {
			boolean create = MessageDialog.openQuestion(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					DBGMUIMessages.getString("createEnforcingIndexTitle"), //$NON-NLS-1$
					DBGMUIMessages.getString("createEnforcingIndexQuestion")); //$NON-NLS-1$
			if (create) {
				IIndex i = (IIndex) UIControllerFactory.getController(
						IElementType.getInstance(IIndex.INDEX_TYPE)).emptyInstance(fk.getName(),
						fk.getConstrainedTable());
				// i.setIndexedTableRef(c.getConstrainedTable().getReference());
				for (IReference colRef : fk.getConstrainedColumnsRef()) {
					i.addColumnRef(colRef);
				}
			}
		}
		// Adding
		ControllerFactory.getController(fk).save(fk);
		source.addConstraint(fk);
		fkController.defaultOpen(fk);
	}

	/**
	 * Refreshing current columns mappings from remote constraint to current table columns.
	 * 
	 * @param guessMappedColumn should this method try to "guess" the foreign key column which
	 *        matches remote column
	 */
	private void adjustForeignKeyColumns(ForeignKeyConstraint constraint, boolean guessMappedColumn) {
		final IKeyConstraint currentRemoteConstraint = constraint.getRemoteConstraint();

		if (currentRemoteConstraint != null) {
			final Iterator<IBasicColumn> remoteColIt = currentRemoteConstraint.getColumns()
					.iterator();
			final Iterator<IBasicColumn> fkColIt = new ArrayList<IBasicColumn>(
					constraint.getColumns()).iterator();
			// Iterating over 2 collection synchronously to :
			// - remove any overflowing fk column
			// - guess any remote constraint column we could match
			while (remoteColIt.hasNext()) {
				final IBasicColumn remoteCol = remoteColIt.next();
				if (fkColIt.hasNext()) {
					fkColIt.next();
				} else {
					IBasicColumn guessedCol = null;
					// Guessing any corresponding column by its name if needed
					if (guessMappedColumn) {
						guessedCol = getConstrainedColumn(constraint.getConstrainedTable(),
								remoteCol.getName());
					}
					if (guessedCol == null) {
						// Forcing inconsistency by setting current col to remote col
						guessedCol = remoteCol;
					}
					constraint.addColumn(guessedCol);
				}
			}
			// Removing any overflowing fk column
			removeSubsequentForeignKeyColumns(constraint, fkColIt);
		}
	}

	/**
	 * Retrieves the constrained column from its name string.
	 * 
	 * @param constraint constraint which reference the named column
	 * @param columnName string value of the column name
	 * @return the {@link IBasicColumn} instance
	 */
	private IBasicColumn getConstrainedColumn(IBasicTable table, String columnName) {
		for (IBasicColumn c : table.getColumns()) {
			if (c.getName().equals(columnName)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Convenience method to remove from the list of constrained columns of the current foreign key
	 * constraint all columns indicated by the specified <code>Iterator</code>.
	 * 
	 * @param fkColsIt an {@link Iterator} pointing to the {@link IBasicColumn} to remove from the
	 *        list of the constrained columns of the current foreign key
	 */
	private void removeSubsequentForeignKeyColumns(ForeignKeyConstraint constraint,
			Iterator<IBasicColumn> fkColsIt) {
		while (fkColsIt.hasNext()) {
			IBasicColumn fkCol = fkColsIt.next();
			constraint.removeColumn(fkCol);
		}
	}

}
