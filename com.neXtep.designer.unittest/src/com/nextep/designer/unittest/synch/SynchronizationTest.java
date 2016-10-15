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
package com.nextep.designer.unittest.synch;

import java.util.Collection;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.sqlgen.ui.model.SubmitionManager;
import com.nextep.designer.synch.SynchPlugin;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.services.ISynchronizationService;
import com.nextep.designer.unittest.Activator;
import com.nextep.designer.unittest.helpers.TestHelper;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

public class SynchronizationTest extends TestCase {

	private final static Log log = LogFactory.getLog(SynchronizationTest.class);
	private final static String PROP_USER = "target.user"; //$NON-NLS-1$
	private final static String PROP_PASSWORD = "target.password"; //$NON-NLS-1$
	private final static String PROP_INSTANCE = "target.instance"; //$NON-NLS-1$
	private final static String PROP_DATABASE = "target.database"; //$NON-NLS-1$
	private final static String PROP_PORT = "target.port"; //$NON-NLS-1$
	private final static String PROP_SERVER = "target.server"; //$NON-NLS-1$
	private ISynchronizationResult synchResult = null;

	@Override
	protected void runTest() throws Throwable {
		final ISynchronizationService synchService = SynchPlugin
				.getService(ISynchronizationService.class);

		// This listener registers the synchronization result
		synchService.addSynchronizationListener(new ISynchronizationListener() {

			@Override
			public void scopeChanged(ComparisonScope scope) {

			}

			@Override
			public void newSynchronization(ISynchronizationResult synchronizationResult) {
				synchResult = synchronizationResult;
			}
		});

		// Synchronizing workspace
		final IConnection targetConnection = buildConnection();
		final IWorkspace workspace = VCSPlugin.getViewService().getCurrentWorkspace();
		synchService.synchronize(workspace, targetConnection, null);
		// We should have a result
		Assert.assertNotNull("Synchronization didn't provide a result", synchResult); //$NON-NLS-1$
		// Now we will submit the result to our database
		final ISQLScript script = synchResult.getGeneratedScript();
		log.info("Submitting following SQL :\n" + script.getSql()); //$NON-NLS-1$
		SubmitionManager.submit(synchResult.getConnection(), script,
				synchResult.getGenerationResult(), new NullProgressMonitor());
		// Now we will do a synchronization again
		synchResult = null;
		synchService.synchronize(workspace, targetConnection, null);
		checkNoDifference(synchResult);

		// Data synchronization
		final IVersionable<?> dataSetV = TestHelper.getVersionableByName("DEPARTMENTS",
				IElementType.getInstance(IDataSet.TYPE_ID), TestHelper.getFirstContainer());
		final IDataSet dataSet = (IDataSet) dataSetV.getVersionnedObject().getModel();
		synchResult = null;
		synchService.synchronizeData(targetConnection, null,
				VersionHelper.getVersionable(dataSet.getTable()));
		Assert.assertNotNull("Data synchronization didn't provide a result", synchResult); //$NON-NLS-1$
		synchService.buildScript(synchResult, null);
		Assert.assertNotNull("Data script has not been built after explicit build",
				synchResult.getGeneratedScript());

		// Now we will submit the result to our database
		final ISQLScript dataScript = synchResult.getGeneratedScript();
		log.info("Submitting following data SQL script :\n" + dataScript.getSql()); //$NON-NLS-1$
		SubmitionManager.submit(synchResult.getConnection(), dataScript,
				synchResult.getGenerationResult(), new NullProgressMonitor());

		// Synchronizing again to check that everything is synched
		synchService.synchronizeData(targetConnection, null,
				VersionHelper.getVersionable(dataSet.getTable()));
		checkNoDifference(synchResult);
	}

	private void checkNoDifference(ISynchronizationResult synchResult) {
		// We should have a result
		Assert.assertNotNull("Second synchronization didn't provide a result", synchResult); //$NON-NLS-1$
		final Collection<IComparisonItem> items = synchResult.getComparedItems();
		// We will now check that no difference could be found
		for (IComparisonItem item : items) {
			// Ignoring MISSING_SOURCE (as mat view may create internal index)
			// and materialized view
			// as synchronization is not yet supported because of Oracle rewrite
			if (item.getDifferenceType() != DifferenceType.MISSING_SOURCE
					&& item.getType() != IElementType.getInstance(IMaterializedView.VIEW_TYPE_ID)
					&& item.getType() != IElementType.getInstance(IDataSet.TYPE_ID)) {

				Assert.assertTrue(
						"A difference has been found after synchronizing the view: " + item.toString(), //$NON-NLS-1$
						DifferenceType.EQUALS == item.getDifferenceType());
			}
		}
	}

	private IConnection buildConnection() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		final DBVendor vendor = VCSPlugin.getService(IWorkspaceService.class).getCurrentWorkspace()
				.getDBVendor();
		final String prefix = vendor.name().toLowerCase() + "."; //$NON-NLS-1$
		final String user = store.getString(prefix + PROP_USER);
		final String password = store.getString(prefix + PROP_PASSWORD);
		final String instance = store.getString(prefix + PROP_INSTANCE);
		final String database = store.getString(prefix + PROP_DATABASE);
		final String port = store.getString(prefix + PROP_PORT);
		final String server = store.getString(prefix + PROP_SERVER);

		IConnection conn = CorePlugin.getTypedObjectFactory().create(IConnection.class);
		conn.setLogin(user);
		conn.setPassword(password);
		conn.setInstance(instance);
		conn.setDatabase(database);
		conn.setServerIP(server);
		conn.setServerPort(port);

		return conn;
	}

	@Override
	public String getName() {
		return "Synchronization"; //$NON-NLS-1$
	}
}
