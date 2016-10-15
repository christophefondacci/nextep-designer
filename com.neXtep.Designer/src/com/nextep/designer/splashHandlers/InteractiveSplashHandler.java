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
package com.nextep.designer.splashHandlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.BasicSplashHandler;
import org.osgi.framework.BundleException;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.gui.dialog.UserLoginGUI;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.Activator;
import com.nextep.designer.DesignerMessages;
import com.nextep.designer.vcs.ui.dialogs.ViewSelectorDialog;

/**
 * @author Christophe Fondacci
 * @since 3.3
 */
public class InteractiveSplashHandler extends BasicSplashHandler {

	private static final Log log = LogFactory.getLog(InteractiveSplashHandler.class);
	ViewSelectorDialog viewSelector = null;
	private Shell sShell = null;
	private IProgressMonitor monitor;
	// private Composite splashContainer;
	private static final int F_COLUMN_COUNT = 1;

	/**
     *
     */
	public InteractiveSplashHandler() {
		log.debug("Splash handler created"); //$NON-NLS-1$
		setProgressRect(new Rectangle(138, 97, 183, 13));
		setMessageRect(new Rectangle(6, 268, 437, 15));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.splash.AbstractSplashHandler#init(org.eclipse.swt.widgets
	 * .Shell)
	 */
	@Override
	public void init(final Shell splash) {
		log.debug("Splash initialization..."); //$NON-NLS-1$
		// Store the shell
		super.init(splash);
		final IProgressMonitor monitor = getBundleProgressMonitor();
		log.debug("Bundle monitor is: " + monitor); //$NON-NLS-1$
		Designer.setProgressMonitor(getBundleProgressMonitor());

		// Static method call here since we cannot instantiate HibernateUtil
		// which would initialize the session factory on the current repository
		// connection which this splash may change
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				// Workaroung of bug
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=298795
				// Register the monitor in UI thread
				HibernateUtil.setMonitor(monitor);
			}
		});
		// We force the element enumeration here
		getBundleProgressMonitor().beginTask(
				DesignerMessages.getString("splashHandler.init.taskName"), 5000); //$NON-NLS-1$
		IElementType.values();
		getBundleProgressMonitor().worked(1);

		final String buildId = Activator.getDefault().getBundle().getVersion().toString();
		monitor.setTaskName("Build ID is " + buildId);
		// User login
		final UserLoginGUI userSelector = new UserLoginGUI();
		log.debug("Initializing user login dialog"); //$NON-NLS-1$
		userSelector.initializeGUI(getSplash());

		// Adding a dispose listener
		userSelector.getShell().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (!userSelector.isAuthenticated()) {
					System.exit(0);
				}
			}
		});
		sShell = userSelector.getShell();
		// Keep the splash screen visible and prevent the RCP application from
		// loading until the close button is clicked.
		doEventLoop(new ICommand() {

			@Override
			public Object execute(Object... parameters) {
				return userSelector.isAuthenticated();
			}

			@Override
			public String getName() {
				return DesignerMessages.getString("splashHandler.authentication.title"); //$NON-NLS-1$
			}
		});

		// To be able to launch the environment on the system view (client
		// update needed),
		// We may have already set a view...
		if (VersionHelper.getCurrentView() == null) {
			viewSelector = new ViewSelectorDialog();
			viewSelector.initializeGUI(getSplash());
			sShell = viewSelector.getShell();
			// Keep the splash screen visible and prevent the RCP application
			// from
			// loading until the close button is clicked.
			doEventLoop(new ICommand() {

				@Override
				public Object execute(Object... parameters) {
					return viewSelector.isOK();
				}

				@Override
				public String getName() {
					return DesignerMessages.getString("splashHandler.workspaceSelection.title"); //$NON-NLS-1$
				}

			});
		}
	}

	/**
     *
     */
	private void doEventLoop(ICommand validator) {
		sShell.pack();
		sShell.open();
		try {
			while (!(Boolean) validator.execute()) {
				try {
					if (!sShell.getDisplay().readAndDispatch()) {
						sShell.getDisplay().sleep();
					}
				} catch (CancelException e) {
					// We are fine, it's a safe exception
					log.info("Action cancelled"); //$NON-NLS-1$
					// If we're within the view selection, cancel action must be
					// performed.
					// TODO: Not very elegant to check the nullity of this
					// variable for a cancel
					// action
					if (viewSelector != null) {
						throw e;
					}
				}
			}
		} catch (Exception e) {
			getSplash().getDisplay().dispose();
			try {
				Activator.getDefault().getBundle().stop();
			} catch (BundleException be) {
				log.error(
						"Error while stopping bundle, program termination/cleanup may have failed", be); //$NON-NLS-1$
			}
			System.exit(0);
		}
		sShell.dispose();
		getSplash().setFocus();
		// sShell.sdispose();
	}

	/**
	 * @see org.eclipse.ui.splash.AbstractSplashHandler#dispose()
	 */
	@Override
	public void dispose() {
		Designer.setProgressMonitor(null);
		super.dispose();
	}

	@Override
	public IProgressMonitor getBundleProgressMonitor() {
		if (monitor == null) {
			IProgressMonitor parentMonitor = super.getBundleProgressMonitor();
			if (parentMonitor instanceof ProgressMonitorPart) {
				((ProgressMonitorPart) parentMonitor).setFont(FontFactory.FONT_BOLD);
			}
			monitor = new SplashProgressMonitor(parentMonitor);
			// monitor = SubMonitor.convert(parentMonitor, 10000);
		}

		return monitor;
	}

}
