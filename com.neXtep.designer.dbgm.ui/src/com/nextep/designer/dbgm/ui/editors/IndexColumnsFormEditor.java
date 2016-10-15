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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.actions.IndexColumnsActionProvider;
import com.nextep.designer.dbgm.ui.jface.ColumnsContentProvider;
import com.nextep.designer.dbgm.ui.jface.IndexedColumnLabelProvider;
import com.nextep.designer.ui.forms.FormComponentContainer;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * Default index columns editor
 * 
 * @author Christophe Fondacci
 */
public class IndexColumnsFormEditor extends AbstractFormEditor<IIndex> {

	private IUIComponent columnsComponent;
	private IndexedColumnLabelProvider labelProvider;

	public IndexColumnsFormEditor() {
		super(
				DBGMUIMessages.getString("editor.index.columns.details"), DBGMUIMessages.getString("editor.index.columns.detailsDesc"), false); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void doBindModel(DataBindingContext context) {

	}

	@Override
	public void setModel(IIndex model) {
		super.setModel(model);
		if (columnsComponent instanceof IModelOriented<?>) {
			((IModelOriented<ITypedObject>) columnsComponent).setModel(model);
		}
		if (labelProvider != null) {
			labelProvider.setIndex(model);
		}
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {
		labelProvider = new IndexedColumnLabelProvider(getModel());
		// Creating the column definition component
		columnsComponent = CorePlugin.getService(ICommonUIService.class).createTypedListComponent(
				labelProvider, new ColumnsContentProvider(), new IndexColumnsActionProvider(),
				getModel());
		columnsComponent.setUIComponentContainer(new FormComponentContainer(managedForm));
		Control c = columnsComponent.create(editor);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

	}

	@Override
	protected void doRefresh() {

	}

}
