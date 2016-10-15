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
package com.nextep.designer.sqlclient.ui.handlers;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceScopes;
import com.nextep.designer.sqlclient.ui.rcp.SQLResultsView;

public class UpdateRowHandler extends AbstractHandler implements IElementUpdater,
		IPropertyChangeListener {

	private Map<IWorkbenchPart, Boolean> toggleStates = new HashMap<IWorkbenchPart, Boolean>();
	private final static String CMD_ID = "com.neXtep.designer.sqlclient.ui.updateRow";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		if (part instanceof SQLResultsView) {
			final SQLResultsView sqlView = (SQLResultsView) part;
			sqlView.setEditableState(!sqlView.getEditableState());
			sqlView.addPartPropertyListener(this);
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		final IWorkbenchPartSite site = (IWorkbenchPartSite) parameters
				.get(IServiceScopes.PARTSITE_SCOPE);
		if (site != null) {
			final IWorkbenchPart part = site.getPart();
			element.setChecked(((SQLResultsView) part).getEditableState());
		} else {
			element.setChecked(false);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() instanceof SQLResultsView) {
			final SQLResultsView view = (SQLResultsView) event.getSource();
			// toggleStates.put(view, view.getEditableState());
			ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(
					ICommandService.class);
			Map filters = new HashMap();
			filters.put(IServiceScopes.PARTSITE_SCOPE, view.getSite());
			service.refreshElements(CMD_ID, filters);
		}
	}
}
