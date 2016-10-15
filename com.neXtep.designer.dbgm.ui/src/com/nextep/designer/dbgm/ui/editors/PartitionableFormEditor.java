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
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.actions.PartitioningColumnsActionProvider;
import com.nextep.designer.dbgm.ui.factories.ConverterFactory;
import com.nextep.designer.dbgm.ui.jface.DbgmLabelProvider;
import com.nextep.designer.dbgm.ui.jface.PartitioningColumnsContentProvider;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class PartitionableFormEditor extends AbstractFormEditor<IPhysicalObject> implements
		ISelectionChangedListener {

	private CCombo partitioningCombo;
	private IUIComponent partitioningColumnsComponent;

	public PartitionableFormEditor() {
		super(
				DBGMUIMessages.getString("editor.partitionable.sectionTitle"), DBGMUIMessages.getString("editor.partitionable.sectionDesc"), false); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {
		// Partitioning combo
		toolkit.createLabel(editor,
				DBGMUIMessages.getString("editor.partitionable.partitioningLabel")); //$NON-NLS-1$
		partitioningCombo = new CCombo(editor, SWT.BORDER | SWT.READ_ONLY);
		toolkit.adapt(partitioningCombo);
		partitioningCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (PartitioningMethod partMethod : PartitioningMethod.values()) {
			partitioningCombo.add(partMethod.name());
		}

		final IPartitionable partitionable = (IPartitionable) getModel().getPhysicalProperties();
		if (getModel() instanceof IBasicTable) {
			// For table we propose partitioning columns edition
			final Label partColIntroLabel = toolkit.createLabel(editor,
					DBGMUIMessages.getString("editor.partitionable.columnsLabel")); //$NON-NLS-1$
			partColIntroLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			final IContentProvider contentProvider = new PartitioningColumnsContentProvider();
			final ILabelProvider labelProvider = new DbgmLabelProvider();
			partitioningColumnsComponent = CorePlugin.getService(ICommonUIService.class)
					.createTypedListComponent(labelProvider, contentProvider,
							new PartitioningColumnsActionProvider(), (ITypedObject) partitionable);
			partitioningColumnsComponent.setUIComponentContainer(getUIComponentContainer());
			final Control c = partitioningColumnsComponent.create(editor);
			c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doBindModel(DataBindingContext context) {
		final IPartitionable partitionable = (IPartitionable) getModel().getPhysicalProperties();
		// Binding partitioning
		IObservableValue widgetValue = WidgetProperties.selection().observe(partitioningCombo);
		IObservableValue modelValue = PojoProperties.value(IPartitionable.class,
				"partitioningMethod").observe(partitionable); //$NON-NLS-1$
		UpdateValueStrategy modelStrategy = new UpdateValueStrategy().setConverter(ConverterFactory
				.createPartitioningMethodModelConverter());
		UpdateValueStrategy targetStrategy = new UpdateValueStrategy()
				.setConverter(ConverterFactory.createPartitioningMethodTargetConverter());
		context.bindValue(widgetValue, modelValue, modelStrategy, targetStrategy);

		// Injecting model in underlying column component
		if (getModel() instanceof IBasicTable
				&& partitioningColumnsComponent instanceof IModelOriented<?>) {
			((IModelOriented<IPhysicalProperties>) partitioningColumnsComponent)
					.setModel(getModel().getPhysicalProperties());
		}
	}

	@Override
	protected void doRefresh() {
		if (getModel() != null && getModel().getPhysicalProperties() == null) {
			UIHelper.setEnablement(partitioningCombo.getParent(), false);
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
		switch (event) {
		case GENERIC_CHILD_ADDED:
		case GENERIC_CHILD_REMOVED:
			// When new physical properties are added, we need to rebind everything
			if (data instanceof IPhysicalProperties) {
				setModel((IPhysicalObject) source);
				return;
			}
			// Falling through
		}
		super.handleEvent(event, source, data);
	}
}
