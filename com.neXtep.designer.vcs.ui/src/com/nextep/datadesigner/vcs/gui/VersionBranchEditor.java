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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionBranch;

/**
 * @author Christophe Fondacci
 *
 */
public class VersionBranchEditor extends ControlledDisplayConnector implements
		SelectionListener {

	private IVersionBranch branch;
	private Composite group;
	private CLabel branchLabel;
	private Combo branchCombo;
	private CLabel newLabel;
	private Text branchText;
//	private Button newBranchButton;
	public VersionBranchEditor(IVersionBranch branch) {
		super(branch,null);
		this.branch=branch;
	}
	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(Object model) {
		this.branch=(IVersionBranch)model;

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
        GridData gridData31 = new GridData();
        gridData31.horizontalAlignment = GridData.FILL;
        gridData31.verticalAlignment = GridData.CENTER;
        GridData gridData18 = new GridData();
        gridData18.horizontalAlignment = GridData.FILL;
        gridData18.verticalAlignment = GridData.CENTER;
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.FILL;
        gridData1.verticalAlignment = GridData.CENTER;
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.FILL;
        gridData2.grabExcessHorizontalSpace=true;
		group = new Composite(parent,SWT.NONE);
		GridLayout l = new GridLayout();
        l.marginBottom=l.marginHeight=l.marginLeft=l.marginRight=l.marginTop
        =l.marginWidth=0;
        l.numColumns=2;
        group.setLayout(l);
	    branchLabel = new CLabel(group, SWT.RIGHT);
        branchLabel.setText("Existing branches : ");
        branchLabel.setLayoutData(gridData18);
        createBranchCombo();
        newLabel = new CLabel(group,SWT.RIGHT);
        newLabel.setText("Branch name : ");
        newLabel.setLayoutData(gridData1);
        branchText = new Text(group,SWT.BORDER);
        branchText.setLayoutData(gridData2);
        ColorFocusListener.handle(branchText);
        TextEditor.handle(branchText, ChangeEvent.NAME_CHANGED, this);
		// Dispose listener
        group.addDisposeListener(this);
		return group;
	}
    /**
     * This method initializes branchCombo
     *
     */
    private void createBranchCombo() {
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = GridData.FILL;
        gridData4.verticalAlignment = GridData.CENTER;
        branchCombo = new Combo(group, SWT.READ_ONLY);
        branchCombo.setLayoutData(gridData4);
        for(IVersionBranch b : VersionHelper.listBranches()) {
        	branchCombo.add(b.getName());
        	branchCombo.setData(b.getName(), b);
        }
  //      branchCombo.addSelectionListener(this);
    }

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getModel()
	 */
	@Override
	public Object getModel() {
		return branch;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return group;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		return branch.getName();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		for(int i = 0 ; i < branchCombo.getItemCount() ; i++) {
			if(branch.getName().equals(branchCombo.getItem(i))) {
				branchCombo.select(i);
				break;
			}
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case NAME_CHANGED:
			branch.setName((String)data);
			break;
		}
		refreshConnector();

	}
	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);

	}
	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
//		if(e.getSource() == branchCombo) {
//			int index = branchCombo.getSelectionIndex();
//			if(index != -1) {
//				IVersionBranch newBranch = (IVersionBranch)branchCombo.getData(branchCombo.getItem(index));
//				version.setBranch(newBranch);
//			}
//		}

	}

}
