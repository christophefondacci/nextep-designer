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
package com.nextep.designer.sqlgen.services.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.factories.CaptureFactory;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.ErrorInfo;
import com.nextep.designer.sqlgen.model.ICapturer;
import com.nextep.designer.sqlgen.model.IMutableCaptureContext;
import com.nextep.designer.sqlgen.services.ICaptureService;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This service provides methods which retrieves a database structural information in the neXtep
 * database model.
 * 
 * @author Bruno Gautier - initial implementation
 * @author Christophe Fondacci - refactoring, added ICapturer methods, fixed monitors, NLS
 */
public final class CaptureService implements ICaptureService {

	private static final Log LOGGER = LogFactory.getLog(CaptureService.class);

	public static final String CAPTURER_EXTENSION_ID = "com.neXtep.designer.sqlgen.sqlCapturer"; //$NON-NLS-1$

	private static final int PCT_TABLES = 50;
	private static final int PCT_INDEXES = 10;
	private static final int PCT_VIEWS = 5;
	private static final int PCT_TRIGGERS = 5;
	private static final int PCT_SEQUENCES = 5;
	private static final int PCT_USERTYPES = 5;
	private static final int PCT_PROCEDURES = 10;
	private static final int PCT_SYNONYMS = 5;
	private static final int PCT_SPECIFICS = 5;

	@Override
	public Collection<IVersionable<?>> getContentsForCompletion(IConnection conn,
			IProgressMonitor monitor) {
		return getContentsFromDatabase(conn, true, monitor);
	}

	@Override
	public Collection<IVersionable<?>> getContentsFromDatabase(IConnection conn,
			IProgressMonitor mainMonitor) {
		return getContentsFromDatabase(conn, false, mainMonitor);
	}

	private Collection<IVersionable<?>> getContentsFromDatabase(IConnection conn, boolean simple,
			IProgressMonitor mainMonitor) {
		// Initializing our capturer
		IMutableCaptureContext context = CaptureFactory.createCaptureContext();
		final ICapturer capturer = initializeCapturer(conn, context);
		long start = 0;
		try {
			SubMonitor monitor = SubMonitor.convert(mainMonitor, 100);

			monitor.subTask(SQLGenMessages.getString("service.capture.retrievingTables")); //$NON-NLS-1$
			if (LOGGER.isDebugEnabled())
				start = System.currentTimeMillis();
			Collection<IBasicTable> tables = capturer.getTables(context,
					monitor.newChild(PCT_TABLES));
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("== [Tables] Total time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
						+ "ms for " + tables.size() + " tables =="); //$NON-NLS-1$ //$NON-NLS-2$

			/*
			 * We store the retrieved tables and columns in the capture context so they will be
			 * available for the following capture operations.
			 */
			for (IBasicTable table : tables) {
				context.addCapturedObject(table.getType(), table.getName(), table);
				for (IBasicColumn column : table.getColumns()) {
					context.addCapturedObject(column.getType(),
							CaptureHelper.getUniqueColumnName(column), column);
				}
				if (!simple) {
					for (IKeyConstraint key : table.getConstraints()) {
						context.addCapturedObject(key.getType(),
								CaptureHelper.getUniqueObjectName(table.getName(), key.getName()),
								key);
					}
				}
			}

			monitor.subTask(SQLGenMessages.getString("service.capture.retrievingViews")); //$NON-NLS-1$
			if (LOGGER.isDebugEnabled())
				start = System.currentTimeMillis();
			Collection<IView> views = capturer.getViews(context, monitor.newChild(PCT_VIEWS));
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("== [Views] Total time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
						+ "ms for " + views.size() + " views =="); //$NON-NLS-1$ //$NON-NLS-2$

			/*
			 * We store the retrieved views in the capture context so they will be available for the
			 * following capture operations.
			 */
			registerContext(context, views);

			if (!simple) {
				monitor.subTask(SQLGenMessages.getString("service.capture.retrievingIndexes")); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					start = System.currentTimeMillis();
				Collection<IIndex> indexes = capturer.getIndexes(context,
						monitor.newChild(PCT_INDEXES));
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("== [Indexes] Total time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
							+ "ms for " + indexes.size() + " indexes =="); //$NON-NLS-1$ //$NON-NLS-2$
				}
				/*
				 * We store the retrieved indexes in the capture context so they will be available
				 * for the following capture operations.
				 */
				for (IIndex index : indexes) {
					context.addCapturedObject(index.getType(),
							CaptureHelper.getUniqueIndexName(index), index);
				}
				monitor.subTask(SQLGenMessages.getString("service.capture.retrievingSequences")); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					start = System.currentTimeMillis();
				Collection<ISequence> sequences = capturer.getSequences(context,
						monitor.newChild(PCT_SEQUENCES));
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("== [Sequences] Total time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
							+ "ms for " + sequences.size() + " =="); //$NON-NLS-1$ //$NON-NLS-2$

				/*
				 * We store the retrieved sequences in the capture context so they will be available
				 * for the following capture operations.
				 */
				registerContext(context, sequences);

				monitor.subTask(SQLGenMessages.getString("service.capture.retrievingSynonyms")); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					start = System.currentTimeMillis();
				Collection<ISynonym> synonyms = capturer.getSynonyms(context,
						monitor.newChild(PCT_SYNONYMS));
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("== [Synonyms] Total time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
							+ "ms for " + synonyms.size() + " =="); //$NON-NLS-1$ //$NON-NLS-2$

				/*
				 * We store the retrieved synonyms in the capture context so they will be available
				 * for the following capture operations.
				 */
				registerContext(context, synonyms);

				monitor.subTask(SQLGenMessages.getString("service.capture.retrievingTriggers")); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					start = System.currentTimeMillis();
				Collection<ITrigger> triggers = capturer.getTriggers(context,
						monitor.newChild(PCT_TRIGGERS));
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("== [Triggers] Total time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
							+ "ms =="); //$NON-NLS-1$

				/*
				 * We store the retrieved triggers in the capture context so they will be available
				 * for the following capture operations.
				 */
				registerContext(context, triggers);

				monitor.subTask(SQLGenMessages.getString("service.capture.retrievingTypes")); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					start = System.currentTimeMillis();
				Collection<IUserType> userTypes = capturer.getUserTypes(context,
						monitor.newChild(PCT_USERTYPES));
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("== [UserTypes] Total time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
							+ "ms =="); //$NON-NLS-1$

				/*
				 * We store the retrieved user types in the capture context so they will be
				 * available for the following capture operations.
				 */
				registerContext(context, userTypes);
			}
			monitor.subTask(SQLGenMessages.getString("service.capture.retrievingProcedures")); //$NON-NLS-1$
			if (LOGGER.isDebugEnabled())
				start = System.currentTimeMillis();
			Collection<IProcedure> procedures = capturer.getProcedures(context,
					monitor.newChild(PCT_PROCEDURES));
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("== [Procedures] Total time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
						+ "ms =="); //$NON-NLS-1$

			/*
			 * We store the retrieved procedures in the capture context so they will be available
			 * for the following capture operations.
			 */
			registerContext(context, procedures);

			if (!simple) {
				monitor.subTask(SQLGenMessages.getString("service.capture.retrievingSpecifics")); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					start = System.currentTimeMillis();
				Collection<IDatabaseObject<?>> vendorObjects = capturer.getVendorSpecificDbObjects(
						context, monitor.newChild(PCT_SPECIFICS));
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("== [Vendor Objects] Total time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
							+ "ms =="); //$NON-NLS-1$

				/*
				 * We store the retrieved database objects in the capture context so they will be
				 * available for the following capture operations.
				 */
				registerContext(context, vendorObjects);
			}
			return context.getDbObjectsAsVersionables();
		} finally {
			capturer.release(context);
		}
	}

	private void registerContext(IMutableCaptureContext context,
			Collection<? extends IDatabaseObject<?>> objects) {
		for (IDatabaseObject<?> object : objects) {
			context.addCapturedObject(object.getType(), object.getName(), object);
		}
	}

	@Override
	public ICapturer getCapturer(DBVendor vendor) {
		IConfigurationElement conf = Designer.getInstance().getExtension(CAPTURER_EXTENSION_ID,
				"databaseVendor", vendor.name()); //$NON-NLS-1$
		if (conf == null) {
			return null;
			// throw new ErrorException(MessageFormat.format(
			//					SQLGenMessages.getString("show.errors.noCapturer"), vendor.name())); //$NON-NLS-1$
		} else {
			try {
				ICapturer capturer = (ICapturer) conf.createExecutableExtension("class"); //$NON-NLS-1$
				return capturer;
			} catch (CoreException e) {
				LOGGER.error(MessageFormat.format(
						"Error while instantiating capturer for vendor {0}", vendor.name())); //$NON-NLS-1$
				throw new ErrorException(e);
			}
		}
	}

	@Override
	public Collection<ErrorInfo> getErrorsFromDatabase(IConnection conn, IProgressMonitor monitor) {
		// Initializing our capturer
		IMutableCaptureContext context = CaptureFactory.createCaptureContext();
		final ICapturer capturer = initializeCapturer(conn, context);

		Collection<ErrorInfo> dbErrors = capturer.getDatabaseErrors(context);
		if (dbErrors == null) {
			dbErrors = Collections.emptyList();
		}
		return dbErrors;
	}

	private ICapturer initializeCapturer(IConnection conn, IMutableCaptureContext context) {
		ICapturer capturer = getCapturer(conn.getDBVendor());
		// Fallbacking on JDBC
		if (capturer == null) {
			capturer = getCapturer(DBVendor.JDBC);
		}
		capturer.initialize(conn, context);
		return capturer;
	}
}
