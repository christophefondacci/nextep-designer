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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.gui.model.AbstractNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * A TypedNavigator provide default implementations of
 * helpful features such as initialization, child connector
 * management and recursive menu generator.
 * <br><br>
 * Graphically speaking, this connector will display all connectors
 * within a parent "typed" folder.
 *
 * @author Christophe Fondacci
 *
 */
public abstract class TypedNavigator extends AbstractNavigator implements INavigatorConnector,IEventListener {

	private Map<IElementType,TypeNavigator> typeItemsMap = null;

	protected TypedNavigator(IObservable model,ITypedObjectUIController controller) {
		super(model,controller);
		typeItemsMap = new HashMap<IElementType,TypeNavigator>();
	}
	/**
	 * Adds a new static type item to this navigator.
	 *
	 * @param type type to statically add
	 */
	protected void addType(IElementType type) {
		TypeNavigator nav = typeItemsMap.get(type);
		if(nav == null ) {
			nav = new TypeNavigator(type);
			nav.createSWTConnector(this.getSWTConnector(), -1);
			nav.setParent(this);
			nav.initialize();
			typeItemsMap.put(type,nav);
		}
	}


	/**
	 * Adds a tree item to the tree within the appropriate
	 * type subitem. Note that this kind of navigator will
	 * NEVER create the TreeItem, instead it will create the
	 * type node if needed and delegates the TreeItem creation
	 * to the <code>TypeNavigator</code> connector.
	 *
	 * @param c connector for which the TreeItem has to be created
	 * @param treeIndex index of the created item in the tree, relatively to its parent
	 */
	protected void createConnector(INavigatorConnector c, int treeIndex) {
		IElementType type = c.getType();
		TypeNavigator nav = typeItemsMap.get(type);
		if( nav == null ) {
			nav = new TypeNavigator(type);
			typeItemsMap.put(type, nav);
			List<INavigatorConnector> types = new ArrayList<INavigatorConnector>(typeItemsMap.values());
			Collections.sort(types);
			nav.createSWTConnector(this.getSWTConnector(), types.indexOf(nav));
			nav.setParent(this);
			nav.initialize();
			//this.addConnector(nav);

		}

		if(c.getSWTConnector()==null) {
			nav.addConnector(c);
		}
	}

	/**
	 * Default management of remove connector
	 */
	public final void removeConnector(INavigatorConnector c) {
		if(c==null) {
			return;
		}
		//Dispatching connector removal to type parent connector
		INavigatorConnector parentType = typeItemsMap.get(c.getType());
		if(parentType!=null) {
			parentType.removeConnector(c);
		}
		//Removing locally
		super.removeConnector(c);
	}

}
