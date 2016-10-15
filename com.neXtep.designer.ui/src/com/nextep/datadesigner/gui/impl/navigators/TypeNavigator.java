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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;

public class TypeNavigator extends UntypedNavigator implements INavigatorConnector,
		SelectionListener {

	private static final Log log = LogFactory.getLog(TypeNavigator.class);
	private TreeItem typeItem;
	private MenuItem createItem;
	private IElementType type;
	private String customLabel;

	public TypeNavigator(IElementType type) {
		super(null, null);
		this.type = type;
	}

	public TypeNavigator(IElementType type, String customLabel) {
		super(null, null);
		this.type = type;
		this.customLabel = customLabel;
	}

	public List<MenuItem> addConnectorMenuItems(Menu parentMenu) {
		// Initializing item list
		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		// Adding items
		createItem = new MenuItem(parentMenu, SWT.NONE);
		createItem.setText("Create new " + type.getName());
		createItem.setImage(ImageFactory.getImage(type.getCategoryIcon()));
		createItem.setData(this);
		createItem.addSelectionListener(this);
		menuItems.add(createItem);
		return menuItems;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return ImageFactory.getImage(type.getCategoryIcon());
	}

	protected TreeItem createSWTConnector(TreeItem parent, int treeIndex) {
		if (type != IElementType.getInstance("CONTAINER")) {
			if (treeIndex == -1) {
				typeItem = new TreeItem(parent, SWT.NONE);
			} else {
				typeItem = new TreeItem(parent, SWT.NONE, treeIndex);
			}
			typeItem.setImage(ImageFactory.getImage(type.getCategoryIcon()));
			typeItem.setData(this);
		} else {
			typeItem = parent;
		}
		refreshConnector();
		return typeItem;
	}

	/**
	 * We override default comparison to make sure container types are always placed before standard
	 * types.
	 */
	@Override
	public int compareTo(INavigatorConnector o) {
		if (getType() == IElementType.getInstance("CONTAINER")
				&& o.getType() != IElementType.getInstance("CONTAINER")) {
			return -1;
		} else if (getType() != IElementType.getInstance("CONTAINER")
				&& o.getType() == IElementType.getInstance("CONTAINER")) {
			return 1;
		} else {
			return super.compareTo(o);
		}
	}

	public void defaultAction() {
		// TODO Auto-generated method stub

	}

	public TreeItem getSWTConnector() {
		return typeItem;
	}

	public IElementType getType() {
		return type;
	}

	public void refreshConnector() {
		if (customLabel != null) {
			typeItem.setText(customLabel);
		} else {
			typeItem.setText(type.getCategoryTitle());
		}
	}

	public void widgetDefaultSelected(SelectionEvent arg0) {
		widgetSelected(arg0);

	}

	public void widgetSelected(SelectionEvent arg0) {
		if (arg0.getSource() == createItem) {
			try {
				INavigatorConnector parent = (INavigatorConnector) typeItem.getParentItem()
						.getData();
				UIControllerFactory.getController(type).newInstance(parent.getModel());
			} catch (CancelException e) {
				log.info(e.getMessage());
			}
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getModel()
	 */
	public Object getModel() {
		return type;
	}

	public void setModel(Object model) {
		type = (IElementType) model;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getTitle()
	 */
	public String getTitle() {
		return type.getCategoryTitle();
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// TODO Auto-generated method stub

	}

}
