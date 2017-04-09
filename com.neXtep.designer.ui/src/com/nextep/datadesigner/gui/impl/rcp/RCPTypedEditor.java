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
package com.nextep.datadesigner.gui.impl.rcp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.editors.DesignerSelectionProvider;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * A generic RCP editor which handles {@link ITypedObject} and wraps the
 * underlying internal display connector. This editor delegates to generic
 * factories / controllers the task of loading / persisting the object model.
 * 
 * @author Christophe Fondacci
 */
public class RCPTypedEditor extends EditorPart {

	public static final String EDITOR_ID = "com.neXtep.designer.ui.typedEditor";

	private static final Log log = LogFactory.getLog(RCPTypedEditor.class);
	/** Our underlying internal GUI display connector */
	private IDisplayConnector gui;
	/** Our generic selection provider */
	private DesignerSelectionProvider selProvider;
	/** Our generic typed object model handled by this editor */
	private ITypedObject model;

	/**
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setPartName(input.getName());
		setInput(input);
		if (input instanceof TypedEditorInput) {
			this.model = ((TypedEditorInput) input).getModel();
		} else {
			throw new PartInitException(new Status(IStatus.ERROR, CoreUiPlugin.PLUGIN_ID,
					"Wrong editor input type: Expecting TypedEditorInput."));
		}

	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		final GridLayout gl = new GridLayout();
		gl.marginBottom = gl.marginHeight = gl.marginLeft = gl.marginRight = gl.marginTop = gl.marginWidth = 0;
		parent.setLayout(gl);
		gui = createGUI(model);
		if (gui != null) {
			String message = (String) Designer.getInstance().runCommand("getLockReason", model);
			if (message != null) {
				CLabel msgLabel = new CLabel(parent, SWT.BORDER_SOLID);
				msgLabel.setImage(ImageFactory.ICON_LOCK);

				msgLabel.setText(message);
				msgLabel.setBackground(FontFactory.LIGHT_YELLOW);
				msgLabel.setForeground(FontFactory.CHECKIN_COLOR);
				GridData gridData = new GridData();
				gridData.horizontalAlignment = GridData.FILL;
				gridData.grabExcessHorizontalSpace = true;
				msgLabel.setLayoutData(gridData);
			}
			// Layout for main editor
			Control c = gui.create(parent);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.verticalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace = true;
			c.setLayoutData(gridData);
			gui.refreshConnector();
			setPartName(gui.getTitle());
			setTitleImage(ImageFactory.getImage(model.getType().getIcon()));
			// Initializing a provider which will always provide the same
			// selection
			selProvider = new DesignerSelectionProvider(c, model);
			// selProvider.setSelection(new
			// StructuredSelection(gui.getModel()));
			this.getSite().setSelectionProvider(selProvider);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(gui.getSWTConnector(),
					"com.neXtep.designer.ui." + (model != null ? model.getType().getId() : "null"));
		} else {
			log.debug("RCPViewWrapper: No GUI found");
		}
	}

	/**
	 * Creates the GUI corresponding to this typed object. Subclasses may
	 * override the default controller generated editor
	 * 
	 * @param model
	 *            typed object model
	 * @return the {@link IDisplayConnector} instance
	 */
	protected IDisplayConnector createGUI(ITypedObject model) {
		return UIControllerFactory.getController(model.getType()).initializeEditor(model);
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (gui != null) {
			gui.getSWTConnector().setFocus();
			final TypedEditorInput input = (TypedEditorInput) getEditorInput();
			selProvider.setSelection(new StructuredSelection(gui.getModel()));
		}
	}

}
