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
package com.nextep.designer.ui.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.ui.model.ISizedComponent;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;
import com.nextep.designer.ui.model.IValidatableUI;

/**
 * This class can wrap a neXtep UI component into a {@link TitleAreaDialog}.
 * This kind of wrapper is provided to abstract away dependencies to specific
 * eclipse/jface UI components, allowing neXtep UI to be reusable in different
 * contexts such as editors, wizards, dialogs, views.
 * 
 * @author Christophe Fondacci
 */
public class TitleAreaDialogWrapper extends TitleAreaDialog implements IUIComponentContainer {

	private static final Log LOGGER = LogFactory.getLog(TitleAreaDialogWrapper.class);
	private final IUIComponent component;

	public TitleAreaDialogWrapper(Shell parent, IUIComponent component, int style) {
		super(parent);
		setShellStyle(style);
		this.component = component;
		component.setUIComponentContainer(this);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		if (component instanceof ITitleAreaComponent) {
			final ITitleAreaComponent titled = (ITitleAreaComponent) component;
			if (titled.getAreaTitle() != null) {
				setTitle(titled.getAreaTitle());
			}
			if (titled.getDescription() != null) {
				setMessage(titled.getDescription());
			}
			if (titled.getImage() != null) {
				setTitleImage(titled.getImage());
			}
		}
		component.create(c);
		return c;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (component instanceof ITitleAreaComponent) {
			final ITitleAreaComponent titled = (ITitleAreaComponent) component;
			if (titled.getAreaTitle() != null) {
				newShell.setText(titled.getAreaTitle());
			}
		}
	}

	@Override
	protected void okPressed() {
		if (component instanceof IValidatableUI) {
			((IValidatableUI) component).validate();
		}
		super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		if (component instanceof IValidatableUI) {
			((IValidatableUI) component).cancel();
		}
		super.cancelPressed();
	}

	@Override
	public IUIComponent getUIComponent() {
		return component;
	}

	@Override
	public void run(boolean block, boolean cancelable, final IRunnableWithProgress runnable) {
		Job j = new Job("Processing") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					runnable.run(monitor);
				} catch (InvocationTargetException e) {
					throw new ErrorException(e);
				} catch (InterruptedException e) {
					throw new ErrorException(e);
				}
				return Status.OK_STATUS;
			}
		};
		j.schedule();
		if (block) {
			//
			IStatus status = null;
			final Display display = Display.getDefault();
			while (status == null) {
				while (!display.isDisposed() && display.readAndDispatch()) {
				}
				status = j.getResult();
			}
		}
	}

	@Override
	protected Point getInitialSize() {
		if (component instanceof ISizedComponent) {
			final ISizedComponent sized = (ISizedComponent) component;
			return new Point(sized.getWidth(), sized.getHeight());
		}
		return super.getInitialSize();
	}
}
