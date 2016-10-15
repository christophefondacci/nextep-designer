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
package com.nextep.designer.sqlgen.model;

import java.sql.DatabaseMetaData;
import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.designer.core.model.IConnection;

/**
 * A capturer is the interface between the database and the workspace which can retrieve and provide
 * information from a database in the workspace's model.<br>
 * Note that capturers are statefull, meaning that a new capturer instance is initialized each time
 * the environment needs to fetch information from a database.
 * 
 * @author Christophe Fondacci
 */
public interface ICapturer {

	/**
	 * Defines the connection to the data source that holds the objects that must be captured by
	 * this capturer.
	 * 
	 * @param conn a {@link IConnection} object
	 */
	// void setConnection(IConnection conn);

	/**
	 * Returns the connection to the data source that holds the objects that must be captured by
	 * this capturer.
	 */
	// IConnection getConnection();

	/**
	 * This method must be called right after instantiating the capturer to let it initialize
	 * everything that may be necessary to this capturer to run properly.
	 * 
	 * @param conn a {@link IConnection} representing the data source that holds the objects that
	 *        must be captured by this capturer
	 * @param context a {@link IMutableCaptureContext} representing the context that will be passed
	 *        to this capturer during the capture process
	 */
	void initialize(IConnection conn, IMutableCaptureContext context);

	/**
	 * This method must be called after the capture process to release any resources that might be
	 * held by this capturer.
	 * 
	 * @param context a {@link IMutableCaptureContext} representing the context that has been be
	 *        passed to this capturer during the capture process
	 */
	void release(IMutableCaptureContext context);

	/**
	 * Returns a <code>Collection</code> of the tables found in the data source pointed to by the
	 * connection object provided by the specified <code>context</code> and notifies the specified
	 * <code>monitor</code> while capturing.<br>
	 * The captured tables must be completely defined with their columns, primary key and foreign
	 * keys.
	 * 
	 * @param context a {@link ICaptureContext} to get the connection object to the data source and
	 *        the list of previously captured objects
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a {@link Collection} of {@link IBasicTable} objects
	 */
	Collection<IBasicTable> getTables(ICaptureContext context, IProgressMonitor monitor);

	/**
	 * Returns a <code>Collection</code> of the indexes found in the data source pointed to by the
	 * connection object provided by the specified <code>context</code> and notifies the specified
	 * <code>monitor</code> while capturing.
	 * 
	 * @param context a {@link ICaptureContext} to get the connection object to the data source and
	 *        the list of previously captured objects
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a {@link Collection} of {@link IIndex} objects
	 */
	Collection<IIndex> getIndexes(ICaptureContext context, IProgressMonitor monitor);

	/**
	 * Returns a <code>Collection</code> of the views found in the data source pointed to by the
	 * connection object provided by the specified <code>context</code> and notifies the specified
	 * <code>monitor</code> while capturing.
	 * 
	 * @param context a {@link ICaptureContext} to get the connection object to the data source and
	 *        the list of previously captured objects
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a {@link Collection} of {@link IView} objects
	 */
	Collection<IView> getViews(ICaptureContext context, IProgressMonitor monitor);

	/**
	 * Returns a <code>Collection</code> of the triggers found in the data source pointed to by the
	 * connection object provided by the specified <code>context</code> and notifies the specified
	 * <code>monitor</code> while capturing.
	 * 
	 * @param context a {@link ICaptureContext} to get the connection object to the data source and
	 *        the list of previously captured objects
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a {@link Collection} of {@link ITrigger} objects
	 */
	Collection<ITrigger> getTriggers(ICaptureContext context, IProgressMonitor monitor);

	/**
	 * Returns a <code>Collection</code> of the synonyms found in the data source pointed to by the
	 * connection object provided by the specified <code>context</code> and notifies the specified
	 * <code>monitor</code> while capturing.
	 * 
	 * @param context a {@link ICaptureContext} to get the connection object to the data source and
	 *        the list of previously captured objects
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a {@link Collection} of {@link ITrigger} objects
	 */
	Collection<ISynonym> getSynonyms(ICaptureContext context, IProgressMonitor monitor);

	/**
	 * Returns a <code>Collection</code> of the procedures found in the data source pointed to by
	 * the connection object provided by the specified <code>context</code> and notifies the
	 * specified <code>monitor</code> while capturing.
	 * 
	 * @param context a {@link ICaptureContext} to get the connection object to the data source and
	 *        the list of previously captured objects
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a {@link Collection} of {@link ITrigger} objects
	 */
	Collection<IProcedure> getProcedures(ICaptureContext context, IProgressMonitor monitor);

	/**
	 * Returns a <code>Collection</code> of the sequences found in the data source pointed to by the
	 * connection object provided by the specified <code>context</code> and notifies the specified
	 * <code>monitor</code> while capturing.
	 * 
	 * @param context a {@link ICaptureContext} to get the connection object to the data source and
	 *        the list of previously captured objects
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a {@link Collection} of {@link ITrigger} objects
	 */
	Collection<ISequence> getSequences(ICaptureContext context, IProgressMonitor monitor);

	/**
	 * Returns a <code>Collection</code> of the user types found in the data source pointed to by
	 * the connection object provided by the specified <code>context</code> and notifies the
	 * specified <code>monitor</code> while capturing.
	 * 
	 * @param context a {@link ICaptureContext} to get the connection object to the data source and
	 *        the list of previously captured objects
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a {@link Collection} of {@link ITrigger} objects
	 */
	Collection<IUserType> getUserTypes(ICaptureContext context, IProgressMonitor monitor);

	/**
	 * Returns a <code>Collection</code> of the objects found in the data source pointed to by the
	 * connection object provided by the specified <code>context</code> and notifies the specified
	 * <code>monitor</code> while capturing.<br>
	 * The objects returned by this method are all the objects that cannot be retrieved via the
	 * standard JDBC {@link DatabaseMetaData} interface.
	 * 
	 * @param context a {@link ICaptureContext} to get the connection object to the data source and
	 *        the list of previously captured objects
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a {@link Collection} of {@link IDatabaseObject} objects
	 */
	Collection<IDatabaseObject<?>> getVendorSpecificDbObjects(ICaptureContext context,
			IProgressMonitor monitor);

	/**
	 * Retrieves the errors from the current connection initialized for this capturer.
	 * 
	 * @return a collection of {@link ErrorInfo} representing the database errors
	 */
	Collection<ErrorInfo> getDatabaseErrors(ICaptureContext context);

}
