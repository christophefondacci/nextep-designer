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
package com.nextep.designer.core.model;

import java.util.Collection;

/**
 * This interface defines elements which can contain a set of connections
 * 
 * @author Christophe Fondacci
 */
public interface IConnectionContainer {

	/**
	 * Adds a connection to this container
	 * 
	 * @param connection the {@link IConnection} to add
	 */
	void addConnection(IConnection connection);

	/**
	 * Removes a connection from this container
	 * 
	 * @param connection the {@link IConnection} to remove
	 */
	void removeConnection(IConnection connection);

	/**
	 * Lists all connections of this container
	 * 
	 * @return a Collection of all {@link IConnection} of this container
	 */
	Collection<IConnection> getConnections();

	/**
	 * Replace the whole set of {@link IConnection} of this container
	 * 
	 * @param connections new set of connections
	 */
	void setConnections(Collection<IConnection> connections);
}
