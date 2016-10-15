package com.nextep.designer.headless.standalone;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import com.nextep.datadesigner.vcs.impl.MergeStrategy;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.headless.helpers.HeadlessHelper;
import com.nextep.designer.headless.model.HeadlessConstants;
import com.nextep.designer.headless.model.impl.HeadlessProgressMonitor;
import com.nextep.designer.headless.standalone.services.ISerializationService;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.services.ICaptureService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.impl.Workspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * This application is the main entry-point of the headless "disconnected" neXtep command-line
 * program. Disconnected means that all neXtep features are provided without repository connection.
 */
public class StandaloneApplication implements IApplication {

	private static final Log STDOUT_LOGGER = LogFactory.getLog("OUT"); //$NON-NLS-1$
	private static final Log LOGGER = LogFactory.getLog(StandaloneApplication.class);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		// Program header
		STDOUT_LOGGER
				.info(StandaloneMessages.getString("application.headerLine1") + new Date() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		STDOUT_LOGGER.info(StandaloneMessages.getString("application.headerLine2") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		STDOUT_LOGGER.info("\n"); //$NON-NLS-1$
		// Fetching services
		final ICaptureService captureService = SQLGenPlugin.getService(ICaptureService.class);
		final IWorkspaceService viewService = VCSPlugin.getViewService();

		// Initializing dummy view for headless mode to be properly configured
		final IWorkspace view = new Workspace("headless", "description"); //$NON-NLS-1$ //$NON-NLS-2$
		view.setDBVendor(DBVendor.MYSQL);
		viewService.setCurrentWorkspace(view);

		// Processing command line arguments
		final Map<String, String> argsMap = HeadlessHelper.processArgs(context);
		final String outfile = argsMap.get(HeadlessConstants.EXPORT_FILENAME);
		final IConnection conn = HeadlessHelper.getConnection(HeadlessConstants.TARGET_CONTEXT,
				argsMap);

		LOGGER.info(MessageFormat.format(
				StandaloneMessages.getString("application.dumpingStructureMsg"), conn.toString())); //$NON-NLS-1$
		Collection<IVersionable<?>> objects = captureService.getContentsFromDatabase(conn,
				new HeadlessProgressMonitor(true));
		STDOUT_LOGGER.info("\n"); //$NON-NLS-1$
		LOGGER.info(StandaloneMessages.getString("application.generatingExport")); //$NON-NLS-1$
		final Collection<IVersionable<?>> sources = Collections.emptyList();
		final List<IComparisonItem> items = VCSPlugin.getComparisonManager().compare(sources,
				objects, MergeStrategy.create(ComparisonScope.DB_TO_REPOSITORY), true);
		LOGGER.info(MessageFormat.format(
				StandaloneMessages.getString("application.generatingFileMsg"), outfile)); //$NON-NLS-1$
		File f = new File(outfile);
		final ISerializationService serializer = StandalonePlugin
				.getService(ISerializationService.class);
		serializer.serializeFile(items, f);
		LOGGER.info(MessageFormat.format(
				StandaloneMessages.getString("application.structureDumpedMsg"), outfile)); //$NON-NLS-1$
		LOGGER.info(StandaloneMessages.getString("application.bye")); //$NON-NLS-1$
		return IApplication.EXIT_OK;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		// nothing to do
	}

}
