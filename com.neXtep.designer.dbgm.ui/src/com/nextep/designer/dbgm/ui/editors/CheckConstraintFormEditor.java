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
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.factories.ValidatorFactory;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;

/**
 * @author Christophe Fondacci
 */
public class CheckConstraintFormEditor extends AbstractFormEditor<ICheckConstraint> {

	private Text nameText, descriptionText, conditionText;

	public CheckConstraintFormEditor() {
		super(
				DBGMUIMessages.getString("editor.checkConstraint.title"), DBGMUIMessages.getString("editor.checkConstraint.desc"), true); //$NON-NLS-1$ //$NON-NLS-2$
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
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(descriptionText);
		modelValue = PojoObservables
				.observeDetailValue(selectionValue, "description", String.class); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue, null, null);

		// Binding check condition
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(conditionText);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "condition", String.class); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue, null, null);

	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		toolkit.createLabel(parent, DBGMUIMessages.getString("editor.checkConstraint.nameLbl")); //$NON-NLS-1$
		nameText = toolkit.createText(parent, "", SWT.BORDER); //$NON-NLS-1$
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(parent, DBGMUIMessages.getString("editor.checkConstraint.descLbl")); //$NON-NLS-1$
		descriptionText = toolkit.createText(parent, "", SWT.BORDER); //$NON-NLS-1$
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(parent,
				DBGMUIMessages.getString("editor.checkConstraint.checkConditionLbl")); //$NON-NLS-1$
		conditionText = toolkit.createText(parent, "", SWT.BORDER); //$NON-NLS-1$
		conditionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	@Override
	protected void doRefresh() {

	}

}
