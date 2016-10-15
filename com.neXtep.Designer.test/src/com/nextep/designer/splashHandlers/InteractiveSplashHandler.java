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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.AbstractSplashHandler;
import org.osgi.framework.BundleException;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.vcs.gui.dialog.UserLoginGUI;
import com.nextep.designer.Activator;
import com.nextep.designer.vcs.ui.dialogs.ViewSelectorDialog;

/**
 * @since 3.3
 */
public class InteractiveSplashHandler extends AbstractSplashHandler {

	private static final Log log = LogFactory.getLog(InteractiveSplashHandler.class);
	ViewSelectorDialog viewSelector = null;
	IProgressMonitor monitor;
	private Shell sShell = null;
	// private Composite splashContainer;
	private static final int F_COLUMN_COUNT = 1;

	/**
     *
     */
	public InteractiveSplashHandler() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.splash.AbstractSplashHandler#init(org.eclipse.swt.widgets.Shell)
	 */
	public void init(final Shell splash) {
		// Store the shell
		super.init(splash);
		splash.setSize(450, 350);
		// FillLayout layout = new FillLayout();
		// getSplash().setLayout(layout)�;
		// Force shell to inherit the splash background
		getSplash().setBackgroundMode(SWT.INHERIT_DEFAULT);
		createSplashContainer();
		createUICompositeBlank();
		// splash.redraw();
		monitor = new SplashProgressMonitor(getSplash());
		Designer.setProgressMonitor(monitor);
		// Force the splash screen to layout
		splash.layout(true);
		splash.setLayoutDeferred(true);
		viewSelector = new ViewSelectorDialog();

		// User login
		final UserLoginGUI userSelector = new UserLoginGUI();
		userSelector.initializeGUI(getSplash());
		// splash.setSize(449,349);
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
				return "Authentication";
			}
		});

		viewSelector.initializeGUI(getSplash());
		sShell = viewSelector.getShell();
		// Keep the splash screen visible and prevent the RCP application from
		// loading until the close button is clicked.
		doEventLoop(new ICommand() {

			@Override
			public Object execute(Object... parameters) {
				return viewSelector.isOK();
			}

			@Override
			public String getName() {
				return "View selection";
			}

		});
	}

	/**
     *
     */
	private void doEventLoop(ICommand validator) {
		// Shell splash = getSplash();
		sShell.pack();
		sShell.open();
		try {
			while (!(Boolean) validator.execute()) {
				if (sShell.getDisplay().readAndDispatch() == false) {
					sShell.getDisplay().sleep();
				}
			}
		} catch (Exception e) {
			getSplash().getDisplay().dispose();
			try {
				Activator.getDefault().getBundle().stop();
			} catch (BundleException be) {
				log.error(
						"Error while stopping bundle, program termination/cleanup may have failed",
						be);
			}
			System.exit(0);
		}
		sShell.setVisible(false);
		getSplash().setFocus();
		// sShell.sdispose();
	}

	// /**
	// *
	// */
	// private void createUIButtonCancel() {
	// // Create the button
	// fButtonCancel = new Button(fCompositeLogin, SWT.PUSH);
	// fButtonCancel.setText("Cancel");
	// // Configure layout data
	// GridData data = new GridData(SWT.NONE, SWT.NONE, false, false);
	// data.widthHint = F_BUTTON_WIDTH_HINT;
	// data.verticalIndent = 10;
	// fButtonCancel.setLayoutData(data);
	// }
	//
	// /**
	// *
	// */
	// private void createUIButtonOK() {
	// // Create the button
	// fButtonOK = new Button(fCompositeLogin, SWT.PUSH);
	// fButtonOK.setText("OK");
	// // Configure layout data
	// GridData data = new GridData(SWT.NONE, SWT.NONE, false, false);
	// data.widthHint = F_BUTTON_WIDTH_HINT;
	// data.verticalIndent = 10;
	// fButtonOK.setLayoutData(data);
	// }
	//
	// /**
	// *
	// */
	// private void createUILabelBlank() {
	// Label label = new Label(fCompositeLogin, SWT.NONE);
	// label.setVisible(false);
	// }
	//
	// /**
	// *
	// */
	// private void createUITextPassword() {
	// // Create the text widget
	// int style = SWT.PASSWORD | SWT.BORDER;
	// fTextPassword = new Text(fCompositeLogin, style);
	// // Configure layout data
	// GridData data = new GridData(SWT.NONE, SWT.NONE, false, false);
	// data.widthHint = F_TEXT_WIDTH_HINT;
	// data.horizontalSpan = 2;
	// fTextPassword.setLayoutData(data);
	// }
	//
	// /**
	// *
	// */
	// private void createUILabelPassword() {
	// // Create the label
	// Label label = new Label(fCompositeLogin, SWT.NONE);
	// label.setText("&Password:");
	// // Configure layout data
	// GridData data = new GridData();
	// data.horizontalIndent = F_LABEL_HORIZONTAL_INDENT;
	// label.setLayoutData(data);
	// }
	//
	// /**
	// *
	// */
	// private void createUITextUserName() {
	// // Create the text widget
	// fTextUsername = new Text(fCompositeLogin, SWT.BORDER);
	// // Configure layout data
	// GridData data = new GridData(SWT.NONE, SWT.NONE, false, false);
	// data.widthHint = F_TEXT_WIDTH_HINT;
	// data.horizontalSpan = 2;
	// fTextUsername.setLayoutData(data);
	// }
	//
	// /**
	// *
	// */
	// private void createUILabelUserName() {
	// // Create the label
	// Label label = new Label(fCompositeLogin, SWT.NONE);
	// label.setText("&User Name:");
	// // Configure layout data
	// GridData data = new GridData();
	// data.horizontalIndent = F_LABEL_HORIZONTAL_INDENT;
	// label.setLayoutData(data);
	// }
	//
	/**
	 *
	 */
	private void createUICompositeBlank() {
		CLabel spanner = new CLabel(getSplash(), SWT.NONE);
		// spanner.setBackground(FontFactory.WHITE);
		// spanner.setBackgroundMode(SWT.INHERIT_FORCE);
		GridData data = new GridData(SWT.NONE, SWT.FILL, true, true);
		data.horizontalSpan = F_COLUMN_COUNT;
		spanner.setSize(1, 10);
		spanner.setLayoutData(data);
	}

	/**
	 *
	 */
	private void createSplashContainer() {
		// Create the composite
		// splashContainer = new Composite(getSplash(), SWT.BORDER);
		GridLayout layout = new GridLayout(F_COLUMN_COUNT, false);
		// splashContainer.setLayout(layout);
		getSplash().setLayout(layout);
	}

	// /**
	// *
	// */
	// private void configureUISplash() {
	// // Configure layout
	// FillLayout layout = new FillLayout();
	// getSplash().setLayout(layout);
	// // Force shell to inherit the splash background
	// getSplash().setBackgroundMode(SWT.INHERIT_DEFAULT);
	// }

	/**
	 * @see org.eclipse.ui.splash.AbstractSplashHandler#getBundleProgressMonitor()
	 */
	@Override
	public IProgressMonitor getBundleProgressMonitor() {
		return monitor;
	}

	/**
	 * @see org.eclipse.ui.splash.AbstractSplashHandler#dispose()
	 */
	@Override
	public void dispose() {
		Designer.setProgressMonitor(null);
		super.dispose();
	}

}
