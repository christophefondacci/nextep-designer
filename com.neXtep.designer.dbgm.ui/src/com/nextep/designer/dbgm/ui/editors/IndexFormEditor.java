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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.factories.ValidatorFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;

/**
 * @author Christophe Fondacci
 */
public class IndexFormEditor extends AbstractFormEditor<IIndex> {

	private static final Log LOGGER = LogFactory.getLog(IndexFormEditor.class);

	private Text nameText, descText, indexedTableText;
	private CCombo indexTypeCombo;

	public IndexFormEditor() {
		super(
				DBGMUIMessages.getString("editor.index.details"), DBGMUIMessages.getString("editor.index.detailsDesc"), false); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {
		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.index.name")); //$NON-NLS-1$
		nameText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.index.description")); //$NON-NLS-1$
		descText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		descText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Creating the remote table editor
		final Link remoteTableLink = new Link(editor, SWT.NONE);
		remoteTableLink.setText(DBGMUIMessages.getString("editor.index.indexedTable")); //$NON-NLS-1$
		remoteTableLink.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IBasicTable indexedTable = getModel().getIndexedTable();
				if (indexedTable != null) {
					final ITypedObjectUIController controller = UIControllerFactory
							.getController(IElementType.getInstance(IBasicTable.TYPE_ID));
					controller.defaultOpen(indexedTable);
				}
			}
		});
		indexedTableText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		indexedTableText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		indexedTableText.setEditable(false);
		// Available index types proposed within a Combo list
		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.index.type")); //$NON-NLS-1$
		indexTypeCombo = new CCombo(editor, SWT.BORDER | SWT.READ_ONLY);
		toolkit.adapt(indexTypeCombo);
		for (IndexType type : IndexType.values()) {
			if (type.isAvailableFor(DBGMHelper.getVendorFor((ITypedObject) getModel()))) {
				String typeName = IFormatter.PROPPER_LOWER.format(type.name()).replace('_', ' ');
				indexTypeCombo.add(typeName);
			}
		}
		indexTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

	}

	@Override
	protected void doBindModel(DataBindingContext context) {
		// IObservableValue selectionValue = ViewersObservables
		// .observeSingleSelection(getSelectionProvider());

		final IIndex index = getModel();
		// Binding name
		IObservableValue widgetValue = WidgetProperties.text(SWT.FocusOut).observe(nameText);
		IObservableValue modelValue = PojoProperties.value(IIndex.class, "indexName", String.class) //$NON-NLS-1$
				.observe(index);
		// Observables.value(observeDetailValue(selectionValue,
		//				"indexName", String.class); //$NON-NLS-1$

		UpdateValueStrategy targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setAfterConvertValidator(ValidatorFactory.createNameValidator(false));
		Binding boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy, null);
		ControlDecorationSupport.create(boundValue, SWT.TOP | SWT.LEFT);

		// Binding description
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(descText);
		modelValue = PojoProperties.value(IIndex.class, "description", String.class).observe(index); //$NON-NLS-1$
		// Observables
		//				.observeDetailValue(selectionValue, "description", String.class); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue, null, null);

		// Binding index type
		widgetValue = WidgetProperties.text().observe(indexTypeCombo);
		modelValue = PojoProperties.value(IIndex.class, "indexType", IndexType.class) //$NON-NLS-1$
				.observe(index);
		//		Observables.observeDetailValue(selectionValue, "indexType", //$NON-NLS-1$
		// IndexType.class);
		targetModelStrategy = new UpdateValueStrategy().setConverter(new IConverter() {

			@Override
			public Object getToType() {
				return IndexType.class;
			}

			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object convert(Object fromObject) {
				final String s = ((String) fromObject).replace(' ', '_').toUpperCase();
				return IndexType.valueOf(s);
			}
		});

		UpdateValueStrategy modelTargetStrategy = new UpdateValueStrategy()
				.setConverter(new IConverter() {

					@Override
					public Object getToType() {
						return String.class;
					}

					@Override
					public Object getFromType() {
						return IndexType.class;
					}

					@Override
					public Object convert(Object fromObject) {
						final IndexType type = (IndexType) fromObject;
						return IFormatter.PROPPER_LOWER.format(type.name()).replace('_', ' ');
					}
				});
		boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy,
				modelTargetStrategy);
	}

	@Override
	protected void doRefresh() {
		final IIndex index = getModel();
		try {
			final IBasicTable table = index.getIndexedTable();
			indexedTableText.setText(table.getName());
		} catch (ErrorException e) {
			LOGGER.error("Unable to retrieve indexed table instance : " + e.getMessage(), e); //$NON-NLS-1$
		}
		indexedTableText.setEditable(false);
	}

}
