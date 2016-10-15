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
/**
 *
 */
package com.nextep.datadesigner.gui.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.GUIWrapper;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.IInvokable;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * Provides an invokable interface for all controllers that could be invoked by the application.<br>
 * This abstract class provides the <code>invokeGUI</code> conveniency method that implements the
 * neverending event loop.<br>
 * This class must be extended by any sub dialog boxes of the application as it may evolve in a near
 * future to implement common behaviours for dialogs.
 * 
 * @author Christophe Fondacci
 * @deprecated this lease-coupled controller reveals architectural problems and should NEVER be used
 */
@Deprecated
public abstract class InvokableController implements IInvokable {

	private Shell rootShell = null;
	private Image icon;
	private static final Log log = LogFactory.getLog(InvokableController.class);

	/**
	 * Invokes a GUI and implement the neverending event loop
	 * 
	 * @param gui the <code>IDesignerGUI</code> object to display
	 */
	protected void invokeGUI(final IDesignerGUI gui) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				invokeGuiInUIThread(gui);
			}
		});
	}

	private void invokeGuiInUIThread(IDesignerGUI gui) {
		Object snapshot = null;
		if (Designer.isDebugging()) {
			snapshot = Observable.getSnapshot();
		}
		if (gui.getShell() == null) {
			if (CoreUiPlugin.getDefault().getWorkbench() != null
					&& CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null) {
				if (rootShell == null) {
					this.rootShell = CoreUiPlugin.getDefault().getWorkbench()
							.getActiveWorkbenchWindow().getShell();
				}
			} else {
				this.rootShell = Display.getCurrent().getActiveShell();
			}
			gui.initializeGUI(rootShell);
		}
		Shell shell = gui.getShell();
		if (icon != null) {
			shell.setImage(icon);
		} else {
			shell.setImage(ImageFactory.ICON_DESIGNER_TINY);
		}
		// Positioning shell from parent and child bounds
		shell.layout();
		Rectangle pRect = rootShell.getBounds();
		Rectangle cRect = shell.getBounds();
		Point location = new Point(Math.max(0, pRect.x + pRect.width / 2 - cRect.width / 2),
				Math.max(0, pRect.y + pRect.height / 2 - cRect.height / 2));
		shell.setLocation(location);
		Display display = Display.getDefault();
		shell.open();
		// Standard SWT dispose loop
		try {
			while (!shell.isDisposed()) {
				try {
					if (!display.readAndDispatch())
						display.sleep();
				} catch (ErrorException e) {
					// An error might happen, but we should continue anyway
					// ExceptionHandler.handle(e);
					log.error("Error in the UI event loop: " + e.getMessage(), e);
				}
			}
		} catch (CancelException e) {
			// if(!shell.isDisposed()) {
			// shell.dispose();
			// }
			// log.error(e);
			throw e;
		} finally {
			if (!shell.isDisposed()) {
				shell.dispose();
			}
			if (Designer.isDebugging()) {
				Observable.dumpSnapshotDelta(snapshot);
			}
		}
		if (gui instanceof GUIWrapper && ((GUIWrapper) gui).isCancelled()) {
			throw new CancelException("Operation cancelled");
		}
	}

	protected void setRootShell(Shell shell) {
		this.rootShell = shell;
	}

	/**
	 * Opens a wizard on the specified wizard pages
	 * 
	 * @param title wizard title
	 * @param pages wizard pages
	 * @return the wizard
	 */
	protected int openWizard(final String title, IWizardPage... pages) {
		Wizard w = new Wizard() {

			@Override
			public boolean performFinish() {
				return true;
			}
		};
		w.setWindowTitle(title);
		for (IWizardPage p : pages) {
			w.addPage(p);
		}
		return openWizardDialog(w);
	}

	/**
	 * Opens a wizard dialog on the specified wizard
	 * 
	 * @param wizard to open
	 */
	protected int openWizardDialog(IWizard wizard) {
		if (CoreUiPlugin.getDefault().getWorkbench() != null
				&& CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null) {
			if (rootShell == null) {
				this.rootShell = CoreUiPlugin.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getShell();
			}
		} else {
			this.rootShell = Display.getCurrent().getActiveShell();
		}
		WizardDialog d = new WizardDialog(rootShell, wizard);
		d.setTitle(wizard.getWindowTitle());
		d.setBlockOnOpen(true);
		d.open();
		return d.getReturnCode();
	}

	/**
	 * Defines the icon of the GUI dialog
	 * 
	 * @param icon
	 */
	protected void setIcon(Image icon) {
		this.icon = icon;
	}
}
