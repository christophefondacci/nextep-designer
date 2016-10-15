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
package com.nextep.designer.dbgm.ui.actions;

import java.util.Collections;
import java.util.List;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.jface.DbgmLabelProvider;
import com.nextep.designer.ui.dialogs.FormComponentDialogWrapper;
import com.nextep.designer.ui.model.IFormComponentContainer;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;
import com.nextep.designer.ui.model.base.AbstractUIComponent;
import com.nextep.designer.vcs.ui.jface.TypedInnerReferencesContentProvider;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class IndexColumnsActionProvider extends AbstractFormActionProvider {

	@Override
	public Object add(ITypedObject parent) {
		final IIndex index = (IIndex) parent;
		final IBasicTable parentTable = index.getIndexedTable();
		final IContentProvider provider = new TypedInnerReferencesContentProvider(
				index.getColumns(), IElementType.getInstance(IBasicColumn.TYPE_ID));
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);

		final ITypedObject column = uiService.findElement(Display.getDefault().getActiveShell(),
				"Select the table column to add to the index", parentTable, provider,
				new DbgmLabelProvider());
		if (column != null) {
			index.addColumnRef(((IBasicColumn) column).getReference());
			return column;
		}
		return null;
	}

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
		final IIndex index = (IIndex) parent;
		final IBasicColumn column = (IBasicColumn) toRemove;
		if (column != null) {
			index.removeColumnRef(column.getReference());
		}
	}

	@Override
	public void up(ITypedObject parent, ITypedObject element) {
		final IIndex index = (IIndex) parent;
		final IBasicColumn column = (IBasicColumn) element;
		if (column != null) {
			final IReference columnRef = column.getReference();
			final List<IReference> colRefs = index.getIndexedColumnsRef();
			int colIndex = colRefs.indexOf(columnRef);
			if (colIndex > 0) {
				final IBasicColumn swappedCol = index.getColumns().get(colIndex - 1);
				Collections.swap(colRefs, colIndex, colIndex - 1);
				index.notifyListeners(ChangeEvent.MODEL_CHANGED, column);
				column.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				swappedCol.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
			}
		}
	}

	@Override
	public void down(ITypedObject parent, ITypedObject element) {
		final IIndex index = (IIndex) parent;
		final IBasicColumn column = (IBasicColumn) element;
		if (column != null) {
			final IReference columnRef = column.getReference();
			final List<IReference> colRef = index.getIndexedColumnsRef();
			int colIndex = colRef.indexOf(columnRef);
			if (colIndex < colRef.size() - 1) {
				final IBasicColumn swappedCol = index.getColumns().get(colIndex + 1);
				Collections.swap(colRef, colIndex, colIndex + 1);
				index.notifyListeners(ChangeEvent.MODEL_CHANGED, column);
				column.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				swappedCol.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
			}
		}
	}

	@Override
	public boolean isSortable() {
		return true;
	}

	private class FunctionEditor extends AbstractUIComponent {

		private String prefixString;

		public FunctionEditor(String prefixLength) {
			this.prefixString = prefixLength;
		}

		@Override
		public Control create(Composite parent) {
			final IManagedForm form = ((IFormComponentContainer) getUIComponentContainer())
					.getForm();
			final FormToolkit toolkit = form.getToolkit();

			parent.setLayout(new GridLayout(2, false));
			toolkit.createLabel(parent, DBGMUIMessages.getString("editor.index.function")); //$NON-NLS-1$
			final Text prefixText = toolkit.createText(parent, prefixString);
			prefixText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			prefixText.addListener(SWT.Modify, new Listener() {

				@Override
				public void handleEvent(Event event) {
					prefixString = prefixText.getText();
				}
			});
			return prefixText;
		}

		public String getFunction() {
			if (prefixString == null) {
				return null;
			} else {
				return "".equals(prefixString.trim()) ? null : prefixString; //$NON-NLS-1$
			}
		}
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public void edit(ITypedObject parent, ITypedObject element) {
		final IIndex index = (IIndex) parent;
		final IReferenceable col = (IReferenceable) element;
		final String function = index.getFunction(col.getReference());
		final FunctionEditor editor = new FunctionEditor(function);
		final FormComponentDialogWrapper dlg = new FormComponentDialogWrapper(
				Display.getDefault().getActiveShell(),
				DBGMUIMessages.getString("editor.index.funcEditorTitle"), DBGMUIMessages.getString("editor.index.funcEditorHeader"), editor); //$NON-NLS-1$ //$NON-NLS-2$
		dlg.setBlockOnOpen(true);
		if (dlg.open() == Window.OK) {
			final String newFunction = editor.getFunction();
			index.setFunction(((IReferenceable) element).getReference(), newFunction);
		}
	}
}
