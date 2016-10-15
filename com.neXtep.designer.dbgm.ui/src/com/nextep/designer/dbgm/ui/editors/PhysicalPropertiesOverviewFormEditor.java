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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * @author Christophe Fondacci
 */
public class PhysicalPropertiesOverviewFormEditor extends AbstractFormEditor<IPhysicalObject> {

	private Text tablespaceText;
	private Button physicalPropertiesButton;

	public PhysicalPropertiesOverviewFormEditor() {
		super(
				DBGMUIMessages.getString("editor.physProps.sectionTitle"), DBGMUIMessages.getString("editor.physProps.sectionDesc"), false); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {
		// Creating the physical properties button
		physicalPropertiesButton = toolkit.createButton(editor,
				DBGMUIMessages.getString("editor.physProps.physicalCheck"), //$NON-NLS-1$
				SWT.CHECK);
		physicalPropertiesButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));
		physicalPropertiesButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (physicalPropertiesButton.getSelection()) {
					final IPhysicalObject t = getModel();
					if (t.getPhysicalProperties() == null) {
						ITypedObjectUIController controller = UIControllerFactory.getController(t
								.getPhysicalPropertiesType());
						controller.emptyInstance(t.getName(), t);
						refresh();
					}
				} else {
					final IPhysicalObject t = getModel();
					if (t.getPhysicalProperties() != null) {
						final boolean confirmed = MessageDialog.openConfirm(Display.getDefault()
								.getActiveShell(), DBGMUIMessages
								.getString("editor.physProps.confirmRemovalTitle"), //$NON-NLS-1$
								DBGMUIMessages.getString("editor.physProps.confirmRemovalMsg")); //$NON-NLS-1$
						if (confirmed) {
							CorePlugin.getService(IWorkspaceUIService.class).remove(
									t.getPhysicalProperties());
						} else {
							physicalPropertiesButton.setSelection(true);
						}
					}
				}
			}
		});

		// Creating tablespace text
		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.physProps.tablespace")); //$NON-NLS-1$
		tablespaceText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		tablespaceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
	}

	@Override
	protected void doBindModel(DataBindingContext context) {
		// Not binding anything if the model is not ready
		if (getModel() == null) {
			return;
		}
		final IPhysicalProperties props = getModel().getPhysicalProperties();
		// Binding tablespace
		IObservableValue widgetValue = WidgetProperties.text(SWT.FocusOut).observe(tablespaceText);
		IObservableValue modelValue = PojoProperties.value(IPhysicalProperties.class,
				"tablespaceName").observe(props); //$NON-NLS-1$
		context.bindValue(widgetValue, modelValue);
	}

	@Override
	protected void doRefresh() {
		physicalPropertiesButton.setSelection(getModel().getPhysicalProperties() != null);
		final boolean enabled = physicalPropertiesButton.getSelection();
		if (tablespaceText.getEditable()) {
			tablespaceText.setEditable(enabled);
		}
	}

	@Override
	public void setModel(IPhysicalObject model) {
		super.setModel(model);
		if (model != null) {
			final IPhysicalProperties props = model.getPhysicalProperties();
			bindController(props);
			Designer.getListenerService().registerListener(this, props, new IEventListener() {

				@Override
				public void handleEvent(ChangeEvent event, IObservable source, Object data) {
					refresh();
				}
			});
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (source instanceof IPhysicalObject) {
			switch (event) {
			case GENERIC_CHILD_ADDED:
			case GENERIC_CHILD_REMOVED:
				// If we add or remove the physical properties we re-bind everything
				if (data instanceof IPhysicalProperties) {
					setModel((IPhysicalObject) source);
					return;
				}
			}
		}
		// Falling back here
		super.handleEvent(event, source, data);
	}
}
