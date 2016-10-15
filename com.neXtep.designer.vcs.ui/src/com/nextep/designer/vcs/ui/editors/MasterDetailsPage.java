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
package com.nextep.designer.vcs.ui.editors;

import java.text.MessageFormat;
import java.util.Collection;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerListener;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.model.IFormActionProvider;
import com.nextep.designer.ui.services.IUIService;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * A master details page implementation used by the UI framework to create dynamic pages for
 * type-specific edition. This class should generally not be instantiated directly, preferred method
 * is to use the {@link IUIService#createPageFor(com.nextep.datadesigner.model.IElementType)} method
 * that will instantiate and setup this page correclty.
 * 
 * @author Christophe Fondacci
 */
public class MasterDetailsPage extends FormPage implements IModelOriented<ITypedObject>,
		IEventListener, IMarkerListener {

	private MasterDetailsTypedBlock columnsEditorBlock;
	private String pageTitle, sectionTitle, sectionDesc;
	private Image icon;
	private IContentProvider contentProvider;
	private ILabelProvider labelProvider;
	private IFormActionProvider actionProvider;
	private ITypedObject input;
	private String inputCachedName;
	private String formTitle;
	private IManagedForm form;

	/**
	 * 
	 */
	public MasterDetailsPage(String pageId, String pageTitle, String sectionTitle,
			String sectionDesc, FormEditor editor, ITypedObject parentModel) {
		super(editor, pageId, pageTitle);
		this.sectionTitle = sectionTitle;
		this.sectionDesc = sectionDesc;
		setModel(parentModel);
		// Default form title initialization
		this.formTitle = pageTitle;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		this.form = managedForm;
		columnsEditorBlock = new MasterDetailsTypedBlock(sectionTitle, sectionDesc,
				contentProvider, labelProvider, actionProvider, input);
		columnsEditorBlock.setPart(this);
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());
		updateFormTitle(form);
		form.setImage(icon);
		columnsEditorBlock.createContent(managedForm);

		CorePlugin.getService(ICommonUIService.class).updateFormMessages(managedForm, input, this);
		CorePlugin.getService(IMarkerService.class).addMarkerListener(this);
	}

	private void updateFormTitle(ScrolledForm form) {
		final String pageTitle = MessageFormat
				.format(UIMessages.getString("service.ui.formPageTitle"), input == null ? "" : IFormatter.UPPER_LEADING.format(input.getType().getName()), input == null ? "" : ((INamedObject) input).getName(), IFormatter.UPPER_LEADING.format(formTitle)); //$NON-NLS-1$
		form.setText(pageTitle);
	}

	public void setIcon(Image icon) {
		this.icon = icon;
	}

	public void setContentProvider(IContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}

	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	// public void setPageTitle(String title) {
	// this.pageTitle = title;
	// }

	/**
	 * @param actionProvider the action provider to set
	 */
	public void setActionProvider(IFormActionProvider actionProvider) {
		this.actionProvider = actionProvider;
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case MODEL_CHANGED:
			// Checking whether name has changed
			if (input != null && form != null) {
				if (inputCachedName == null
						|| !inputCachedName.equals(((INamedObject) input).getName())) {
					updateFormTitle(form.getForm());
				}
			}
		}
	}

	@Override
	public void setModel(ITypedObject model) {
		Designer.getListenerService().unregisterListeners(this);
		this.input = model;
		if (model instanceof IObservable) {
			Designer.getListenerService().registerListener(this, (IObservable) model, this);
		}
		// Updating form title
		if (form != null) {
			updateFormTitle(form.getForm());
		}
		// Updating name cache so that we can detect name changes
		if (model != null) {
			inputCachedName = ((INamedObject) model).getName();
		} else {
			inputCachedName = "";
		}
	}

	@Override
	public ITypedObject getModel() {
		return input;
	}

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
		CorePlugin.getService(IMarkerService.class).removeMarkerListener(this);
		super.dispose();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IFormActionProvider.class) {
			return actionProvider;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void markersChanged(Object o, Collection<IMarker> oldMarkers,
			Collection<IMarker> newMarkers) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				updateFormMessages();
			}
		});
	}

	private void updateFormMessages() {
		final IManagedForm managedForm = getManagedForm();
		// Updating messages
		if (getModel() instanceof ITypedObject) {
			CorePlugin.getService(ICommonUIService.class).updateFormMessages(managedForm,
					(ITypedObject) getModel(), this);
		}
	}

	@Override
	public void markersReset(Collection<IMarker> allMarkers) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				updateFormMessages();
			}
		});
	}

	public void setFormTitle(String formTitle) {
		this.formTitle = formTitle;
	}

}
