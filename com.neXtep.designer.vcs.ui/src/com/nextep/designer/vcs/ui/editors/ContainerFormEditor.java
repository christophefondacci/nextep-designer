package com.nextep.designer.vcs.ui.editors;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;

public class ContainerFormEditor extends AbstractFormEditor<IVersionContainer> {

	private Text nameText, shortText, descText, schemaText;
	private CCombo vendorCombo;

	public ContainerFormEditor() {
		super(VCSUIMessages.getString("editor.container.title"), VCSUIMessages //$NON-NLS-1$
				.getString("editor.container.description"), false); //$NON-NLS-1$

	}

	@Override
	protected void doBindModel(DataBindingContext context) {
		final IVersionContainer container = getModel();
		// Binding name
		IObservableValue widgetValue = WidgetProperties.text(SWT.FocusOut).observe(nameText);
		IObservableValue modelValue = PojoProperties
				.value(IVersionContainer.class, "name").observe(container); //$NON-NLS-1$
		Binding boundValue = context.bindValue(widgetValue, modelValue);

		// Binding description
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(descText);
		modelValue = PojoProperties
				.value(IVersionContainer.class, "description").observe(container); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue);

		// Binding short text
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(shortText);
		modelValue = PojoProperties.value(IVersionContainer.class, "shortName").observe(container); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue);

		// Binding schema text
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(schemaText);
		modelValue = PojoProperties.value(IVersionContainer.class, "schemaName").observe(container); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue);
		// Binding database vendor
		// widgetValue = WidgetProperties.selection().observe(vendorCombo);
		// modelValue = PojoProperties
		//				.value(IVersionContainer.class, "DBVendor.name").observe(container); ////$NON-NLS-1$
		// boundValue = context.bindValue(widgetValue, modelValue);

	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {
		editor.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		editor.setLayoutData(gd);

		// Name edition section
		toolkit.createLabel(editor, VCSUIMessages.getString("editor.container.name")); //$NON-NLS-1$
		nameText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Short name edition section
		toolkit.createLabel(editor, VCSUIMessages.getString("editor.container.shortName")); //$NON-NLS-1$
		shortText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		shortText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Description edition section
		toolkit.createLabel(editor, VCSUIMessages.getString("editor.container.desc")); //$NON-NLS-1$
		descText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		descText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Short name edition section
		toolkit.createLabel(editor, VCSUIMessages.getString("editor.container.vendor")); //$NON-NLS-1$
		vendorCombo = new CCombo(editor, SWT.READ_ONLY | SWT.BORDER);
		vendorCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		toolkit.adapt(vendorCombo);
		// ComboEditor.handle(vendorCombo, ChangeEvent.DBVENDOR_CHANGED, this);
		int i = 0;
		for (DBVendor v : DBVendor.values()) {
			vendorCombo.add(v.toString());
			vendorCombo.setData(v.toString(), v);
			if (v == VCSPlugin.getViewService().getCurrentWorkspace().getDBVendor()) {
				vendorCombo.select(i);
			}
			i++;
		}
		IVersionContainer c = getModel();
		if (c.getDBVendor() == null) {
			vendorCombo.setEnabled(true);
			c.setDBVendor((DBVendor) vendorCombo.getData(vendorCombo.getText()));
		} else {
			vendorCombo.setEnabled(false);
		}
		
		
		// Schema name edition section
		toolkit.createLabel(editor, VCSUIMessages.getString("editor.container.schema")); //$NON-NLS-1$
		schemaText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		schemaText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	@Override
	protected void doRefresh() {
		// Refreshing vendor
		int index = 0;
		for (DBVendor v : DBVendor.values()) {
			if (getModel().getDBVendor() == v) {
				vendorCombo.select(index);
				break;
			}
			index++;
		}

	}

}
