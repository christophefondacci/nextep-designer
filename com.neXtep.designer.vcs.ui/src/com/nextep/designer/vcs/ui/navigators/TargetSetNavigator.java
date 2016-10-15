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
package com.nextep.designer.vcs.ui.navigators;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.ITargetSet;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * @author Christophe Fondacci
 */
public class TargetSetNavigator extends UntypedNavigator {

	private TreeItem item;
	private Tree tree;

	public TargetSetNavigator(ITargetSet set, ITypedObjectUIController controller, Tree t) {
		super(set, controller);
		this.tree = t;
	}

	@Override
	public void initializeChildConnectors() {
		ITargetSet set = (ITargetSet) getModel();
		for (IConnection c : set.getConnections()) {
			addConnector(UIControllerFactory.getController(c.getType()).initializeNavigator(c));
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#createSWTConnector(org.eclipse.swt.widgets.TreeItem,
	 *      int)
	 */
	@Override
	protected TreeItem createSWTConnector(TreeItem parent, int treeIndex) {
		if (parent != null) {
			if (treeIndex != -1) {
				item = new TreeItem(parent, SWT.NONE, treeIndex);
			} else {
				item = new TreeItem(parent, SWT.NONE);
			}
		} else {
			item = new TreeItem(tree, SWT.NONE);
		}
		item.setImage(getConnectorIcon());
		item.setData(this);

		return item;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public TreeItem getSWTConnector() {
		return item;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		return VCSUIMessages.getString("navigator.targetSet.title"); //$NON-NLS-1$
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case CONNECTION_ADDED:
			try {
				// Only adding if the connector does not exist (exception)
				INavigatorConnector conn = getConnector(data);
				if (conn == null) {
					addConnector(UIControllerFactory.getController(data).initializeNavigator(data));
				}
			} catch (ErrorException e) {
				addConnector(UIControllerFactory.getController(data).initializeNavigator(data));
			}
			break;
		case CONNECTION_REMOVED:
			removeConnector(getConnector(data));
			break;
		}
		refreshConnector();
	}

	/**
	 * Overridden to expand all nodes
	 * 
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();
		expandAll(item);
	}

	private void expandAll(TreeItem i) {
		i.setExpanded(true);
		for (TreeItem child : i.getItems()) {
			expandAll(child);
		}
	}

	@Override
	public void setModel(Object model) {
		super.setModel(model);
		refreshConnector();
	}
}
