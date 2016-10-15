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
package com.nextep.datadesigner.vcs.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.ui.VCSImages;

public class VersionViewNavigator extends TypedNavigator implements IEventListener {

	// private static final Log log = LogFactory.getLog(VersionViewNavigator.class);
	private TreeItem viewItem = null;
	private Tree tree;

	public VersionViewNavigator(IWorkspace view, ITypedObjectUIController controller, Tree tree) {
		super(view, controller);
		this.tree = tree;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initializeChildConnectors() {
		IWorkspace view = (IWorkspace) getModel();
		// Adding connectors for subitems
		List<IVersionable<?>> contentList = new ArrayList<IVersionable<?>>(view.getContents());
		Collections.sort(contentList);
		for (IVersionable<?> v : view.getContents()) {
			this.addConnector(UIControllerFactory.getController(
					IElementType.getInstance("VERSIONABLE")).initializeNavigator(v));
			// Forcing consistency
			if (v.getContainer() != view) {
				try {
					Observable.deactivateListeners();
					v.setContainer(view);
				} finally {
					Observable.activateListeners();
				}

			}
		}
	}

	@Override
	protected TreeItem createSWTConnector(TreeItem parent, int treeIndex) {
		// A version view navigator can be a root element
		// so we check if parent is null to build our first
		// root item
		if (parent == null) {
			// This is our root item, we build from tree
			viewItem = new TreeItem(tree, SWT.NONE);
		} else {
			// The view is encapsulated in a larger tree
			viewItem = new TreeItem(parent, SWT.NONE);
		}
		viewItem.setImage(ImageFactory.getImage(getType().getIcon()));
		viewItem.setData(this);
		return viewItem;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return VCSImages.ICON_VIEW;
	}

	@Override
	public TreeItem getSWTConnector() {
		return viewItem;
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance("VIEW");
	}

	// @Override
	// public void defaultAction() {
	// // TODO Auto-generated method stub
	//
	// }
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case VERSIONABLE_ADDED:
			this.addConnector(new VersionableNavigator((IVersionable<?>) data, UIControllerFactory
					.getController(IElementType.getInstance("VERSIONABLE"))));
			// initialize();
			break;
		case VERSIONABLE_REMOVED:
			// INavigatorConnector c = this.getConnector(data);
			// c.getSWTConnector().dispose();
			// this.removeConnector(c);
			INavigatorConnector c = this.getConnector((IVersionable<?>) data);
			this.removeConnector(c);
			break;
		}
		refreshConnector();

	}

}
