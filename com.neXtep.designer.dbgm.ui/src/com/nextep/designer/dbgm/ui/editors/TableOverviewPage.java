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
package com.nextep.designer.dbgm.ui.editors;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
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
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.jface.ConstraintsContentProvider;
import com.nextep.designer.dbgm.ui.jface.IndexContentProvider;
import com.nextep.designer.dbgm.ui.services.IDatabaseModelUIService;
import com.nextep.designer.ui.forms.FormComponentContainer;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;
import com.nextep.designer.ui.services.IUIService;
import com.nextep.designer.vcs.services.IWorkspaceService;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class TableOverviewPage extends FormPage implements IEventListener,
		IModelOriented<IBasicTable>, IMarkerListener {

	private static final String PAGE_ID = IUIService.PAGE_ID_PREFIX + "TABLE"; //$NON-NLS-1$
	private IBasicTable table;
	private List<IUIComponent> tableComponents = Collections.emptyList();
	private IManagedForm form;
	private String tableCachedName;

	public TableOverviewPage() {
		super(PAGE_ID, DBGMUIMessages.getString("overview.table.pageTitle")); //$NON-NLS-1$
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case MODEL_CHANGED:
			if (table != null && !tableCachedName.equals(table.getName())) {
				updateFormTitle();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setModel(IBasicTable model) {
		Designer.getListenerService().unregisterListeners(this);
		this.table = model;
		tableCachedName = table.getName();
		if (model != null) {
			Designer.getListenerService().registerListener(this, table, this);
		}
		// Propagating the model change to sub-components
		for (IUIComponent component : tableComponents) {
			if (component instanceof IModelOriented<?>) {
				((IModelOriented) component).setModel(model);
			}
		}
		updateFormTitle();
	}

	private void updateFormTitle() {
		// Adjusting form's title
		if (form != null) {
			final IBasicTable model = getModel();
			form.getForm().setText(
					MessageFormat.format(DBGMUIMessages.getString("overview.table.title"), //$NON-NLS-1$
							model == null ? "" : model.getType().getName(), //$NON-NLS-1$
							model == null ? "" : model.getName())); //$NON-NLS-1$
		}
	}

	@Override
	public IBasicTable getModel() {
		return table;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		this.form = managedForm;
		final ScrolledForm form = managedForm.getForm();
		final FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());
		updateFormTitle();
		form.setImage(DBGMImages.ICON_TABLE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);

		// Dynamically injecting table components
		Composite tableComponentsComposite = toolkit.createComposite(form.getBody(), SWT.BORDER);
		GridData d = new GridData(SWT.FILL, SWT.FILL, true, false);
		d.widthHint = 250;
		tableComponentsComposite.setLayoutData(d);
		GridLayout gl = new GridLayout();
		gl.horizontalSpacing = gl.marginBottom = gl.marginHeight = gl.marginLeft = gl.marginRight = gl.marginTop = gl.marginWidth = gl.verticalSpacing = 0;
		tableComponentsComposite.setLayout(gl);

		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
		final IWorkspaceService workspaceService = CorePlugin.getService(IWorkspaceService.class);
		final DBVendor currentVendor = workspaceService.getCurrentWorkspace().getDBVendor();
		final IElementType tableType = IElementType.getInstance(IBasicTable.TYPE_ID);
		// Fetching table components
		tableComponents = uiService.getEditorComponentsFor(tableType, currentVendor);
		// Fetching non-default components
		if (table.getType() != tableType) {
			tableComponents
					.addAll(uiService.getEditorComponentsFor(table.getType(), currentVendor));
		}
		final IUIComponentContainer container = new FormComponentContainer(managedForm);
		for (IUIComponent component : tableComponents) {
			component.setUIComponentContainer(container);
			component.create(tableComponentsComposite);
			if (component instanceof IModelOriented<?>) {
				((IModelOriented<IBasicTable>) component).setModel(getModel());
			}
		}

		// Adding the column section
		final SectionPart part = new SectionPart(form.getBody(), toolkit, Section.TITLE_BAR
				| Section.EXPANDED);
		final Section columnSection = part.getSection();
		TableWrapLayout propsLayout = new TableWrapLayout();
		columnSection.setLayout(propsLayout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 3);
		gd.widthHint = 250;
		gd.heightHint = 100;
		columnSection.setLayoutData(gd);
		columnSection.setText(DBGMUIMessages.getString("overview.table.columnsTitle")); //$NON-NLS-1$
		//		columnSection.setDescription(DBGMUIMessages.getString("overview.table.columnsDesc")); //$NON-NLS-1$

		// Since it could be confusing for users, we add a link to the columns tab here.
		Link columnLink = new Link(columnSection, SWT.WRAP);
		columnSection.setDescriptionControl(columnLink);
		columnLink.setText(MessageFormat.format(
				DBGMUIMessages.getString("overview.table.columnsDesc"), //$NON-NLS-1$
				IElementType.getInstance(IBasicColumn.TYPE_ID).getCategoryTitle()));
		columnLink.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		columnLink.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final ISelection s = getSite().getSelectionProvider().getSelection();
				IBasicColumn selectedCol = null;
				// Extracting any currently selected column from the site selection provider
				if (s instanceof IStructuredSelection && !s.isEmpty()) {
					Object o = ((IStructuredSelection) s).getFirstElement();
					if (o instanceof IBasicColumn) {
						selectedCol = (IBasicColumn) o;
					}
				}
				getEditor().setActivePage(
						IUIService.PAGE_ID_PREFIX
								+ IElementType.getInstance(IBasicColumn.TYPE_ID).getId(),
						selectedCol);
			}
		});
		// Building the columns editor
		final Composite columnsTable = CorePlugin.getService(IDatabaseModelUIService.class)
				.createColumnEditor(this, columnSection, getModel());
		columnsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		toolkit.adapt(columnsTable);
		part.setFormInput(getModel());
		managedForm.addPart(part);
		columnSection.setClient(columnsTable);

		// Adding the constraint section
		final IFormPart constraintsPart = new ContentProvidedListFormPart(
				DBGMUIMessages.getString("overview.table.constraintsTitle"), //$NON-NLS-1$
				DBGMUIMessages.getString("overview.table.constraintsDesc"), form.getBody(), toolkit, //$NON-NLS-1$
				new ConstraintsContentProvider(), this);
		constraintsPart.setFormInput(getModel());
		managedForm.addPart(constraintsPart);

		// Adding the index section
		final IFormPart indexPart = new ContentProvidedListFormPart(
				DBGMUIMessages.getString("overview.table.indexTitle"), //$NON-NLS-1$
				DBGMUIMessages.getString("overview.table.indexDesc"), form.getBody(), toolkit, new IndexContentProvider(), this); //$NON-NLS-1$
		indexPart.setFormInput(getModel());
		managedForm.addPart(indexPart);

		uiService.createVersionControlToolbarActions(form.getToolBarManager(), getModel(), this);
		form.updateToolBar();

		uiService.updateFormMessages(managedForm, getModel(), this);
		CorePlugin.getService(IMarkerService.class).addMarkerListener(this);
	}

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
		for (IUIComponent component : tableComponents) {
			component.dispose();
		}
		CorePlugin.getService(IMarkerService.class).removeMarkerListener(this);
		super.dispose();
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		// Emptying this method because of Eclipse bug resetting title information
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
