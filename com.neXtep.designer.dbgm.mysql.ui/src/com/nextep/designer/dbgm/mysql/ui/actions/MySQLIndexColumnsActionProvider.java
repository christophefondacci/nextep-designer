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
package com.nextep.designer.dbgm.mysql.ui.actions;

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
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.dbgm.mysql.model.IMySQLIndex;
import com.nextep.designer.dbgm.ui.actions.IndexColumnsActionProvider;
import com.nextep.designer.ui.dialogs.FormComponentDialogWrapper;
import com.nextep.designer.ui.model.IFormComponentContainer;
import com.nextep.designer.ui.model.base.AbstractUIComponent;

/**
 * @author Christophe Fondacci
 */
public class MySQLIndexColumnsActionProvider extends IndexColumnsActionProvider {

	private class PrefixEditor extends AbstractUIComponent {

		private String prefixString;

		public PrefixEditor(Integer prefixLength) {
			this.prefixString = prefixLength == null ? "" : prefixLength.toString();
		}

		@Override
		public Control create(Composite parent) {
			final IManagedForm form = ((IFormComponentContainer) getUIComponentContainer())
					.getForm();
			final FormToolkit toolkit = form.getToolkit();

			parent.setLayout(new GridLayout(2, false));
			toolkit.createLabel(parent, "Prefix length : ");
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

		public Integer getPrefixLength() {
			if (prefixString != null) {
				try {
					return Integer.parseInt(prefixString);
				} catch (NumberFormatException e) {
					return null;
				}
			}
			return null;
		}
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public void edit(ITypedObject parent, ITypedObject element) {
		final IMySQLIndex index = (IMySQLIndex) parent;
		final IReferenceable col = (IReferenceable) element;
		final Integer prefixLength = index.getColumnPrefixLength(col.getReference());
		final PrefixEditor editor = new PrefixEditor(prefixLength);
		final FormComponentDialogWrapper dlg = new FormComponentDialogWrapper(Display.getDefault()
				.getActiveShell(), "Define indexed column properties", "Properties", editor);
		dlg.setBlockOnOpen(true);
		if (dlg.open() == Window.OK) {
			final Integer prefix = editor.getPrefixLength();
			((IMySQLIndex) parent).setColumnPrefixLength(((IReferenceable) element).getReference(),
					prefix);
		}
	}
}
