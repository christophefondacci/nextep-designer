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
/**
 *
 */
package com.nextep.datadesigner.vcs.gui;

import java.util.Collection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.ui.controllers.ContainerUIController;

/**
 * @author Christophe Fondacci
 */
public class ContainerEditorGUI extends ControlledDisplayConnector implements IEventListener {

	private Composite group = null;
	private CLabel nameLbl = null;
	private Text nameTxt = null;
	private CLabel descLbl = null;
	private Text descTxt = null;
	private CLabel shortLbl = null;
	private Text shortTxt = null;
	private Composite parent = null;
	private Combo vendorCombo;
	private IDisplayConnector externalEditor;

	public ContainerEditorGUI(IVersionContainer container, ContainerUIController controller) {
		super(container, controller);
	}

	/**
	 * This method initializes group
	 */
	private void createGroup() {

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		group = new Composite(parent, SWT.BORDER);
		group.setLayoutData(gridData);
		group.setLayout(gridLayout);

	}

	private void createName() {
		GridData gridData1 = new GridData();
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.horizontalAlignment = GridData.FILL;
		nameLbl = new CLabel(group, SWT.RIGHT);
		nameLbl.setText("Container name : ");
		nameLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		nameTxt = new Text(group, SWT.BORDER);
		nameTxt.setLayoutData(gridData1);
		nameTxt.setTextLimit(30);
		TextEditor.handle(nameTxt, ChangeEvent.NAME_CHANGED, this);
		ColorFocusListener.handle(nameTxt);
	}

	private void createShortName() {
		GridData gridData1 = new GridData();
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.horizontalAlignment = GridData.FILL;
		shortLbl = new CLabel(group, SWT.RIGHT);
		shortLbl.setText("Short name : ");
		shortLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		shortTxt = new Text(group, SWT.BORDER);
		shortTxt.setLayoutData(gridData1);
		shortTxt.setTextLimit(20);
		TextEditor.handle(shortTxt, ChangeEvent.SHORTNAME_CHANGED, this);
		ColorFocusListener.handle(shortTxt);
	}

	private void createDescription() {
		GridData gridData1 = new GridData();
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.horizontalAlignment = GridData.FILL;
		descLbl = new CLabel(group, SWT.RIGHT);
		descLbl.setText("Description : ");
		descLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		descTxt = new Text(group, SWT.BORDER);
		descTxt.setLayoutData(gridData1);
		descTxt.setTextLimit(200);
		TextEditor.handle(descTxt, ChangeEvent.DESCRIPTION_CHANGED, this);
		ColorFocusListener.handle(descTxt);
	}

	private void createVendorCombo() {
		Label vendorLabel = new Label(group, SWT.RIGHT);
		vendorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		vendorLabel.setText("Vendor : ");
		vendorCombo = new Combo(group, SWT.READ_ONLY);
		vendorCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		ComboEditor.handle(vendorCombo, ChangeEvent.DBVENDOR_CHANGED, this);
		int i = 0;
		for (DBVendor v : DBVendor.values()) {
			vendorCombo.add(v.toString());
			vendorCombo.setData(v.toString(), v);
			if (v == VersionHelper.getCurrentView().getDBVendor()) {
				vendorCombo.select(i);
			}
			i++;
		}
		IVersionContainer c = (IVersionContainer) getModel();
		if (c.getDBVendor() == null) {
			vendorCombo.setEnabled(true);
			c.setDBVendor((DBVendor) vendorCombo.getData(vendorCombo.getText()));
		} else {
			vendorCombo.setEnabled(false);
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#addConnector(com.nextep.datadesigner.gui.model.IDisplayConnector)
	 */
	public void addConnector(IDisplayConnector child) {
		// Nonsense for a container
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createSWTControl(Composite parent) {
		this.parent = parent;
		// Creating SWT controls
		createGroup();
		createName();
		createShortName();
		createVendorCombo();
		createDescription();
		// Editor extensions
		Collection<IDisplayConnector> connectors = UIHelper.getEditorExtension(
				(ITypedObject) getModel(), null);
		// Only expecting 1 contribution, iterate to extend
		if (!connectors.isEmpty()) {
			externalEditor = connectors.iterator().next();
			TabFolder tab = new TabFolder(group, SWT.NONE);
			tab.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			// Creating the overview folder
			TabItem filtersTabItem = new TabItem(tab, SWT.NONE);
			filtersTabItem.setText("Synchronization filters");
			Composite filterPane = new Composite(tab, SWT.NONE);
			filterPane.setLayout(new GridLayout(1, false));
			filtersTabItem.setControl(filterPane);

			externalEditor.create(filterPane).setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, true));

		}
		group.addDisposeListener(this);
		return group;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#focus(com.nextep.datadesigner.gui.model.IDisplayConnector)
	 */
	public void focus(IDisplayConnector childFocus) {
		group.setFocus();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#refreshConnector()
	 */
	public void refreshConnector() {
		IVersionContainer container = (IVersionContainer) getModel();
		if (nameTxt.isDisposed()) {
			return;
		}
		nameTxt.setText(notNull(container.getName()));
		descTxt.setText(container.getDescription() == null ? "" : container.getDescription());
		shortTxt.setText(container.getShortName() == null ? "" : container.getShortName());

		// Refreshing vendor
		int index = 0;
		for (DBVendor v : DBVendor.values()) {
			if (container.getDBVendor() == v) {
				vendorCombo.select(index);
				break;
			}
			index++;
		}
		final boolean b = !container.updatesLocked();
		nameTxt.setEnabled(b);
		descTxt.setEnabled(b);
		shortTxt.setEnabled(b);
		externalEditor.refreshConnector();
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IVersionContainer container = (IVersionContainer) getModel();
		switch (event) {
		case NAME_CHANGED:
			container.setName((String) data);
			break;
		case DESCRIPTION_CHANGED:
			container.setDescription((String) data);
			break;
		case SHORTNAME_CHANGED:
			container.setShortName((String) data);
			break;
		// case MODEL_CHANGED:
		// refreshConnector();
		// //controller.modelChanged(container);
		// break;
		}
		refreshConnector();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getSWTConnector()
	 */
	public Control getSWTConnector() {
		return group;
	}

}
