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
package com.nextep.designer.dbgm.oracle.ui.actions;

import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.dbgm.ui.jface.ColumnsContentProvider;
import com.nextep.designer.dbgm.ui.jface.DbgmLabelProvider;
import com.nextep.designer.dbgm.ui.model.IColumnBinding;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class ClusteredTableColumnsActionProvider extends AbstractFormActionProvider {

	private final static Log LOGGER = LogFactory.getLog(ClusteredTableColumnsActionProvider.class);

	@Override
	public Object add(ITypedObject parent) {
		return null;
	}

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
	}

	@Override
	public boolean isAddRemoveEnabled() {
		return false;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public void edit(ITypedObject parent, ITypedObject element) {
		final IColumnBinding binding = (IColumnBinding) element;
		final IOracleClusteredTable table = (IOracleClusteredTable) parent;
		final IContentProvider contentProvider = new ColumnsContentProvider();
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);

		IBasicTable mappedTable = null;
		try {
			mappedTable = (IBasicTable) VersionHelper.getReferencedItem(table.getTableReference());
		} catch (ErrorException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					DBOMUIMessages.getString("action.clusterTabColumns.unresolvableTableTitle"), //$NON-NLS-1$
					DBOMUIMessages.getString("action.clusterTabColumns.unresolvableTableMsg")); //$NON-NLS-1$
			LOGGER.error("Unresolvable clustered table: " + e.getMessage(), e); //$NON-NLS-1$
			return;
		}
		final IBasicColumn column = (IBasicColumn) uiService
				.findElement(
						Display.getDefault().getActiveShell(),
						MessageFormat.format(
								DBOMUIMessages
										.getString("action.clusterTabColumns.pickColumnTitle"), binding.getColumn().getName()), mappedTable, contentProvider, //$NON-NLS-1$
						new DbgmLabelProvider());
		if (column != null) {
			table.setColumnReferenceMapping(binding.getColumn().getReference(),
					column.getReference());
		}
	}
}
