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

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDesignerGUI;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.vcs.gui.external.VersionablePaintItemListener;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.navigators.DiffLineNavigator;

/**
 * @author Christophe Fondacci
 *
 */
public class VersionDiffViewer implements IDesignerGUI, SelectionListener {
	private static Log log = LogFactory.getLog(VersionDiffViewer.class);
	private Shell sShell = null;
	private Tree sourceTree = null;
	private TreeColumn srcColumn;
	private TreeColumn tgtColumn;
	private TreeColumn typeColumn;
	//private Tree targetTree = null;
	private CLabel sourceLabel = null;
	private Text sourceText = null;
	private CLabel targetLabel = null;
	private Text targetText = null;
	private Button diffOnlyButton = null;
	private IComparisonItem[] result;
	boolean diffOnlyFlag;

	public VersionDiffViewer(IComparisonItem... result) {
		this.result=result;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#getDisplay()
	 */
	@Override
	public Display getDisplay() {
		return sShell.getDisplay();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#getShell()
	 */
	@Override
	public Shell getShell() {
		return sShell;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#initializeGUI(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	public void initializeGUI(Shell parentGUI) {
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.verticalAlignment = GridData.FILL;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan=2;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		sShell = new Shell(parentGUI,SWT.APPLICATION_MODAL | SWT.SHELL_TRIM);
		sShell.setText("Version comparison...");
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(600, 350));
		sourceLabel = new CLabel(sShell, SWT.NONE);
		sourceLabel.setText("Source release : ");
		sourceText = new Text(sShell,SWT.NONE);
		sourceText.setEditable(false);
		targetLabel = new CLabel(sShell, SWT.NONE);
		targetLabel.setText("Target release");
		targetText = new Text(sShell,SWT.NONE);
		targetText.setEditable(false);
		diffOnlyButton = new Button(sShell,SWT.PUSH);
		GridData data = new GridData();
		data.horizontalSpan=2;
		diffOnlyButton.setLayoutData(data);
		diffOnlyButton.addSelectionListener(this);

		diffOnlyFlag = true;
		sourceTree = new Tree(sShell, SWT.BORDER | SWT.FULL_SELECTION);
		sourceTree.setLayoutData(gridData);
		VersionablePaintItemListener.handle(sourceTree, true, false, true);
		srcColumn = new TreeColumn(sourceTree,SWT.NONE);
		srcColumn.setText("Source Item");
		srcColumn.setWidth(300);
		typeColumn = new TreeColumn(sourceTree,SWT.NONE);
		typeColumn.setText("Item type");
		typeColumn.setWidth(100);
		tgtColumn = new TreeColumn(sourceTree,SWT.NONE);
		tgtColumn.setText("Target Item");
		tgtColumn.setWidth(200);
		sourceTree.setHeaderVisible(true);
		refreshGUI();
	}
	private void refreshGUI() {
		if(result.length==1) {
			IComparisonItem soloResult = result[0];
			// Setting source & target release in a null safe way
			if(soloResult.getSource() instanceof IVersionable) {
				IVersionable<?> srcVersion = (IVersionable<?>)soloResult.getSource();
				sourceText.setText(srcVersion.getVersion().getLabel() + " - " +srcVersion.getVersion().getStatus().getLabel());
			}
			if(soloResult.getTarget() instanceof IVersionable) {
				IVersionable<?> tgtVersion =(IVersionable<?>)soloResult.getTarget();
				targetText.setText(tgtVersion.getVersion().getLabel() + " - " + tgtVersion.getVersion().getStatus().getLabel());
			}
		} else {
			sourceText.setText("Comparing a set of source items");
			targetText.setText("Comparing a set of target items");
		}
		// Setting proper button action
		if(diffOnlyFlag) {
			diffOnlyButton.setText("Show all hierarchy");
		} else {
			diffOnlyButton.setText("Show differences only");
		}
		sourceTree.removeAll();
		TreeItem srcRoot = new TreeItem(sourceTree,SWT.NONE);
		srcRoot.setText("Comparison results..."); //result.getSource().toString());
		srcRoot.setImage(VCSImages.ICON_DIFF);
		//generateViewer(result,srcRoot);
		ProgressMonitorDialog pd = new ProgressMonitorDialog(this.getShell());
		try {
			pd.run(true, false, new GenerationProgress(srcRoot,result));
		} catch( Exception e) {
			throw new ErrorException(e);
		}
		srcRoot.setExpanded(true);

	}
	private class GenerationProgress implements IRunnableWithProgress {
		private IComparisonItem[] result;
		private IComparisonItem currentItem;
		private TreeItem rootItem;
		private IProgressMonitor monitor;
		public GenerationProgress(TreeItem rootItem,IComparisonItem... result) {
			this.result=result;
			this.rootItem=rootItem;
		}
		/**
		 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			this.monitor=monitor;
			// Initializing by counting the work to do
			monitor.setTaskName("Initializing...");
			int tasks = 0;
			for(IComparisonItem item : result) {
				tasks += item.getSubItems().size();
			}
			// Comparison
			monitor.beginTask("Parsing comparison results...", tasks);
			for(final IComparisonItem item : result) {
				currentItem = item;
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						generateViewer(item,rootItem);
					}	
				});
				monitor.worked(1);
			}
			monitor.done();
//			while(rootItem.getParent().getShell().getDisplay().readAndDispatch());
		}
		private void generateViewer(IComparisonItem compItem, TreeItem treeItem) {
//			monitor.setTaskName("Generating " + treeItem.getText() + " differency contents...");
//			while(treeItem.getParent().getShell().getDisplay().readAndDispatch());

			//log.debug("Generating difference for " + compItem.toString());
			if(diffOnlyFlag && compItem.getDifferenceType() == DifferenceType.EQUALS) {
				worked(compItem);
				return;
			}
			INavigatorConnector c = new DiffLineNavigator(compItem);
			c.create(treeItem, -1);
			c.refreshConnector();
//			Thread.sleep(100);
			for(IComparisonItem item : compItem.getSubItems()) {
				generateViewer(item, c.getSWTConnector());
			}
			worked(compItem);
			//Expanding differences
			if(compItem.getDifferenceType()==DifferenceType.DIFFER) {
				c.getSWTConnector().setExpanded(true);
			}
		}
		private void worked(IComparisonItem compItem) {
			if(compItem == currentItem) {
				monitor.worked(1);
//				while(rootItem.getParent().getShell().getDisplay().readAndDispatch());
			}
		}
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
		diffOnlyFlag = !diffOnlyFlag;
		refreshGUI();
	}


}
