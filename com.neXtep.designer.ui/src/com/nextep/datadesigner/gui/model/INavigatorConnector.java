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
package com.nextep.datadesigner.gui.model;

import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.ITypedObject;

/**
 * This interface represents a navigator connector.
 * Every element which has to be displayed inside a
 * "navigator" should provide such a connector.<br>
 * 
 * This interface should not be implemented directly.
 * Clients must rather extend one of the provided 
 * abstract implementation : {@link TypedNavigator},
 * {@link UntypedNavigator} or {@link AbstractNavigator}.
 *
 * @author Christophe Fondacci
 *
 */
public interface INavigatorConnector extends IConnector<TreeItem,INavigatorConnector>, IEventListener,
	DisposeListener, Comparable<INavigatorConnector>, ITypedObject {

	/**
	 * Creates the SWT tree connector given its parent.
	 * This method <b>must not</b> bother with child connector creation
	 * and must not call the <code>createSWTConnector</code> method
	 * on its own child connectors.
	 * The children SWT connector creation will be performed by
	 * abstract implementors <code>TypedNavigator</code> or
	 * <code>UntypedNavigator</code>. <br>
	 * All implementors should extend one of these 2 class.
	 *
	 * @param parent parent item in the tree
	 * @param treeIndex index at which the item should be created
	 */
	public abstract TreeItem create(TreeItem parent, int treeIndex);
	/**
	 * The default action of a connector. This action will be
	 * executed when double-clicking the connector
	 */
	public abstract void defaultAction();
	/**
	 * Retrieves the child connector associated with the specified 
	 * model and returns it.
	 * @param model the model for which this connector should have
	 * 		  a child connector.
	 * @return the connector representing the specified model.
	 * @throws ErrorException if no connector exists for the specified model
	 */
	public abstract INavigatorConnector getConnector(Object model);
	/**
	 * A possibility to define the SWT parent Tree control which
	 * will contain the SWT connector's control. If a user defines
	 * the tree control of a navigator, the framework will be
	 * able to create root items (navigators which don't have any
	 * parent, the first of the hierarchy).
	 *  
	 * @param tree the SWT Tree control containing the navigator's SWT controls
	 */
	public abstract void setTree(Tree tree);
	/**
	 * Defines the parent connector of a connector.
	 * 
	 * @param parent the parent connector.
	 */
	public abstract void setParent(INavigatorConnector parent);
	/**
	 * @return the parent connector of this connector or <code>null</code>
	 * 		   if this connector is a root connector.
	 */
	public abstract INavigatorConnector getParent();
}
