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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.factories.ValidatorFactory;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;

/**
 * @author Christophe Fondacci
 */
public class TablePartitionFormEditor extends AbstractFormEditor<ITablePartition> {

	private Text highValueText;

	public TablePartitionFormEditor() {
		super(
				DBGMUIMessages.getString("editor.tabPartition.sectionTitle"), DBGMUIMessages.getString("editor.tabPartition.sectionDesc"), true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void doBindModel(DataBindingContext context) {
		IObservableValue selectionValue = ViewersObservables
				.observeSingleSelection(getSelectionProvider());

		// Binding name
		IObservableValue widgetValue = WidgetProperties.text(SWT.FocusOut).observe(highValueText);
		IObservableValue modelValue = PojoObservables.observeDetailValue(selectionValue,
				"highValue", String.class); //$NON-NLS-1$

		UpdateValueStrategy targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setAfterConvertValidator(ValidatorFactory.createNotEmptyValidator(
				"HIGH VALUE", false)); //$NON-NLS-1$
		Binding boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy, null);
		ControlDecorationSupport.create(boundValue, SWT.TOP | SWT.LEFT);
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {
		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.tabPartition.highValueLabel")); //$NON-NLS-1$
		highValueText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		highValueText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	@Override
	protected void doRefresh() {

	}

}
