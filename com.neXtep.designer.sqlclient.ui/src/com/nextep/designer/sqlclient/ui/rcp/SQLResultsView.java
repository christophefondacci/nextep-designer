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
package com.nextep.designer.sqlclient.ui.rcp;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.connectors.SQLResultConnector;
import com.nextep.designer.sqlclient.ui.model.IPinnable;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLQueryListener;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;

public class SQLResultsView extends ViewPart implements IUIComponentContainer, IPinnable {

	public final static String VIEW_ID = "com.neXtep.designer.sqlclient.ui.rcp.SQLResultsView"; ////$NON-NLS-1$
	private final static String PROP_EDITABLE = "editable";
	private SQLResultConnector sqlResultsComponent;
	private Control control;
	private boolean pinned = false;

	public SQLResultsView() {
		sqlResultsComponent = new SQLResultConnector();
	}

	@Override
	public IUIComponent getUIComponent() {
		return sqlResultsComponent;
	}

	@Override
	public void run(boolean block, boolean cancellable, IRunnableWithProgress runnable) {

	}

	@Override
	public void createPartControl(Composite parent) {
		control = sqlResultsComponent.create(parent);
		TableViewer viewer = sqlResultsComponent.getTableViewer();
		getSite().setSelectionProvider(viewer);
		registerContextMenu(viewer);
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(control, "com.neXtep.designer.sqlclient.ui.SQLResultsView"); //$NON-NLS-1$
	}

	@Override
	public void setFocus() {
		control.setFocus();
	}

	public void resizeColumns() {
		sqlResultsComponent.adjusteColumnSizes();
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == ISQLQuery.class) {
			return sqlResultsComponent.getSQLQuery();
		} else if (adapter == ISQLQueryListener.class) {
			return sqlResultsComponent;
		} else if (adapter == IPinnable.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	private void registerContextMenu(TableViewer viewer) {
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);

		// this is to work around complaints about missing standard groups.
		contextMenu.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new GroupMarker("editions")); //$NON-NLS-1$
				manager.add(new Separator());
				manager.add(new GroupMarker("actions")); //$NON-NLS-1$
				manager.add(new Separator());
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

			}
		});

		getSite().registerContextMenu(contextMenu, viewer);
		Menu menu = contextMenu.createContextMenu(viewer.getTable());
		viewer.getTable().setMenu(menu);
	}

	public void setEditableState(boolean isEditable) {
		final boolean oldValue = sqlResultsComponent.getEditableState();
		sqlResultsComponent.setEditableState(isEditable);
		firePartPropertyChanged(PROP_EDITABLE, String.valueOf(oldValue), String.valueOf(isEditable));
	}

	public boolean getEditableState() {
		return sqlResultsComponent.getEditableState();
	}

	@Override
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	@Override
	public boolean isPinned() {
		return pinned;
	}

	@Override
	public void dispose() {
		SQLClientPlugin.getService(ISQLClientService.class).viewDisposed(this);
		super.dispose();
	}

	@Override
	public void setErrorMessage(String message) {
		// No error message support
	}
}
