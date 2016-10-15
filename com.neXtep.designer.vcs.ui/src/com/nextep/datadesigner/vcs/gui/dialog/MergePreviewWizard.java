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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.gef.VersionTreeDiagram;
import com.nextep.designer.vcs.gef.VersionTreeGUI;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 *
 */
public class MergePreviewWizard extends WizardDisplayConnector {


	private Composite contents = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Composite mergePreview = null;
	private VersionTreeGUI versionTree;
	private Text previewText = null;
	private Label settingsLabel = null;
	private Button autoCheckoutCheck = null;
	private Button autoMergeCheck = null;
	private IVersionable<?> targetVersionable;
	private IVersionInfo mergedRelease;
	/**
	 * @param name
	 * @param title
	 * @param image
	 */
	public MergePreviewWizard(IVersionable<?> targetVersionable) {
		super("Preview", "Preview of the new merge release...", null);
		this.targetVersionable = targetVersionable;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createSWTControl(Composite parent) {
		GridData gridData4 = new GridData();
		gridData4.horizontalSpan = 2;
		GridData gridData3 = new GridData();
		gridData3.horizontalSpan = 2;
		GridData gridData2 = new GridData();
		gridData2.horizontalSpan = 2;
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.CENTER;
		gridData1.verticalAlignment = GridData.BEGINNING;
		contents = new Composite(parent,SWT.NONE);
		createMergePreview();
		addNoMarginLayout(contents, 2, false);
		contents.setSize(new Point(421, 200));
		previewText = new Text(contents, SWT.MULTI | SWT.WRAP);
		previewText.setEditable(false);
		previewText.setFont(FontFactory.FONT_ITALIC);
		previewText.setLayoutData(gridData1);


//		settingsLabel = new Label(contents, SWT.NONE);
//		settingsLabel.setText("Merge settings");
//		settingsLabel.setFont(FontFactory.FONT_BOLD);
//		settingsLabel.setLayoutData(gridData2);
//		autoCheckoutCheck = new Button(contents, SWT.CHECK);
//		autoCheckoutCheck.setText("Automatically checkout elements");
//		autoCheckoutCheck.setLayoutData(gridData3);
//		autoMergeCheck = new Button(contents, SWT.CHECK);
//		autoMergeCheck.setText("Merge automatically when possible");
//		autoMergeCheck.setLayoutData(gridData4);
		return contents;
	}

	/**
	 * This method initializes mergePreview
	 *
	 */
	private void createMergePreview() {
		Composite container = new Composite(contents, SWT.BORDER);
		addNoMarginLayout(container, 1);
		container.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;

		//Initializing version tree
		versionTree = new VersionTreeGUI(targetVersionable,SWT.VERTICAL,false,buildVersionTree());

		//Creating control
		mergePreview = (Composite)versionTree.create(container);

		mergePreview.setLayout(new GridLayout());
		mergePreview.setLayoutData(gridData);
	}

	private List<IVersionInfo> buildVersionTree() {
		//Building version tree for merge preview
		MergeWizard wiz = (MergeWizard)getWizard();
		List<IVersionInfo> versionsList = new ArrayList<IVersionInfo>();
		versionsList.add(wiz.getToRelease());
		versionsList.add(wiz.getFromRelease());
		mergedRelease = VersionFactory.buildNextVersionInfo(wiz.getToRelease(),null);
		versionsList.add(mergedRelease);
		return versionsList;
	}
	private void addMergePreviewLine() {
		MergeWizard wiz = (MergeWizard)getWizard();
		VersionTreeDiagram d = (VersionTreeDiagram)versionTree.getModel();
		d.addVersionSuccessor(wiz.getFromRelease(), mergedRelease);
		versionTree.getGraphicalViewer().setContents(d);
		versionTree.refreshConnector();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		if(versionTree != null) {
			versionTree.setVersionTree(buildVersionTree());
			versionTree.zoomFit();
			addMergePreviewLine();
			MergeWizard wiz = (MergeWizard)getWizard();
			previewText.setText("New release will be created on branch\n" + wiz.getToRelease().getBranch().getName() + " from releases " + wiz.getFromRelease().getLabel() + " and " + wiz.getToRelease().getLabel() +".");
		}
	}


	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getModel()
	 */
	@Override
	public Object getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return contents;
	}

}
