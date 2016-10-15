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
package com.nextep.datadesigner.vcs.gui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.vcs.gef.VersionTreeGUI;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 *
 */
public class MergeInitGUI extends WizardDisplayConnector {

	private Composite dialog = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private CLabel nameLabel = null;
	private Text nameText = null;
	private CLabel branchLabel = null;
	private Text branchText = null;
	private CLabel releaseLabel = null;
	private Text releaseText = null;
	private CLabel TargetLabel = null;
	private CLabel sourceLabel = null;
	private Label label = null;
	private Control composite = null;
	private IVersionable<?> versionable;
	private VersionTreeGUI versionTreeGUI;
	public MergeInitGUI(IVersionable<?> versionable) {
		super("Initialization","Initializing merge of " + versionable.getType().getName().toLowerCase() + " " + versionable.getName(),null);//ImageDescriptor.createFromImage(VCSImages.ICON_MERGE));
		this.versionable = versionable;
	}
	/**
	 * This method initializes sShell
	 */
	private void createSShell(Composite parent) {
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.FILL;
		gridData11.horizontalSpan = 2;
		gridData11.verticalSpan = 2;
		gridData11.verticalAlignment = GridData.CENTER;
		GridData gridData7 = new GridData();
		gridData7.horizontalSpan = 2;
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.FILL;
		gridData6.verticalAlignment = GridData.CENTER;
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.verticalAlignment = GridData.CENTER;
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.verticalAlignment = GridData.CENTER;
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.verticalAlignment = GridData.CENTER;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.verticalAlignment = GridData.CENTER;
		GridData gridData1 = new GridData();
		gridData1.horizontalSpan = 2;
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.horizontalAlignment = GridData.FILL;
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginBottom=gridLayout.marginHeight=gridLayout.marginLeft=gridLayout.marginRight=
			gridLayout.marginTop=gridLayout.marginWidth=0;
		gridLayout.numColumns = 2;
		dialog = new Composite(parent, SWT.SHELL_TRIM);
	//	sShell.setText("Merge Wizard");
		dialog.setLayout(gridLayout);
		dialog.setSize(new Point(430, 500));
		TargetLabel = new CLabel(dialog, SWT.NONE);
		TargetLabel.setText("Target description");
		TargetLabel.setFont(FontFactory.FONT_BOLD);
		TargetLabel.setLayoutData(gridData1);
		nameLabel = new CLabel(dialog, SWT.RIGHT);
		nameLabel.setText("Name : ");
		nameLabel.setLayoutData(gridData2);
		nameText = new Text(dialog, SWT.BORDER);
		nameText.setEditable(false);
		nameText.setLayoutData(gridData);
		branchLabel = new CLabel(dialog, SWT.RIGHT);
		branchLabel.setText("Branch : ");
		branchLabel.setLayoutData(gridData3);
		branchText = new Text(dialog, SWT.BORDER);
		branchText.setEditable(false);
		branchText.setLayoutData(gridData5);
		releaseLabel = new CLabel(dialog, SWT.RIGHT);
		releaseLabel.setText("Release : ");
		releaseLabel.setLayoutData(gridData4);
		releaseText = new Text(dialog, SWT.BORDER);
		releaseText.setEditable(false);
		releaseText.setLayoutData(gridData6);
		sourceLabel = new CLabel(dialog, SWT.NONE);
		sourceLabel.setFont(FontFactory.FONT_BOLD);
		sourceLabel.setLayoutData(gridData7);
		sourceLabel.setText("Source description");
		label = new Label(dialog, SWT.WRAP);
		label.setText("Pick one of the releases in the version tree below. ");
		label.setFont(FontFactory.FONT_ITALIC);
		label.setLayoutData(gridData11);
		createComposite();
	}

	/**
	 * This method initializes composite
	 *
	 */
	private void createComposite() {
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = GridData.FILL;
		gridData8.horizontalSpan = 2;
		gridData8.grabExcessHorizontalSpace = true;
		gridData8.grabExcessVerticalSpace = true;
		gridData8.verticalAlignment = GridData.FILL;
		versionTreeGUI = new VersionTreeGUI(versionable,SWT.HORIZONTAL,true,null);
		composite = versionTreeGUI.create(dialog);
		Designer.getListenerService().registerListener(versionTreeGUI.getSWTConnector(),versionTreeGUI,this);

//		composite = new Composite(sShell, SWT.NONE);
//		composite.setLayout(new GridLayout());
		composite.setLayoutData(gridData8);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createSWTControl(Composite parent) {
		createSShell(parent);
		dialog.layout();
		dialog.addDisposeListener(this);
		return dialog;
	}

	/**
	 *
	 * @see com.nextep.datadesigner.gui.model.IConnector#getModel()
	 */
	@Override
	public Object getModel() {
		return versionTreeGUI.getModel();
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return dialog;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		nameText.setText(versionable.getName());
		branchText.setText(versionable.getVersion().getBranch().getName());
		releaseText.setText(versionable.getVersion().getLabel());
		versionTreeGUI.refreshConnector();
	}

	/**
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
		return "The merge operation will create a new release of the selected checked-in element \r\n" +
		"to integrate updates from another release which has evolved independantly"; // \r\n"+
		//"(typically from another branch).";
	}
	/**
	 * @see org.eclipse.jface.dialogs.DialogPage#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		MergeWizard wiz = (MergeWizard)getWizard();
		if(versionTreeGUI.getModel() == null || wiz.getFromRelease() == null) {
			setPageComplete(false);
			return "Select a source release from the version tree to continue with merge";
		} else if( wiz.getFromRelease().getStatus() != IVersionStatus.CHECKED_IN){
			setPageComplete(false);
			return "Selected source release is " + wiz.getFromRelease().getStatus().getLabel().toLowerCase() + ". The source release of a merge must be checked-in.";
		} else {
			setPageComplete(true);
		}
		return null;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		((MergeWizard)this.getWizard()).setFromRelease((IVersionInfo)versionTreeGUI.getModel());
		super.handleEvent(event, source, data);
	}
//	/**
//	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
//	 */
//	@Override
//	public boolean isPageComplete() {
//		return (versionTreeGUI.getModel()!=null);
//	}

}
