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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.datadesigner.beng.gui;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.beng.gui.swt.DeliveryListTable;
import com.nextep.datadesigner.gui.impl.swt.TableColumnSorter;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.ui.BengUIMessages;
import com.nextep.designer.beng.ui.jface.DeliveryContainerInfoContentProvider;
import com.nextep.designer.beng.ui.jface.DeliveryInfoTableLabelProvider;
import com.nextep.designer.beng.ui.jface.VendorDeliveriesContentProvider;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.ui.jface.ContainerInfoLabelProvider;
import com.nextep.designer.vcs.ui.jface.ContainerInfoTable;

public class VersionViewDeliveriesEditor extends WizardDisplayConnector {

	private IDeliveryInfo selectedDelivery = null;
	private IWorkspace view;
	private SashForm sash;
	private TableViewer deliveriesTableViewer;

	public VersionViewDeliveriesEditor(IWorkspace view) {
		super("Delivery selection", "Delivery selection", null);
		setMessage(BengUIMessages.getString("viewDeliveryWizardText")); //$NON-NLS-1$
		setPageComplete(false);
		this.view = view;
		Designer.getListenerService().registerListener(this, view, this);
	}

	public IDeliveryInfo getSelectedDelivery() {
		return selectedDelivery;
	}

	@Override
	public Control createSWTControl(Composite parent) {
		sash = new SashForm(parent, SWT.HORIZONTAL);
		GridData gd = new GridData();
		gd.widthHint = 100;
		gd.heightHint = 300;
		sash.setLayoutData(gd);

		final Composite leftComposite = new Composite(sash, SWT.NONE);
		final GridLayout grid = new GridLayout(1, false);
		grid.marginBottom = grid.marginHeight = grid.marginLeft = grid.marginRight = grid.marginTop = 0;
		leftComposite.setLayout(grid);

		final Table deliveriesTab = DeliveryListTable.create(leftComposite);
		GridData dlvTabData = new GridData(SWT.FILL, SWT.FILL, true, true);
		dlvTabData.widthHint = 250;
		dlvTabData.heightHint = 300;
		deliveriesTab.setLayoutData(dlvTabData);
		deliveriesTableViewer = new TableViewer(deliveriesTab);
		deliveriesTableViewer.setContentProvider(new VendorDeliveriesContentProvider());
		deliveriesTableViewer.setLabelProvider(new DeliveryInfoTableLabelProvider());
		deliveriesTableViewer.setComparator(new TableColumnSorter(deliveriesTab,
				deliveriesTableViewer));
		setControl(sash);

		final Composite rightComposite = new Composite(sash, SWT.NONE);
		final GridLayout rightGrid = new GridLayout(1, false);
		rightGrid.marginBottom = rightGrid.marginHeight = rightGrid.marginLeft = rightGrid.marginRight = rightGrid.marginTop = 0;
		rightComposite.setLayout(rightGrid);
		final Table containerInfoTab = ContainerInfoTable.create(rightComposite);
		final TableViewer contentsTable = new TableViewer(containerInfoTab);
		contentsTable.setContentProvider(new DeliveryContainerInfoContentProvider());
		contentsTable.setLabelProvider(new ContainerInfoLabelProvider());
		GridData contData = new GridData(SWT.FILL, SWT.FILL, true, true);
		contData.widthHint = 150;
		contData.heightHint = 300;
		containerInfoTab.setLayoutData(contData);
		deliveriesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectedDelivery = (IDeliveryInfo) selection.getFirstElement();
				contentsTable.setInput(selectedDelivery);
				setPageComplete(true);
			}
		});
		sash.pack();
		return sash;
	}

	@Override
	public Control getSWTConnector() {
		return sash;
	}

	@Override
	public Object getModel() {
		return view;
	}

	@Override
	public void refreshConnector() {
		deliveriesTableViewer.setInput(view.getDBVendor());
	}

	@Override
	public void releaseConnector() {
		super.releaseConnector();
		if (view != null) {
			Designer.getListenerService().unregisterListener(view, this);
		}
	}
}
