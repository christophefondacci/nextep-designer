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

import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class ClusteredTableActionProvider extends AbstractFormActionProvider {

	@Override
	public Object add(ITypedObject parent) {
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
		final Object elt = uiService.findElement(
				Display.getCurrent().getActiveShell(),
				DBOMUIMessages.getString("addClusteredTableSelection"), //$NON-NLS-1$
				IElementType.getInstance(IBasicTable.TYPE_ID),
				IElementType.getInstance(IMaterializedView.VIEW_TYPE_ID));
		if (elt != null) {
			IBasicTable t = (IBasicTable) elt;
			return ((IOracleCluster) parent).addClusteredTable(t.getReference());
		}
		return null;
	}

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
		((IOracleCluster) parent).removeClusteredTable(((IOracleClusteredTable) toRemove)
				.getTableReference());
	}
}
