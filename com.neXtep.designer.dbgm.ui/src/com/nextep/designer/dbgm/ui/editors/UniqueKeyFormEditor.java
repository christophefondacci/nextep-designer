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

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.actions.ConstraintColumnsActionProvider;
import com.nextep.designer.dbgm.ui.factories.ValidatorFactory;
import com.nextep.designer.dbgm.ui.jface.ColumnsContentProvider;
import com.nextep.designer.dbgm.ui.jface.DbgmLabelProvider;
import com.nextep.designer.ui.forms.FormComponentContainer;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class UniqueKeyFormEditor extends AbstractFormEditor<UniqueKeyConstraint> {

	private UniqueKeyConstraint uniqueKey;
	private Text nameText, descText;
	private Button primaryButton;
	private IUIComponent columnsComponent;

	public UniqueKeyFormEditor() {
		super(
				DBGMUIMessages.getString("editor.uniqueKey.details"), DBGMUIMessages.getString("editor.uniqueKey.detailsDesc"), true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {
		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.uniqueKey.name")); //$NON-NLS-1$
		nameText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.uniqueKey.desc")); //$NON-NLS-1$
		descText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		descText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(editor, ""); //$NON-NLS-1$
		primaryButton = toolkit.createButton(editor,
				DBGMUIMessages.getString("editor.uniqueKey.primaryKey"), SWT.CHECK); //$NON-NLS-1$
		primaryButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		columnsComponent = CorePlugin.getService(ICommonUIService.class).createTypedListComponent(
				new DbgmLabelProvider(), new ColumnsContentProvider(),
				new ConstraintColumnsActionProvider(), uniqueKey);
		columnsComponent.setUIComponentContainer(new FormComponentContainer(managedForm));
		Control c = columnsComponent.create(editor);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
	}

	@Override
	protected void doBindModel(DataBindingContext context) {
		IObservableValue selectionValue = ViewersObservables
				.observeSingleSelection(getSelectionProvider());

		// Binding name
		IObservableValue widgetValue = WidgetProperties.text(SWT.FocusOut).observe(nameText);
		IObservableValue modelValue = PojoObservables.observeDetailValue(selectionValue, "name", //$NON-NLS-1$
				String.class);

		UpdateValueStrategy targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setAfterConvertValidator(ValidatorFactory.createNameValidator(false));
		Binding boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy, null);
		ControlDecorationSupport.create(boundValue, SWT.TOP | SWT.LEFT);

		// Binding description
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(descText);
		modelValue = PojoObservables
				.observeDetailValue(selectionValue, "description", String.class); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue, null, null);

		// Binding primary flag
		widgetValue = WidgetProperties.selection().observe(primaryButton);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "constraintType", //$NON-NLS-1$
				ConstraintType.class);
		targetModelStrategy = new UpdateValueStrategy().setConverter(new IConverter() {

			@Override
			public Object getToType() {
				return ConstraintType.class;
			}

			@Override
			public Object getFromType() {
				return boolean.class;
			}

			@Override
			public Object convert(Object fromObject) {
				return (Boolean) fromObject ? ConstraintType.PRIMARY : ConstraintType.UNIQUE;
			}
		});
		UpdateValueStrategy modelTargetStrategy = new UpdateValueStrategy()
				.setConverter(new IConverter() {

					@Override
					public Object getToType() {
						return boolean.class;
					}

					@Override
					public Object getFromType() {
						return ConstraintType.class;
					}

					@Override
					public Object convert(Object fromObject) {
						return fromObject == ConstraintType.PRIMARY;
					}
				});
		boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy,
				modelTargetStrategy);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setModel(UniqueKeyConstraint model) {
		super.setModel(model);
		if (columnsComponent instanceof IModelOriented<?>) {
			((IModelOriented<ITypedObject>) columnsComponent).setModel(model);
		}
	}

	@Override
	protected void doRefresh() {
	}
}
