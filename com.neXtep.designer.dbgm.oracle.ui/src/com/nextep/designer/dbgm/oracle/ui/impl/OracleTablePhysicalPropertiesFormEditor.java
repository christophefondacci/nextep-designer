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
package com.nextep.designer.dbgm.oracle.ui.impl;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleTable;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.PhysicalOrganisation;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.dbgm.oracle.ui.factories.OracleConverterFactory;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * @author Christophe Fondacci
 */
public class OracleTablePhysicalPropertiesFormEditor extends AbstractFormEditor<IPhysicalObject> {

	private Text tablespaceText;
	private CCombo organizationCombo;
	private Button physicalPropertiesButton, loggingButton, compressedButton;
	private SelectionListener physicalPropertiesSelectionListener = new PhysicalPropertiesSelectionListener();

	/**
	 * The selection listener that enables physical properties when check box is clicked. This
	 * listener is in charge of instantiating the {@link IPhysicalProperties} bean when needed so
	 * that information could be set for the {@link IPhysicalObject}.
	 */
	private class PhysicalPropertiesSelectionListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (physicalPropertiesButton.getSelection()) {
				final IPhysicalObject t = getModel();
				if (t != null && t.getPhysicalProperties() == null) {
					ITypedObjectUIController controller = UIControllerFactory.getController(t
							.getPhysicalPropertiesType());
					controller.emptyInstance(t.getName(), t);

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
	}

	public OracleTablePhysicalPropertiesFormEditor() {
		super(
				DBOMUIMessages.getString("editor.tablePhysProps.title"), DBOMUIMessages.getString("editor.tablePhysProps.desc"), false); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {
		editor.setLayout(new GridLayout(4, false));
		// Creating the physical properties button
		physicalPropertiesButton = toolkit.createButton(editor,
				DBOMUIMessages.getString("editor.tablePhysProps.physPropsEnabledButton"), //$NON-NLS-1$
				SWT.CHECK);
		physicalPropertiesButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		// Handling selection listener registration
		physicalPropertiesButton.addSelectionListener(physicalPropertiesSelectionListener);

		// Creating tablespace text
		toolkit.createLabel(editor, DBOMUIMessages.getString("editor.tablePhysProps.tablespace")); //$NON-NLS-1$
		tablespaceText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		tablespaceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		// Organization combo
		final IPhysicalProperties props = getModel() == null ? null : getModel()
				.getPhysicalProperties();
		if (props instanceof ITablePhysicalProperties
				|| (props == null && getModel() instanceof IOracleTable)) {
			toolkit.createLabel(editor,
					DBOMUIMessages.getString("editor.tablePhysProps.organization")); //$NON-NLS-1$
			organizationCombo = new CCombo(editor, SWT.BORDER | SWT.READ_ONLY);
			toolkit.adapt(organizationCombo);
			organizationCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
			for (PhysicalOrganisation org : PhysicalOrganisation.values()) {
				organizationCombo.add(org.getLabel());
			}
		}
		toolkit.createLabel(editor, "Options : ");
		// Creating the logging button
		loggingButton = toolkit.createButton(editor, "Logging", //$NON-NLS-1$
				SWT.CHECK);
		loggingButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		// Creating the compressed button
		compressedButton = toolkit.createButton(editor, "Compressed", //$NON-NLS-1$
				SWT.CHECK);
		compressedButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		final Label filler = toolkit.createLabel(editor, ""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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

		// Binding organization
		if (props instanceof ITablePhysicalProperties
				|| (props == null && getModel() instanceof IOracleTable)) {
			widgetValue = WidgetProperties.selection().observe(organizationCombo);
			modelValue = PojoProperties.value(IOracleTablePhysicalProperties.class,
					"physicalOrganisation").observe(props); //$NON-NLS-1$
			UpdateValueStrategy modelStrategy = new UpdateValueStrategy()
					.setConverter(OracleConverterFactory.createPhysicalOrganizationModelConverter());
			UpdateValueStrategy targetStrategy = new UpdateValueStrategy()
					.setConverter(OracleConverterFactory
							.createPhysicalOrganizationTargetConverter());
			context.bindValue(widgetValue, modelValue, modelStrategy, targetStrategy);
		}
		// Binding logging
		widgetValue = WidgetProperties.selection().observe(loggingButton);
		modelValue = PojoProperties.value(IPhysicalProperties.class, "logging").observe(props); //$NON-NLS-1$
		context.bindValue(widgetValue, modelValue);
		// Binding compressed
		widgetValue = WidgetProperties.selection().observe(compressedButton);
		modelValue = PojoProperties.value(IPhysicalProperties.class, "compressed").observe(props); //$NON-NLS-1$
		context.bindValue(widgetValue, modelValue);
	}

	@Override
	protected void doRefresh() {
		final ICoreService coreService = CorePlugin.getService(ICoreService.class);
		final ILockable<?> lockable = coreService.getLockable(getModel());
		final boolean parentEnabled = lockable == null ? true : !lockable.updatesLocked();
		physicalPropertiesButton.setSelection(getModel().getPhysicalProperties() != null);
		final boolean enabled = physicalPropertiesButton.getSelection() && parentEnabled;
		tablespaceText.setEditable(enabled);
		loggingButton.setEnabled(enabled);
		compressedButton.setEnabled(enabled);
		if (organizationCombo != null) {
			organizationCombo.setEnabled(enabled);
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
