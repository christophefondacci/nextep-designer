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

import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.gui.model.AbstractNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * An UntypedNavigator provide default implementations of
 * helpful features such as initialization, child connector
 * management and recursive menu generator.
 * <br><br>
 * Graphically speaking, this connector will display all connectors
 * directly in the parent folder.
 *
 * @author Christophe Fondacci
 *
 */
public abstract class UntypedNavigator extends AbstractNavigator implements INavigatorConnector, IEventListener {



	protected UntypedNavigator(IObservable model,ITypedObjectUIController controller) {
		super(model,controller);
	}
	/**
	 * Creates the connector SWT control. This method has been
	 * added to mutualize process between <code>addConnector</code>
	 * and <code>initialize</code> methods. Its main purpose is to
	 * add the navigator as the TreeItem's data and to cascade
	 * <code>initialize</code> actions.
	 *
	 * @param c the connector for which the SWT control has to be created
	 * @param index index at which the control should be created in the tree, relatively to its parent
	 */
	protected void createConnector(INavigatorConnector c, int index) {
		TreeItem item = c.create(this.getSWTConnector(), index);
		item.setData(c);
		c.initialize();
	}


//	/**
//	 * Implementation of the getConnectorMenu which will browse
//	 * all TreeItems from this item to the root item. For each item it
//	 * will invoke the addConnectorMenuItems method to add parent menu
//	 * items to the contextual menu.
//	 */
//	public final Menu getConnectorMenu(Shell s) {
//		Menu menu = new Menu(s,SWT.NONE);
//		//We add this connector items first
//		this.addConnectorMenuItems(menu);
//		// Browsing tree items to add menu options
//		TreeItem parentItem = this.getSWTConnector().getParentItem();
//		while(parentItem != null ) {
//			Object itemData = parentItem.getData();
//			if(itemData instanceof INavigatorConnector) {
//				INavigatorConnector parentConnector = (INavigatorConnector)itemData;
//				//Adding separator
//				new MenuItem(menu,SWT.SEPARATOR);
//				//Adding parent items
//				parentConnector.addConnectorMenuItems(menu);
//			}
//			// Parsing tree hierarchy
//			parentItem = parentItem.getParentItem();
//		}
//		return menu;
//	}



}
