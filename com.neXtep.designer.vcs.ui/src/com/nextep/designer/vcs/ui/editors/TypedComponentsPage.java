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
import java.util.Collections;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerListener;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.forms.FormComponentContainer;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;
import com.nextep.designer.ui.services.IUIService;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * A typed component page is a form UI page representing the editor of a given type. The editor is
 * able to edit one and only one element of this type, as opposed to the {@link MasterDetailsPage}
 * which can alternatively edit a collection of element of a type. <br>
 * Components of the editor page are fetched from contributions of {@link IUIComponent} associated
 * with the type, using the {@link IUIService#getEditorComponentsFor(IElementType, DBVendor)} method
 * call.
 * 
 * @author Christophe Fondacci
 */
public class TypedComponentsPage extends FormPage implements IModelOriented<ITypedObject>,
		IEventListener, IMarkerListener {

	private IElementType type;
	private ITypedObject parentModel;
	private SashForm sashForm;
	private Composite leftPane, rightPane;
	private ScrolledPageBook scrolledLeftPane, scrolledRightPane;
	private List<IUIComponent> components = Collections.emptyList();

	/**
	 * Creates a new typed component page.
	 * 
	 * @param id page unique identifier
	 * @param type the {@link IElementType} of the element that is edited by this page
	 * @param parentModel the {@link ITypedObject} parent model object
	 * @param editor the parent {@link FormEditor} for which this page is created
	 */
	public TypedComponentsPage(String id, IElementType type, ITypedObject parentModel,
			FormEditor editor) {
		super(editor, id, type.getCategoryTitle());
		this.type = type;
		this.parentModel = parentModel;
		if (parentModel instanceof IObservable) {
			Designer.getListenerService().registerListener(this, (IObservable) parentModel, this);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
		final ScrolledForm form = managedForm.getForm();
		final FormToolkit toolkit = managedForm.getToolkit();
		// Header decoration
		toolkit.decorateFormHeading(form.getForm());
		// Form title & image
		form.setText(MessageFormat.format(
				UIMessages.getString("editor.page.typedComponent.title"), type.getName())); //$NON-NLS-1$
		form.setImage(ImageFactory.getImage(type.getIcon()));
		// Form layout
		final GridLayout gridLayout = new GridLayout(1, false);
		form.setLayout(gridLayout);
		// Building components
		final DBVendor currentVendor = DBVendor.valueOf(Designer.getInstance().getContext());
		components = uiService.getEditorComponentsFor(type, currentVendor);
		final IUIComponentContainer container = new FormComponentContainer(managedForm);

		int i = 0;
		for (IUIComponent c : components) {
			c.setUIComponentContainer(container);
			if (c instanceof IModelOriented<?>) {
				((IModelOriented<ITypedObject>) c).setModel(parentModel);
			}
			Composite parentPane = null;
			if (i == 0) {
				parentPane = form.getBody();
			} else {
				// Odd goes left and even goes right
				parentPane = i % 2 == 1 ? leftPane : rightPane;
			}
			final Control control = c.create(parentPane);
			control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			// First component initiates the sash form after it
			if (i++ == 0) {
				sashForm = new SashForm(form.getBody(), SWT.HORIZONTAL);
				toolkit.adapt(sashForm, false, false);
				sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				scrolledLeftPane = toolkit.createPageBook(sashForm, SWT.V_SCROLL | SWT.H_SCROLL);
				leftPane = toolkit.createComposite(scrolledLeftPane);
				leftPane.setLayout(new GridLayout());

				scrolledRightPane = toolkit.createPageBook(sashForm, SWT.V_SCROLL | SWT.H_SCROLL);
				rightPane = toolkit.createComposite(scrolledRightPane);
				rightPane.setLayout(new GridLayout());
			}
		}
		// Adjusting the scrolled composites
		if (scrolledLeftPane != null) {
			scrolledLeftPane.setContent(leftPane);
		}
		if (scrolledRightPane != null) {
			scrolledRightPane.setContent(rightPane);
		}

		uiService.createVersionControlToolbarActions(form.getToolBarManager(), parentModel, this);
		form.updateToolBar();
		CorePlugin.getService(ICommonUIService.class).updateFormMessages(managedForm, parentModel,
				this);
		CorePlugin.getService(IMarkerService.class).addMarkerListener(this);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {

	}

	@Override
	public void setModel(ITypedObject model) {
		Designer.getListenerService().unregisterListeners(this);
		this.parentModel = model;
		if (model instanceof IObservable) {
			Designer.getListenerService().registerListener(this, (IObservable) model, this);
		}
	}

	@Override
	public ITypedObject getModel() {
		return parentModel;
	}

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
		for (IUIComponent component : components) {
			component.dispose();
		}
		CorePlugin.getService(IMarkerService.class).removeMarkerListener(this);
		super.dispose();
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
}
