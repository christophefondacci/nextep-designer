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
package com.nextep.designer.synch.services;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.synch.SynchPlugin;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This interface describes features offered by the synchronization service.
 * Concrete implementation of this service may be obtained by calling
 * {@link SynchPlugin#getSynchronisationService()} method.
 * 
 * @author Christophe Fondacci
 */
public interface ISynchronizationService {

	/**
	 * Starts a synchronisation of the specified container against the given
	 * database connection.<br>
	 * Any registered {@link ISynchronizationListener} will be notified when the
	 * synchronization has been done.
	 * 
	 * @param container
	 *            container to use as the set of elements to synchronize
	 * @param connection
	 *            an {@link IConnection} database connection
	 */
	void synchronize(IVersionContainer container, IConnection connection, IProgressMonitor monitor);

	/**
	 * Builds the synchronization result without notifying anyone. This method
	 * is provided for manual programming synchronization features.
	 * 
	 * @param container
	 *            container to synchronize with the database
	 * @param connection
	 *            the {@link IConnection} to use for this synchronization
	 * @param scope
	 *            synchronization {@link ComparisonScope}
	 * @param monitor
	 *            a {@link IProgressMonitor} to report progress to
	 * @return the {@link ISynchronizationResult}
	 */
	ISynchronizationResult buildSynchronizationResult(IVersionContainer container,
			IConnection connection, ComparisonScope scope, IProgressMonitor monitor);

	/**
	 * Adds a new listener which will be notified of all synchronization events.
	 * 
	 * @param listener
	 *            a {@link ISynchronizationListener} to listen to
	 *            synchronization events
	 */
	void addSynchronizationListener(ISynchronizationListener listener);

	/**
	 * Removes the specified listener from synchronization notifications.
	 * 
	 * @param listener
	 *            a {@link ISynchronizationListener} to remove from
	 *            notifications
	 */
	void removeSynchronizationListener(ISynchronizationListener listener);

	/**
	 * Generates the current content of the {@link ISynchronizationResult} bean
	 * into a SQL script. The newly created script will be injected in the given
	 * result.
	 * 
	 * @param synchResult
	 *            synchronization result of a former synchronization
	 * @param monitor
	 *            the {@link IProgressMonitor} to report work to
	 */
	void buildScript(ISynchronizationResult synchResult, IProgressMonitor monitor);

	/**
	 * Changes the scope of synchronization for the given
	 * {@link ISynchronizationResult}. This method differs from the
	 * {@link ISynchronizationService#synchronize(IVersionContainer, IConnection, IProgressMonitor)}
	 * because it will not re-capture database contents but will apply this
	 * change on an already captured database. However, since the scope changes
	 * a new comparison / merge will be performed
	 * 
	 * @param scope
	 *            new synchronization scope
	 * @see ComparisonScope
	 */
	void changeSynchronizationScope(ComparisonScope scope, ISynchronizationResult result,
			IProgressMonitor monitor);

	/**
	 * Clears any current synchronization information
	 */
	void clearSynchronization();

	/**
	 * Initiates a data synchronization between the specified container and the
	 * supplied database connection.
	 * 
	 * @param conn
	 *            the database {@link IConnection} to use for this data
	 *            synchronization
	 * @param monitor
	 *            a {@link IProgressMonitor} to report progress to
	 * @param synchronizedItems
	 *            items to synchronize data for
	 */
	void synchronizeData(IConnection conn, IProgressMonitor monitor,
			IVersionable<?>... synchronizedItems);
}
