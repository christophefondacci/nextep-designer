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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.ui.rcp;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.ui.DbgmUIPlugin;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITypedFormPage;
import com.nextep.designer.ui.model.impl.GlobalSelectionProvider;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * The main table multi-page editor which contains an overview page and fetches its other child
 * pages through extension contributions.
 * 
 * @author Christophe Fondacci
 */
public class TypedFormRCPEditor extends FormEditor implements IEventListener,
		IModelOriented<ITypedObject> {

	public final static String EDITOR_ID = "com.neXtep.designer.dbgm.ui.typedFormEditor"; //$NON-NLS-1$
	private static final Log LOGGER = LogFactory.getLog(TypedFormRCPEditor.class);

	private ITypedObject model;

	@Override
	protected FormToolkit createToolkit(Display display) {
		// Create a toolkit that shares colors between editors.
		return new FormToolkit(DbgmUIPlugin.getDefault().getFormColors(display));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addPages() {

		IEditorInput input = getEditorInput();
		model = ((IModelOriented<? extends ITypedObject>) input).getModel();
		try {
			addTypedPages(model.getType(), model);
		} catch (PartInitException e) {
			LOGGER.error("Unable to instantiate editor inner pages : " + e.getMessage(), e);
		}
		setPartName(((INamedObject) model).getName());
		setTitleImage(ImageFactory.getImage(model.getType().getIcon()));

		// Tracking model changes to update editor tab title
		if (model instanceof IObservable) {
			Designer.getListenerService().registerListener(this, (IObservable) model, this);
		}
	}

	private void addTypedPages(IElementType type, ITypedObject object) throws PartInitException {
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
		final List<ITypedFormPage> pages = uiService.createContributedPagesFor(type, object, this);
		for (ITypedFormPage page : pages) {
			final int pageIndex = addPage(page.getFormPage());
			if (page.getType() != null) {
				setPageImage(pageIndex, ImageFactory.getImage(page.getType().getTinyIcon()));
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		site.setSelectionProvider(new GlobalSelectionProvider());
	}

	@Override
	public void setModel(ITypedObject model) {
		this.model = model;
		if (model != null) {
			setPartName(((INamedObject) model).getName());
		} else {
			setPartName("[No name]"); //$NON-NLS-1$
		}
	}

	@Override
	public ITypedObject getModel() {
		return model;
	}

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
		super.dispose();
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case MODEL_CHANGED:
			// Checking whether name changed and updates tab name
			final String name = ((INamedObject) model).getName();
			if (!getPartName().equals(name)) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						setPartName(name);
					}
				});
			}
		}
	}
}
