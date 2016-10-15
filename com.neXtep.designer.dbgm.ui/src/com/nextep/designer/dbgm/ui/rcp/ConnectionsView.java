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
package com.nextep.designer.dbgm.ui.rcp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import com.nextep.datadesigner.ctrl.ConnectionNavigator;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.gui.impl.TreeSelectionProvider;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.gui.external.VersionablePaintItemListener;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ITargetSet;
import com.nextep.designer.vcs.ui.navigators.TargetSetNavigator;

/**
 * @author Christophe Fondacci
 */
public class ConnectionsView extends ViewPart {

	private Tree connectionsTree;
	// private TreeItem rootItem;
	private INavigatorConnector targetSetNavigator;
	private final static Log log = LogFactory.getLog(ConnectionsView.class);

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		connectionsTree = new Tree(parent, SWT.BORDER);
		VersionablePaintItemListener.handle(connectionsTree, false, false, true);
		refreshView();

		connectionsTree.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TreeItem i = connectionsTree.getItem(new Point(e.x, e.y));
				if (i != null && i.getData() instanceof ConnectionNavigator) {
					try {
						((INavigatorConnector) i.getData()).defaultAction();
					} catch (CancelException ex) {
						log.info(ex.getMessage());
					} catch (RuntimeException ex) {
						log.info(ex.getMessage(), ex);
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});
		// rootItem = new TreeItem(connectionsTree, SWT.NONE);
		// rootItem.setText("Database connections");
		// rootItem.setData(IElementType.getInstance("CONNECTION"));
		ISelectionProvider provider = TreeSelectionProvider.handle(connectionsTree, true);
		this.getSite().setSelectionProvider(provider);
		registerContextMenu(provider);
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(connectionsTree, "com.neXtep.designer.dbgm.ui.ConnectionsView"); //$NON-NLS-1$
	}

	private void registerContextMenu(ISelectionProvider provider) {
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);

		// this is to work around complaints about missing standard groups.
		contextMenu.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new GroupMarker("editions")); //$NON-NLS-1$
				manager.add(new Separator());
				manager.add(new GroupMarker("sql")); //$NON-NLS-1$
				manager.add(new GroupMarker("actions")); //$NON-NLS-1$
				manager.add(new Separator());
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

			}
		});

		getSite().registerContextMenu(contextMenu, provider);
		Menu menu = contextMenu.createContextMenu(connectionsTree);
		connectionsTree.setMenu(menu);
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		connectionsTree.setFocus();
	}

	private void refreshView() {
		connectionsTree.removeAll();
		targetSetNavigator = new TargetSetNavigator(VCSPlugin.getViewService()
				.getCurrentViewTargets(), UIControllerFactory.getController(IElementType
				.getInstance(ITargetSet.TYPE_ID)), connectionsTree);

		targetSetNavigator.create(null, -1);
		targetSetNavigator.initialize();
		targetSetNavigator.refreshConnector();
	}

}
