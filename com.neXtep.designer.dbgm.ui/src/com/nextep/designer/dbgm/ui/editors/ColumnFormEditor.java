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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.LengthType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.factories.ConverterFactory;
import com.nextep.designer.dbgm.ui.factories.ValidatorFactory;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;

/**
 * @author Christophe Fondacci
 */
public class ColumnFormEditor extends AbstractFormEditor<IBasicColumn> {

	private Text nameText, descText, lengthText, precisionText, defaultText;
	private Button notNullButton;
	private Button virtualButton;
	private CCombo datatypeCombo;
	private CCombo lengthTypeCombo;
	private final Collection<ControlDecorationSupport> fieldDecorators = new ArrayList<ControlDecorationSupport>();

	public ColumnFormEditor() {
		super(
				DBGMUIMessages.getString("editor.columns.details"), DBGMUIMessages.getString("editor.columns.detailsDesc"), true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {

		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.columns.name")); //$NON-NLS-1$
		nameText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.columns.desc")); //$NON-NLS-1$
		descText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		descText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.columns.datatype")); //$NON-NLS-1$
		datatypeCombo = new CCombo(editor, SWT.BORDER);
		toolkit.adapt(datatypeCombo);
		List<String> datatypes = DBGMHelper.getDatatypeProvider(DBGMHelper.getCurrentVendor())
				.listSupportedDatatypes();
		Collections.sort(datatypes);
		for (String datatype : datatypes) {
			datatypeCombo.add(datatype);
		}
		datatypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.columns.length")); //$NON-NLS-1$
		// Composite holding length and dimension (BYTE / CHAR)
		final Composite lengthComposite = toolkit.createComposite(editor);
		GridLayout gl = new GridLayout(2, false);
		gl.marginBottom = gl.marginHeight = gl.marginLeft = gl.marginRight = gl.marginTop = gl.marginWidth = 0;
		lengthComposite.setLayout(gl);
		lengthComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// First length field
		lengthText = toolkit.createText(lengthComposite, "", SWT.BORDER); //$NON-NLS-1$
		lengthText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// Dimension (CHAR or BYTE)
		lengthTypeCombo = new CCombo(lengthComposite, SWT.BORDER);
		for (LengthType type : LengthType.values()) {
			lengthTypeCombo.add(type.name());
		}
		lengthTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		lengthTypeCombo.setEditable(false);

		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.columns.precision")); //$NON-NLS-1$
		precisionText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		precisionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(editor, ""); //$NON-NLS-1$
		notNullButton = toolkit.createButton(editor,
				DBGMUIMessages.getString("editor.columns.notNull"), SWT.CHECK); //$NON-NLS-1$

		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.columns.default")); //$NON-NLS-1$
		defaultText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		defaultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(editor, ""); //$NON-NLS-1$
		virtualButton = toolkit.createButton(editor,
				DBGMUIMessages.getString("editor.columns.virtual"), SWT.CHECK); //$NON-NLS-1$
	}

	@Override
	protected void doBindModel(DataBindingContext context) {
		IObservableValue selectionValue = ViewersObservables
				.observeSingleSelection(getSelectionProvider());

		IObservableValue widgetValue = WidgetProperties.text(SWT.FocusOut).observe(nameText);
		IObservableValue modelValue = PojoObservables.observeDetailValue(selectionValue, "name", //$NON-NLS-1$
				String.class);

		UpdateValueStrategy targetModelStrategy = new UpdateValueStrategy();
		// UpdateValueStrategy.POLICY_CONVERT);
		targetModelStrategy.setAfterConvertValidator(ValidatorFactory.createNameValidator(false));
		Binding boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy, null);
		fieldDecorators.add(ControlDecorationSupport.create(boundValue, SWT.TOP | SWT.LEFT));

		// Binding description
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(descText);
		modelValue = PojoObservables
				.observeDetailValue(selectionValue, "description", String.class); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue);

		// Binding datatype
		widgetValue = WidgetProperties.selection().observe(datatypeCombo);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "datatype.name", //$NON-NLS-1$
				String.class);
		boundValue = context.bindValue(widgetValue, modelValue);

		final IValidator integerValidator = ValidatorFactory.createIntegerValidator();
		// Binding length
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(lengthText);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "datatype.length", //$NON-NLS-1$
				int.class);
		targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setAfterGetValidator(integerValidator);
		targetModelStrategy.setConverter(ConverterFactory.createToIntegerConverter(true, true));

		UpdateValueStrategy modelTargetStrategy = new UpdateValueStrategy();
		modelTargetStrategy.setAfterGetValidator(integerValidator);
		modelTargetStrategy.setConverter(ConverterFactory.createFromIntegerConverter(true, true));
		boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy,
				modelTargetStrategy);
		fieldDecorators.add(ControlDecorationSupport.create(boundValue, SWT.TOP | SWT.LEFT));

		// Binding length type
		widgetValue = WidgetProperties.selection().observe(lengthTypeCombo);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "datatype.lengthType", //$NON-NLS-1$
				String.class);
		targetModelStrategy = new UpdateValueStrategy().setConverter(ConverterFactory
				.createLengthTypeModelConverter());
		modelTargetStrategy = new UpdateValueStrategy().setConverter(ConverterFactory
				.createLengthTypeTargetConverter());
		boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy,
				modelTargetStrategy);

		// Binding precision
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(precisionText);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "datatype.precision", //$NON-NLS-1$
				int.class);
		targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setAfterGetValidator(integerValidator);
		targetModelStrategy.setConverter(ConverterFactory.createToIntegerConverter(true, true));

		modelTargetStrategy = new UpdateValueStrategy();
		modelTargetStrategy.setAfterGetValidator(integerValidator);
		modelTargetStrategy.setConverter(ConverterFactory.createFromIntegerConverter(true, true));
		boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy,
				modelTargetStrategy);
		fieldDecorators.add(ControlDecorationSupport.create(boundValue, SWT.TOP | SWT.LEFT));

		// Binding not null
		widgetValue = WidgetProperties.selection().observe(notNullButton);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "notNull", boolean.class); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue);

		// Binding virtual
		widgetValue = WidgetProperties.selection().observe(virtualButton);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "virtual", boolean.class); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue);

		// Binding default
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(defaultText);
		modelValue = PojoObservables
				.observeDetailValue(selectionValue, "defaultExpr", String.class); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue);
	}

	@Override
	protected void doRefresh() {
		final DBVendor currentVendor = DBGMHelper.getCurrentVendor();
		final IDatatypeProvider provider = DBGMHelper.getDatatypeProvider(currentVendor);

		final List<String> stringDatatypes = provider.listStringDatatypes();
		final List<String> dateDatatypes = provider.getDateDatatypes();

		final IDatatype d = getModel().getDatatype();
		final String datatype = d.getName().toUpperCase();

		// We show length type if string datatype and not date
		boolean showSizeType = stringDatatypes.contains(datatype)
				&& !dateDatatypes.contains(datatype) && currentVendor == DBVendor.ORACLE;
		lengthTypeCombo.setVisible(showSizeType);

		// If not shown, we make length type UNDEFINED
		if (!showSizeType && d.getLengthType() != LengthType.UNDEFINED) {
			if (VersionHelper.ensureModifiable(getModel().getParent(), false)) {
				d.setLengthType(LengthType.UNDEFINED);
			}
		}
	}

}
