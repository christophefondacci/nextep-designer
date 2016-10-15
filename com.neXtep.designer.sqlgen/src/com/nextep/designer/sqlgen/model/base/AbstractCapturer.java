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
package com.nextep.designer.sqlgen.model.base;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.sqlgen.model.ErrorInfo;
import com.nextep.designer.sqlgen.model.ICaptureContext;
import com.nextep.designer.sqlgen.model.ICapturer;
import com.nextep.designer.sqlgen.model.IMutableCaptureContext;

/**
 * Basic common capturer methods implementation.
 * 
 * @author Bruno Gautier
 * @author Christophe Fondacci
 */
public abstract class AbstractCapturer implements ICapturer {

	private static final Log LOGGER = LogFactory.getLog(AbstractCapturer.class);

	protected static final String ATTR_CATALOG = "CATALOG"; //$NON-NLS-1$
	protected static final String ATTR_SCHEMA = "SCHEMA"; //$NON-NLS-1$

	@Override
	public void initialize(IConnection conn, IMutableCaptureContext context) {
		try {
			IDatabaseConnector connector = CorePlugin.getConnectionService().getDatabaseConnector(
					conn);
			Connection c = connector.connect(conn);
			context.setConnectionObject(c);
			context.setSchema(connector.getSchema(conn));
			context.setCatalog(connector.getCatalog(conn));
			context.setConnection(conn);
		} catch (SQLException sqle) {
			throw new ErrorException("Unable to connect to the " + conn.getDBVendor()
					+ " database: " + sqle.getMessage(), sqle);
		}
	}

	@Override
	public void release(IMutableCaptureContext context) {
		Connection conn = (Connection) context.getConnectionObject();
		try {
			conn.close();
		} catch (SQLException sqle) {
			LOGGER.warn("Unable to close JDBC connection: " + sqle.getMessage(), sqle);
		}
	}

	@Override
	public Collection<IUserType> getUserTypes(ICaptureContext context, IProgressMonitor monitor) {
		LOGGER.info("User types ignored, no capture implementation available");
		return Collections.emptyList();
	}

	@Override
	public Collection<IDatabaseObject<?>> getVendorSpecificDbObjects(ICaptureContext context,
			IProgressMonitor monitor) {
		return Collections.emptyList();
	}

	@Override
	public Collection<ISequence> getSequences(ICaptureContext context, IProgressMonitor monitor) {
		LOGGER.info("Sequences ignored, no capture implementation available");
		return Collections.emptyList();
	}

	@Override
	public Collection<ISynonym> getSynonyms(ICaptureContext context, IProgressMonitor monitor) {
		LOGGER.info("Synonyms ignored, no capture implementation available");
		return Collections.emptyList();
	}

	@Override
	public Collection<IProcedure> getProcedures(ICaptureContext context, IProgressMonitor monitor) {
		LOGGER.info("Procedures ignored, no capture implementation available");
		return Collections.emptyList();
	}

	@Override
	public Collection<IView> getViews(ICaptureContext context, IProgressMonitor monitor) {
		LOGGER.info("Views ignored, no capture implementation available");
		return Collections.emptyList();
	}

	@Override
	public Collection<ITrigger> getTriggers(ICaptureContext context, IProgressMonitor monitor) {
		LOGGER.info("Triggers ignored, no capture implementation available");
		return Collections.emptyList();
	}

	@Override
	public Collection<ErrorInfo> getDatabaseErrors(ICaptureContext context) {
		return Collections.emptyList();
	}

}
