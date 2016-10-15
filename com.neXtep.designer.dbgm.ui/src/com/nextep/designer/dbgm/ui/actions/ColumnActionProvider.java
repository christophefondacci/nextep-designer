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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.ui.actions;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * Provides the UI action for the column page
 * 
 * @author Christophe Fondacci
 */
public class ColumnActionProvider extends AbstractFormActionProvider {

	@Override
	public Object add(ITypedObject parent) {
		IColumnable c = (IColumnable) parent;
		// Retrieving column's controller
		final ITypedObjectUIController columnController = UIControllerFactory
				.getController(IElementType.getInstance(IBasicColumn.TYPE_ID));
		return columnController.emptyInstance(null, c);
	}

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
		final IBasicColumn column = (IBasicColumn) toRemove;
		// Asking user confirmation
		final boolean confirmed = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
				MessageFormat.format(
						DBGMUIMessages.getString("table.editor.confirmDelColumnTitle"), //$NON-NLS-1$
						column.getName()), MessageFormat.format(
						DBGMUIMessages.getString("table.editor.confirmDelColumn"), //$NON-NLS-1$
						column.getName(), column.getParent().getName()));
		if (confirmed) {
			// If confirmed, we remove it through our workspace service
			final IWorkspaceUIService workspaceService = VCSUIPlugin
					.getService(IWorkspaceUIService.class);
			workspaceService.remove((IReferenceable) toRemove);
		}

	}

	@Override
	public void up(ITypedObject parent, ITypedObject element) {
		IColumnable columnable = (IColumnable) parent;
		final IBasicColumn column = (IBasicColumn) element;

		// Retrieving column's position
		final int pos = columnable.getColumns().indexOf(column);
		// We can only "up" it if it is not already at first position (index=0)
		if (pos > 0) {
			// Getting the column immediately on top of the current one
			final IBasicColumn otherCol = column.getParent().getColumns().get(pos - 1);
			// Swapping columns
			Collections.swap(column.getParent().getColumns(), pos, pos - 1);
			column.setRank(pos - 1);
			otherCol.setRank(pos);
			// Saving table
			CorePlugin.getIdentifiableDao().save((IdentifiedObject) parent);
			// Notifying changes on the table
			((IObservable) parent).notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void down(ITypedObject parent, ITypedObject element) {
		IColumnable columnable = (IColumnable) parent;

		final IBasicColumn column = (IBasicColumn) element;
		// Retrieving column's position
		final List<IBasicColumn> columns = columnable.getColumns();
		final int pos = columns.indexOf(column);
		// We can only "down" it if it is not already at last position (index=columns size)
		if (pos < columns.size() - 1) {
			// Getting the column immediately on top of the current one
			final IBasicColumn otherCol = column.getParent().getColumns().get(pos + 1);
			// Swapping columns
			Collections.swap(column.getParent().getColumns(), pos, pos + 1);
			column.setRank(pos + 1);
			otherCol.setRank(pos);
			// Saving table
			CorePlugin.getIdentifiableDao().save((IdentifiedObject) parent);
			// Notifying changes on the table
			((IObservable) parent).notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public boolean isSortable() {
		return true;
	}
}
