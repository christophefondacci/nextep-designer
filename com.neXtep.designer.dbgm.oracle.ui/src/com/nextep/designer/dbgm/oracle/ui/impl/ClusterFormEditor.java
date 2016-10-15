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
package com.nextep.designer.dbgm.oracle.ui.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.dbgm.oracle.ui.actions.ClusteredTableColumnsActionProvider;
import com.nextep.designer.dbgm.oracle.ui.jface.ClusteredTableColumnsContentProvider;
import com.nextep.designer.dbgm.oracle.ui.jface.ClusteredTableColumnsLabelProvider;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.forms.FormComponentContainer;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class ClusterFormEditor extends AbstractFormEditor<IOracleClusteredTable> {

	private static final Log LOGGER = LogFactory.getLog(ClusterFormEditor.class);
	private IUIComponent clusteredTablesComponent;
	private Text tableText;
	private Button changeTableButton;

	public ClusterFormEditor() {
		super("Clustered table details",
				"Define the mapping between cluster and table columns here.", true);
	}

	@Override
	protected void doBindModel(DataBindingContext context) {
	}

	@Override
	protected void createControls(final IManagedForm managedForm, FormToolkit toolkit,
			Composite editor) {
		editor.setLayout(new GridLayout(3, false));
		// Creating the table editor
		final Link remoteTableLink = new Link(editor, SWT.NONE);
		remoteTableLink.setText(DBGMUIMessages.getString("editor.foreignKey.remoteTableLink")); //$NON-NLS-1$
		remoteTableLink.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				final IReference tableRef = getModel().getTableReference();
				if (tableRef != null) {
					try {
						final IBasicTable remoteTable = (IBasicTable) VersionHelper
								.getReferencedItem(tableRef);
						if (remoteTable != null) {
							final ITypedObjectUIController controller = UIControllerFactory
									.getController(IElementType.getInstance(IBasicTable.TYPE_ID));
							controller.defaultOpen(remoteTable);
						}
					} catch (ErrorException e) {
						// Ignoring, only logging in debug since this could happen often when table
						// not yet defined
						LOGGER.debug("Unable to open clustered table : " + e.getMessage(), e);
					}
				}
			}
		});
		tableText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		tableText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		tableText.setEditable(false);
		changeTableButton = toolkit.createButton(editor, "", SWT.PUSH); //$NON-NLS-1$
		changeTableButton.setImage(ImageFactory.ICON_EDIT_TINY);
		changeTableButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changeRemoteTable(managedForm.getForm().getShell());
			}
		});

		clusteredTablesComponent = CorePlugin.getService(ICommonUIService.class)
				.createTypedListComponent(new ClusteredTableColumnsLabelProvider(),
						new ClusteredTableColumnsContentProvider(),
						new ClusteredTableColumnsActionProvider(), getModel());
		clusteredTablesComponent.setUIComponentContainer(new FormComponentContainer(managedForm));
		final Control c = clusteredTablesComponent.create(editor);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
	}

	/**
	 * Prompts the user for a new remote table and defines this selection in the current foreign
	 * key.
	 */
	private void changeRemoteTable(Shell shell) {
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
		final IBasicTable remoteTable = (IBasicTable) uiService.findElement(
				shell,
				DBOMUIMessages.getString("addClusteredTableSelection"), //$NON-NLS-1$
				IElementType.getInstance(IBasicTable.TYPE_ID),
				IElementType.getInstance(IMaterializedView.VIEW_TYPE_ID));
		if (remoteTable != null) {
			final IOracleClusteredTable clusteredTable = getModel();
			final IOracleCluster cluster = clusteredTable.getCluster();
			clusteredTable.setTableReference(remoteTable.getReference());
			// Resetting every mapping to null
			for (IBasicColumn c : cluster.getColumns()) {
				clusteredTable.setColumnReferenceMapping(c.getReference(), null);
			}
		}
	}

	@Override
	protected void doRefresh() {
		final IReference tabRef = getModel().getTableReference();
		try {
			if (tabRef != null) {
				final IBasicTable table = (IBasicTable) VersionHelper.getReferencedItem(tabRef);
				tableText.setText(table.getName());
			} else {
				tableText.setText("");
			}
		} catch (ErrorException e) {
			tableText.setText("");
			LOGGER.error("Unresolved clustered table reference : " + e.getMessage(), e);
		}
		tableText.setEditable(false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setModel(IOracleClusteredTable model) {
		super.setModel(model);
		if (clusteredTablesComponent instanceof IModelOriented<?>) {
			((IModelOriented) clusteredTablesComponent).setModel(model);
		}
	}
}
