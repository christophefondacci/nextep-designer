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
package com.nextep.datadesigner.gui.impl.navigators;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IProperty;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * @author Christophe Fondacci
 */
public class PropertyNavigator extends UntypedNavigator {

	private Tree tree;
	private TreeItem item;

	public PropertyNavigator(IProperty property, Tree tree) {
		super(property, null);
		this.tree = tree;
	}

	@Override
	public void initializeChildConnectors() {
		final IProperty property = (IProperty) getModel();
		for (IProperty p : property.getChildren()) {
			// Checking if we have a numeric property, in which case we won't sort
			boolean sort = true;
			try {
				Integer.valueOf(p.getName());
				sort = false;
			} catch (NumberFormatException e) {
				sort = true;
			}
			addConnector(createPropertyNavigator(p, tree), sort);
		}
	}

	/**
	 * Creates the child navigator connector. Extensions may override to provide different
	 * navigators.
	 * 
	 * @param p property that the navigator will display
	 * @param parent parent tree in which items are displayed
	 * @return a property navigator.
	 */
	protected PropertyNavigator createPropertyNavigator(IProperty p, Tree parent) {
		return new PropertyNavigator(p, parent);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		final IProperty property = (IProperty) getModel();
		item.setText(notNull(property.getName()));
		item.setText(1, notNull(property.getValue()));
		for (INavigatorConnector c : getConnectors()) {
			c.refreshConnector();
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#createSWTConnector(org.eclipse.swt.widgets.TreeItem,
	 *      int)
	 */
	@Override
	protected TreeItem createSWTConnector(TreeItem parent, int treeIndex) {
		final IProperty property = (IProperty) getModel();
		if (parent == null) {
			item = new TreeItem(tree, SWT.NONE);
		} else {
			item = new TreeItem(parent, SWT.NONE);
		}
		// Property image
		try {
			item.setImage(ImageFactory.getImage(property.getType().getIcon()));
		} catch (ClassCastException e) {
			// Happens for some rare non-typed properties
			item.setImage(ImageFactory.ICON_ATTRIBUTE);
		}

		return item;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getSWTConnector()
	 */
	@Override
	public TreeItem getSWTConnector() {
		return item;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getTitle()
	 */
	@Override
	public String getTitle() {
		final IProperty property = (IProperty) getModel();
		return property.getName();
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// TODO Auto-generated method stub

	}

}
