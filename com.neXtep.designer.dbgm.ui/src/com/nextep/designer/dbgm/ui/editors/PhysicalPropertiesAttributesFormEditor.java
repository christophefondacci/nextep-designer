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
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.editors.TextColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;

/**
 * @author Christophe Fondacci
 */
public class PhysicalPropertiesAttributesFormEditor extends AbstractFormEditor<IPhysicalObject> {

	private static final Log LOGGER = LogFactory
			.getLog(PhysicalPropertiesAttributesFormEditor.class);
	private Table attrTable;

	public PhysicalPropertiesAttributesFormEditor() {
		super(
				DBGMUIMessages.getString("editor.physAttrs.sectionTitle"), DBGMUIMessages.getString("editor.physAttrs.sectionDesc"), true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void doBindModel(DataBindingContext context) {

	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite parent) {
		attrTable = toolkit.createTable(parent, SWT.BORDER | SWT.FULL_SELECTION);
		GridData attrData = new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1);
		attrData.minimumHeight = 200;
		attrTable.setLayoutData(attrData);
		attrTable.setLinesVisible(true);
		attrTable.setHeaderVisible(true);
		TableColumn attrNameCol = new TableColumn(attrTable, SWT.NONE);
		attrNameCol.setWidth(150);
		attrNameCol.setText(DBGMUIMessages.getString("editor.physAttrs.attributeColumnTitle")); //$NON-NLS-1$
		TableColumn attrValueCol = new TableColumn(attrTable, SWT.NONE);
		attrValueCol.setWidth(70);
		attrValueCol.setText(DBGMUIMessages.getString("editor.physAttrs.attributeValueTitle")); //$NON-NLS-1$

		for (PhysicalAttribute attr : PhysicalAttribute.values()) {
			TableItem i = new TableItem(attrTable, SWT.NONE);
			i.setText(attr.name());
			i.setData(attr);
			attrTable.setData(attr.name(), i);
		}
		NextepTableEditor tabEditor = NextepTableEditor.handle(attrTable);
		TextColumnEditor.handle(tabEditor, 1, ChangeEvent.PHYSICAL_ATTR_CHANGED, this);

	}

	@Override
	protected void doRefresh() {
		final IPhysicalProperties props = getModel().getPhysicalProperties();
		final boolean partitioned = props instanceof IPartitionable ? ((IPartitionable) props)
				.getPartitioningMethod() != PartitioningMethod.NONE : false;
		for (PhysicalAttribute a : PhysicalAttribute.values()) {
			TableItem i = (TableItem) attrTable.getData(a.name());
			if (!partitioned) {
				i.setText(1, strVal(props == null ? "" : props.getAttribute(a))); //$NON-NLS-1$
			} else {
				// We do not display attribute values for partitioned tables
				// since they will never be used. But they are not erased for
				// user convenience.
				i.setText(1, ""); //$NON-NLS-1$
			}
		}
		final ICoreService coreService = CorePlugin.getService(ICoreService.class);
		final ILockable<?> lockable = coreService.getLockable(getModel());
		final boolean enabled = lockable == null ? true : !lockable.updatesLocked();
		attrTable.setEnabled(enabled && !partitioned);
	}

	private String strVal(Object o) {
		return o == null ? "" : o.toString(); //$NON-NLS-1$
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		final IPhysicalProperties props = getModel().getPhysicalProperties();
		switch (event) {
		case PHYSICAL_ATTR_CHANGED:
			try {
				final Integer val = Integer.parseInt((String) data);
				props.setAttribute((PhysicalAttribute) source,
						data == null || "".equals(((String) data).trim()) ? null : val);//$NON-NLS-1$
			} catch (NumberFormatException e) {
				LOGGER.error("Unable to parse " + data + " as a valid integer value");
			}
			refresh();
			break;
		case GENERIC_CHILD_ADDED:
			if (data instanceof IPhysicalProperties) {
				bindModel();
				Designer.getListenerService().registerListener(this, (IPhysicalProperties) data,
						this);
			}
		default:
			super.handleEvent(event, source, data);
		}
	}

	@Override
	public void setModel(IPhysicalObject model) {
		super.setModel(model);
		if (model != null) {
			Designer.getListenerService().registerListener(this, model.getPhysicalProperties(),
					this);
			bindController(model.getPhysicalProperties());
		}
	}
}
