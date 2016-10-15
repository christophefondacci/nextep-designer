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
package com.nextep.designer.vcs.ui.dialogs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.application.DisplayAccess;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.repository.RepositoryMessages;
import com.nextep.designer.repository.RepositoryPlugin;
import com.nextep.designer.repository.services.IRepositoryUpdaterService;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.installer.model.IInstallerMonitor;

public class RepositoryInstallerMonitorPage extends WizardDisplayConnector implements
		IInstallerMonitor {

	private Label mainLbl;
	private ProgressBar mainProgress;
	private Label subLbl;
	// private ProgressBar subProgress;
	private StyledText text;
	// private Button closeButton;
	private Composite shell;
	private int logCount = 0;
	private boolean hasStarted = false;
	private StringBuffer logBuffer;
	private boolean isDone = false;
	private IDatabaseConnector dbConnector;
	private static final int SCROLL_INTERVAL = 30;
	private static final Log log = LogFactory.getLog(RepositoryInstallerMonitorPage.class);
	// Workaround the dreadful UI deadlock
	private ThreadLocal<Boolean> isDisplayRegistered = new ThreadLocal<Boolean>();

	public RepositoryInstallerMonitorPage(IDatabaseConnector dbConnector) {
		super("repositoryInstallWizard", RepositoryMessages.getString("repositoryInstallerTitle"), //$NON-NLS-1$ //$NON-NLS-2$
				ImageDescriptor.createFromImage(VCSImages.WIZ_INSTALL));
		setMessage(RepositoryMessages.getString("repositoryInstallerDesc")); //$NON-NLS-1$
		this.dbConnector = dbConnector;
	}

	public Control createSWTControl(Composite parent) {
		logBuffer = new StringBuffer();
		this.shell = new Composite(parent, SWT.NONE);
		shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// setSize(150,200);
		shell.setLayout(new GridLayout(1, false));
		mainLbl = new Label(shell, SWT.NONE);
		GridData lblData = new GridData(SWT.FILL, SWT.FILL, true, false);
		lblData.widthHint = 300;
		mainLbl.setLayoutData(lblData);
		mainProgress = new ProgressBar(shell, SWT.HORIZONTAL | SWT.SMOOTH);
		mainProgress.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		subLbl = new Label(shell, SWT.NONE);
		subLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// subProgress = new ProgressBar(this,SWT.HORIZONTAL | SWT.SMOOTH);
		// subProgress.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		text = new StyledText(shell, SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 200;
		gd.widthHint = 350;
		text.setLayoutData(gd);
		text.setFont(FontFactory.FONT_SCRIPT);
		setPageComplete(false);
		return shell;
	}

	@Override
	public void log(final String s) {
		handleDisplayThreading();
		logBuffer.append(s);
		if (s != null) {
			log.info(s.trim());
		}
		if (++logCount % SCROLL_INTERVAL == 0 || isDone) {
			logBuffer();
		}
	}

	private void logBuffer() {
		handleDisplayThreading();
		// Ensuring UI thread
		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				text.append(logBuffer.toString());
				text.setSelectionRange(text.getText().length(), 1);
				text.showSelection();
			}
		});
		logBuffer = new StringBuffer();
	}

	@Override
	public void start(final String message, final int work) {
		handleDisplayThreading();
		log.info(message);

		// Ensuring UI thread
		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				setPageComplete(false);
				subLbl.setText(message);
			}
		});
	}

	@Override
	public void work(final String message) {
		handleDisplayThreading();
		// Ensuring UI thread
		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				setPageComplete(false);
				subLbl.setText(message);
			}
		});
	}

	public void mainStart(final String message, final int work) {
		handleDisplayThreading();
		hasStarted = true;
		// Ensuring UI thread
		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				setPageComplete(false);
				mainLbl.setText(message);
				mainProgress.setMaximum(work);
				mainProgress.setSelection(0);
			}
		});
	}

	public void mainWork(final String message) {
		handleDisplayThreading();
		// Ensuring UI thread
		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				setPageComplete(false);
				mainLbl.setText(message);
				mainProgress.setSelection(mainProgress.getSelection() + 1);
			}
		});
	}

	public void done() {
		handleDisplayThreading();
		// Ensuring UI thread
		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				setPageComplete(true);
				logBuffer();
				// Forcing display wake up (bug at least on mac cocoa)
				Display.getCurrent().wake();
			}
		});
	}

	@Override
	public Control getSWTConnector() {
		return shell;
	}

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (!isPageComplete() && !hasStarted) {
			final IConnection conn = CorePlugin.getRepositoryService().getRepositoryConnection();
			RepositoryPlugin.getService(IRepositoryUpdaterService.class).upgrade(this, conn);
		}
	}

	@Override
	protected void checkModel() {
		// Don't do anything
	}

	private void handleDisplayThreading() {
		if (isDisplayRegistered.get() == null) {
			try {
				DisplayAccess.accessDisplayDuringStartup();
			} catch (RuntimeException e) {

			} finally {
				isDisplayRegistered.set(true);
			}
		}
	}
}
