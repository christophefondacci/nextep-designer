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

import java.util.List;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.ui.BENGImages;
import com.nextep.designer.beng.ui.BengUIMessages;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 *
 */
public class DependencySelector extends WizardDisplayConnector {
	private Composite editor = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Label moduleLabel = null;
	private Combo moduleCombo = null;
	private Label releaseLabel = null;
	private Combo releaseCombo = null;

	private IVersionContainer 	selectedContainer;
	private IVersionInfo		selectedRelease;
	
	public DependencySelector() {
		super("Dependency selector",BengUIMessages.getString("dependencySelector.title"),ImageDescriptor.createFromImage(BENGImages.ICON_NEW_DEPENDENCY)); //$NON-NLS-2$
	}
	/**
	 * This method initializes moduleCombo	
	 *
	 */
	private void createModuleCombo() {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.horizontalAlignment = GridData.FILL;
		moduleCombo = new Combo(editor, SWT.READ_ONLY);
		moduleCombo.setLayoutData(gridData);
		
		List<IVersionable<?>> modules = VersionHelper.getAllVersionables(VersionHelper.getCurrentView(),IElementType.getInstance(IVersionContainer.TYPE_ID));
		for(IVersionable<?> v : modules) {
			moduleCombo.add(v.getName());
			moduleCombo.setData(v.getName(),v);
		}
		ComboEditor.handle(moduleCombo, ChangeEvent.CONTAINER_CHANGED, this);
	}

	/**
	 * This method initializes releaseCombo	
	 *
	 */
	private void createReleaseCombo() {
		GridData gridData3 = new GridData();
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.verticalAlignment = GridData.CENTER;
		gridData3.horizontalAlignment = GridData.FILL;
		releaseCombo = new Combo(editor, SWT.READ_ONLY);
		releaseCombo.setLayoutData(gridData3);
		ComboEditor.handle(releaseCombo, ChangeEvent.RELEASE_CHANGED, this);
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createSWTControl(Composite parent) {
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.verticalAlignment = GridData.CENTER;
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		editor = new Composite(parent,SWT.NONE);
		editor.setLayout(gridLayout);
		editor.setSize(new Point(300, 107));
		moduleLabel = new Label(editor, SWT.NONE);
		moduleLabel.setText(BengUIMessages.getString("dependencySelector.module")); //$NON-NLS-1$
		moduleLabel.setLayoutData(gridData2);
		createModuleCombo();
		releaseLabel = new Label(editor, SWT.NONE);
		releaseLabel.setText(BengUIMessages.getString("dependencySelector.release")); //$NON-NLS-1$
		releaseLabel.setLayoutData(gridData1);
		createReleaseCombo();
		// Returning our main composite control
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		super.refreshConnector();
		releaseCombo.removeAll();
		IVersionable<IVersionContainer> c = (IVersionable<IVersionContainer>)moduleCombo.getData(moduleCombo.getText());
		if(c!=null) {
			IVersionInfo version = c.getVersion();
			while(version!=null) {
				releaseCombo.add(version.getLabel());
				releaseCombo.setData(version.getLabel(), version);
				version = version.getPreviousVersion();
			}
			if(selectedRelease!=null) {
				releaseCombo.setText(selectedRelease.getLabel());
			}
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.model.IModelOriented#getModel()
	 */
	@Override
	public Object getModel() {
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case CONTAINER_CHANGED:
			selectedContainer = (IVersionContainer)moduleCombo.getData(moduleCombo.getText());
			break;
		case RELEASE_CHANGED:
			selectedRelease = (IVersionInfo)releaseCombo.getData(releaseCombo.getText());
			break;
		}
		super.handleEvent(event, source, data);
	}
	
	public IVersionContainer getSelectedModule() {
		return selectedContainer;
	}
	
	public IVersionInfo getSelectedRelease() {
		return selectedRelease;
	}
}
