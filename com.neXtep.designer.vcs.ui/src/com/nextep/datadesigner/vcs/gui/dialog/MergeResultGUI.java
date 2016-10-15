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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.gui.external.VersionablePaintItemListener;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.MergeStatus;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * A display connector featuring the results of a merge operation.
 * This display is used for user validation and ambiguity resolving
 * before applying the merge.<br>
 * <br>
 * This display connector can be used as a wizard page.
 *
 * @author Christophe Fondacci
 *
 */
public class MergeResultGUI extends WizardDisplayConnector implements TreeListener, SelectionListener, MouseListener, MouseWheelListener {

	private static final int ADDITIONS = 0;
	private static final int UPDATES = 1;
	private static final int DELETIONS = 2;
	private static final int UNCHANGED = 3;
	private static final String[] msgKeys = {"comparison.additionNode","comparison.updateNode","comparison.removalNode","comparison.unchangedNode"};
	
	private static final Log log = LogFactory.getLog(MergeResultGUI.class);
	private Composite 	gui			= null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Tree 		sourceTree 	= null;
	private Tree 		targetTree 	= null;
	private Tree 		mergedTree 	= null;
	private TreeItem[] 	sourceRoot	= new TreeItem[4];
	private TreeItem[] 	targetRoot 	= new TreeItem[4];
	private TreeItem[] 	mergedRoot 	= new TreeItem[4];
	private Button		hideButton  = null;
	private boolean		hideUnchanged=true;
	private SashForm	sash 		= null;
//	private Label 		sourceLabel = null;
//	private Label 		targetLabel = null;
//	private Label 		mergedLabel = null;

	private IComparisonItem[] result;
	private MergeNavigator sourceConn;
	private boolean showMergeColumn = false;
	private boolean repositoryMergeWindow = false;
	private boolean noSort = false;
	private String sourceRootText, targetRootText, mergedRootText;
	/** A root item check state saver to determine user selection / deselection*/
	private boolean[] rootChecked = new boolean[] {false,false,false,false};

	public MergeResultGUI(boolean showMergeColumn, IComparisonItem... result ) {
		super("Result","Merge results",null);
		this.result=result;
		this.showMergeColumn = showMergeColumn;
		setRootText("","","");
		noSort = false;
	}
	public void setIsRepositoryMerge(boolean repositoryMergeWindow) {
		this.repositoryMergeWindow = repositoryMergeWindow;
	}
	public void setNoSort(boolean noSort) {
		this.noSort = noSort;
	}
	public void setRootText(String sourceText, String targetText, String mergedText) {
		sourceRootText = sourceText;
		targetRootText = targetText;
		mergedRootText = mergedText;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createSWTControl(Composite parent) {
		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.verticalAlignment = GridData.FILL;
		gridData2.grabExcessVerticalSpace = true;
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.heightHint=400;
		GridData gridData1 = new GridData();
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.verticalAlignment = GridData.FILL;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.heightHint=400;
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.heightHint=400;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		if(showMergeColumn) {
			gridLayout.makeColumnsEqualWidth = true;
		}
		gui = new Composite(parent,SWT.NONE);
		addNoMarginLayout(gui,3);
		//sShell.setText("Shell");
//		gui.setLayout(gridLayout);
		//sShell.setSize(new Point(709, 293));
//		sourceLabel = new Label(gui, SWT.NONE);
//		sourceLabel.setText("Source release X.X.X.X on Branch Status");
//		targetLabel = new Label(gui, SWT.NONE);
//		targetLabel.setText("Target release X.X.X.X Status");
//		mergedLabel = new Label(gui, SWT.NONE);
//		mergedLabel.setText("Merge result release X.X.X.X Status");
		hideButton = new Button(gui,SWT.PUSH);
		hideButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,3,1));
		hideButton.setText( (hideUnchanged ? "Show" : "Hide") + " unchanged items");
		hideButton.addSelectionListener(this);
		sash = new SashForm(gui,SWT.NONE);
		sash.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		//Building trees
		Composite src = new Composite(sash,SWT.NONE);
		addNoMarginLayout(src, 1);
		Label srcLabel = new Label(src,SWT.NONE);
		srcLabel.setText(sourceRootText);
		srcLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		sourceTree = new Tree(src, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER);
		sourceTree.setLayoutData(gridData2);
		VersionablePaintItemListener.handle(sourceTree,false, false, true);

		Composite tgt = new Composite(sash,SWT.NONE);
		addNoMarginLayout(tgt, 1);
		Label tgtLabel = new Label(tgt,SWT.NONE);
		tgtLabel.setText(targetRootText);
		tgtLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		targetTree = new Tree(tgt, (repositoryMergeWindow ? SWT.CHECK : SWT.NONE) | SWT.FULL_SELECTION | SWT.BORDER);
		targetTree.setLayoutData(gridData1);
		VersionablePaintItemListener.handle(targetTree, false, false, true);
		
		Composite mrg = new Composite(sash,SWT.NONE);
		addNoMarginLayout(mrg,1);
		Label mrgLabel = new Label(mrg,SWT.NONE);
		mrgLabel.setText(mergedRootText);
		mrgLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		mergedTree = new Tree(mrg, (repositoryMergeWindow ? SWT.CHECK : SWT.NONE) | SWT.FULL_SELECTION | SWT.BORDER);
//		mergedTree.setVisible(showMergeColumn);
		mergedTree.setLayoutData(gridData);
		VersionablePaintItemListener.handle(mergedTree, false, false, true);
		
		// Hiding or showing merge column via sash form weights
		if(showMergeColumn) {
			sash.setWeights(new int[] {1,1,1});
		} else {
			sash.setWeights(new int[] {1,1,0});
		}
		
		// Initializing arrays for 3 nodes
		String[] nodeText = new String[] { VCSUIMessages.getString("comparison.additionNode"),
				VCSUIMessages.getString("comparison.updateNode"),
				VCSUIMessages.getString("comparison.removalNode"),
				VCSUIMessages.getString("comparison.unchangedNode") };
		Image[] nodeImages = new Image[] {VCSImages.ICON_DIFF_ADDED,VCSImages.ICON_DIFF_CHANGED,VCSImages.ICON_DIFF_REMOVED,null};
		// Initializing root trees
		for(int i=0 ; i < 4 ; i++) {
			sourceRoot[i] = new TreeItem(sourceTree,NONE);
			targetRoot[i] = new TreeItem(targetTree,NONE);
			mergedRoot[i] = new TreeItem(mergedTree,NONE);
			sourceRoot[i].setImage(nodeImages[i]);
			targetRoot[i].setImage(nodeImages[i]);
			mergedRoot[i].setImage(nodeImages[i]);
			sourceRoot[i].setText(nodeText[i]);
			targetRoot[i].setText(nodeText[i]);
			mergedRoot[i].setText(nodeText[i]);
		}



		createMergeNavigators();

//		sourceRoot.setExpanded(true);
//		targetRoot.setExpanded(true);
//		mergedRoot.setExpanded(true);

		for(int i=0;i<3;i++) {
			expandTreeItems(sourceRoot[i]);
		}
		//Adding tree listener
		sourceTree.addTreeListener(this);
		targetTree.addTreeListener(this);
		mergedTree.addTreeListener(this);
		sourceTree.addSelectionListener(this);
		targetTree.addSelectionListener(this);
		mergedTree.addSelectionListener(this);
		sourceTree.addMouseListener(this);
		targetTree.addMouseListener(this);
		mergedTree.addMouseListener(this);
		
		sourceTree.addMouseWheelListener(this);
		targetTree.addMouseWheelListener(this);
		mergedTree.addMouseWheelListener(this);

		sourceTree.getVerticalBar().addSelectionListener(this);
		targetTree.getVerticalBar().addSelectionListener(this);
		mergedTree.getVerticalBar().addSelectionListener(this);
//		mergedTree.setVisible(showMergeColumn);
		gui.layout();

		return gui;
	}
	
	private void expandTreeItems(TreeItem root) {
		for(TreeItem i : root.getItems()) {
			final MergeNavigator m = (MergeNavigator)i.getData();
			final IComparisonItem item = (IComparisonItem)m.getModel();
			if(item.getMergeInfo().getStatus()==MergeStatus.MERGE_RESOLVED && item.getMergeInfo().getMergeProposal()!=item.getSource() && item.getMergeInfo().getMergeProposal()!=item.getTarget()) {
				m.getSourceItem().setExpanded(true);
				m.getTargetItem().setExpanded(true);
				m.getMergedItem().setExpanded(true);
				expandTreeItems(i);
			}
		}
	}

	private void createMergeNavigators() {
		// Sorting results by source name / target name
		final List<IComparisonItem> sortedResult = Arrays.asList(result);
		// Sorting items unless told not to do so
		if(!noSort) {
			Collections.sort(sortedResult,new Comparator<IComparisonItem>() {
	
				@Override
				public int compare(IComparisonItem o1, IComparisonItem o2) {
					IReferenceable src = o1.getSource() == null ? o1.getTarget() : o1.getSource();
					IReferenceable tgt = o2.getSource() == null ? o2.getTarget() : o2.getSource();
					return ((INamedObject)src).getName().compareTo(((INamedObject)tgt).getName());
				}
				
			});
		}
		// Creating merge lines items
		ProgressMonitorDialog pd = new ProgressMonitorDialog(this.getShell());
		try {
			pd.run(false, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.setTaskName("Initializing...");
					monitor.beginTask("Preparing preview for user validation...",result.length);
					for(IComparisonItem content : sortedResult) {
//						if(!hideUnchanged || content.getDifferenceType()!=DifferenceType.EQUALS) {
							int type = UPDATES;
							switch(content.getDifferenceType()) {
							case EQUALS:
								if(hideUnchanged) {
									continue;
								}
								type=UNCHANGED;
								break;
							case DIFFER:
								type=UPDATES;
								break;
							case MISSING_SOURCE:
								type=DELETIONS;
								break;
							case MISSING_TARGET:
								type=ADDITIONS;
								break;
							}
							sourceConn = new MergeNavigator(sourceRoot[type],targetRoot[type],mergedRoot[type],content);
							sourceConn.setShowAllChecks(repositoryMergeWindow);
							if(content.getDifferenceType()!=DifferenceType.EQUALS) {
								sourceConn.showUnchangedItems(!hideUnchanged);
							} else {
								sourceConn.showUnchangedItems(true);
							}
							sourceConn.create(sourceRoot[type], -1);
							sourceConn.initialize();
							sourceConn.refreshConnector(false);
							monitor.worked(1);
						}
//					}
					for(int i=0 ; i<4 ; i++) {
						final int count = sourceRoot[i].getItemCount();
						if(count>0) {
							sourceRoot[i].setText(VCSUIMessages.getString(msgKeys[i]) + " (" + count + ")");
							sourceRoot[i].setFont(FontFactory.FONT_BOLD);
							// Unchecking everything to ensure reprocessing 
							sourceRoot[i].setChecked(false);
							targetRoot[i].setChecked(false);
							mergedRoot[i].setChecked(false);
							// Processing gray checks
							gray(sourceRoot[i]);
							gray(targetRoot[i]);
							gray(mergedRoot[i]);
						} else {
							sourceRoot[i].setText(VCSUIMessages.getString(msgKeys[i]));
							sourceRoot[i].setFont(null);
						}
					}
					monitor.done();
				}
				
			});
		} catch( Exception e) {
			throw new ErrorException(e);
		}
	}
	/**
	 * This method recursively grays the check box of the treeitem
	 * when at least one child is selected.
	 * 
	 * @param item TreeItem to gray
	 * @return a boolean indicating when the specified item is grayed or fully selected 
	 */
	private boolean gray(TreeItem item) {
		if(item.getChecked()) return true;
		boolean childSelected = false;
		for(TreeItem child : item.getItems()) {
			if(gray(child)) {
				item.setGrayed(true);
				item.setChecked(true);
				childSelected = true;
			}
		}
		return childSelected;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getModel()
	 */
	@Override
	public Object getModel() {
		return result;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return gui;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		if(sourceConn != null) {
			sourceConn.refreshConnector();
		}
	}
	/**
	 * @see org.eclipse.swt.events.TreeListener#treeCollapsed(org.eclipse.swt.events.TreeEvent)
	 */
	@Override
	public void treeCollapsed(TreeEvent e) {
		itemAction(e,false);
	}
	/**
	 * @see org.eclipse.swt.events.TreeListener#treeExpanded(org.eclipse.swt.events.TreeEvent)
	 */
	@Override
	public void treeExpanded(TreeEvent e) {
		itemAction(e,true);
	}

	private void itemAction(TreeEvent e, boolean expanded) {
		TreeItem i = (TreeItem)e.item;
		if(i!= null && i.getData() instanceof MergeNavigator) {
			MergeNavigator nav = (MergeNavigator)i.getData();
			nav.getSourceItem().setExpanded(expanded);
			nav.getTargetItem().setExpanded(expanded);
			nav.getMergedItem().setExpanded(expanded);
		} else {
			for(int j=0 ; j<4 ; j++) {
				if(sourceRoot[j]==i || targetRoot[j]==i || mergedRoot[j]==i) {
					sourceRoot[j].setExpanded(expanded);
					targetRoot[j].setExpanded(expanded);
					mergedRoot[j].setExpanded(expanded);					
				}
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
		if(e.getSource() instanceof ScrollBar) {
			final Object source = e.getSource();
			MergeNavigator nav = null;
			if(source == sourceTree.getVerticalBar()) {
				nav = (MergeNavigator)sourceTree.getTopItem().getData();
			} else if(source == targetTree.getVerticalBar()) {
				nav = (MergeNavigator)targetTree.getTopItem().getData();
			} else if(source == mergedTree.getVerticalBar()) {
				nav = (MergeNavigator)mergedTree.getTopItem().getData();
			}
			if(nav != null) {
				sourceTree.setTopItem(nav.getSourceItem());
				targetTree.setTopItem(nav.getTargetItem());
				mergedTree.setTopItem(nav.getMergedItem());
			} else {
				for(int i=0; i<4 ; i++) {
					if(source==sourceTree.getVerticalBar() && sourceRoot[i]==sourceTree.getTopItem()) {
						targetTree.setTopItem(targetRoot[i]);
						mergedTree.setTopItem(mergedRoot[i]);
					} else if(source==targetTree.getVerticalBar() && targetRoot[i]==targetTree.getTopItem()) {
						sourceTree.setTopItem(sourceRoot[i]);
						mergedTree.setTopItem(mergedRoot[i]);
					} else if(source==mergedTree.getVerticalBar() && mergedRoot[i]==mergedTree.getTopItem()) {
						sourceTree.setTopItem(sourceRoot[i]);
						targetTree.setTopItem(targetRoot[i]);
					}
				}
//				sourceTree.setTopItem(sourceTree.getItem(0));
//				targetTree.setTopItem(targetTree.getItem(0));
//				mergedTree.setTopItem(mergedTree.getItem(0));
			}
		} else if( e.getSource() == hideButton) {
			hideUnchanged = !hideUnchanged;
			for(int i=0 ; i<4 ; i++) {
				sourceRoot[i].removeAll();
				targetRoot[i].removeAll();
				mergedRoot[i].removeAll();
			}
			createMergeNavigators();
//			sourceRoot.setExpanded(true);
//			targetRoot.setExpanded(true);
//			mergedRoot.setExpanded(true);
			for(int i=0 ; i<4 ; i++) {
				expandTreeItems(sourceRoot[i]);
			}
			hideButton.setText( (hideUnchanged ? "Show" : "Hide") + " unchanged items");
		} else if(e.item != null && e.item.getData() instanceof MergeNavigator) {
			MergeNavigator nav = (MergeNavigator)e.item.getData();
			sourceTree.setSelection(nav.getSourceItem());
			targetTree.setSelection(nav.getTargetItem());
			mergedTree.setSelection(nav.getMergedItem());
			if(e.detail==SWT.CHECK) {
				final TreeItem item = nav.getSourceItem();
				nav.handleEvent(ChangeEvent.SELECTION_CHANGED, null, (!item.getChecked() || !item.getGrayed()));
			}
		} else {
			if(e.detail==SWT.CHECK) {
				for(int j=0 ; j<4 ; j++) {
					if(e.item == sourceRoot[j] || e.item == targetRoot[j] || e.item == mergedRoot[j]) {
						// If a root is selected, selecting all child items
						final TreeItem item = (TreeItem)e.item;
						boolean checked = item.getChecked();
						item.setGrayed(false);
//						if(rootChecked[j]!=checked) {
							for(TreeItem child : ((TreeItem)e.item).getItems()) {
								child.setChecked(checked);
								if(child.getData() instanceof MergeNavigator) {
									MergeNavigator nav = (MergeNavigator)child.getData();
//									sourceTree.setSelection(nav.getSourceItem());
//									targetTree.setSelection(nav.getTargetItem());
//									mergedTree.setSelection(nav.getMergedItem());
									nav.handleEvent(ChangeEvent.SELECTION_CHANGED, null, !checked);
//									nav.selectProposal((IComparisonItem)nav.getModel(), checked ? Proposal.SOURCE : Proposal.TARGET, true);
//									nav.fullRefresh();
								}
							}
//							rootChecked[j]=checked;
//						}
					}
				}
			}
		}
	}
	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
//		// We arbitrarily take the source item to retrieve the merge
//		// navigator which contains the comparison item as item data
//		TreeItem[] sel = sourceTree.getSelection();
//		if(sel.length>0) {
//			if(sel[0].getData()==null) return;
//			final INavigatorConnector conn = ((INavigatorConnector)sel[0].getData()); 
//			IComparisonItem comp = (IComparisonItem)conn.getModel();
//			MergeResultGUI mergeGUI = new MergeResultGUI(true,comp.getSubItems().toArray(new IComparisonItem[comp.getSubItems().size()]));
//			mergeGUI.setIsRepositoryMerge(true); //NoSort(true);
//			mergeGUI.setRootText(
//					"Source",
//					"Target",
//					"Merge preview");
//			try {
//				mergeGUI.setNoSort(true);
//				new GUIWrapper(mergeGUI,"Merge results",800,600).invoke();
//				conn.refreshConnector();
//			} catch(CancelException ce) {
//				log.debug(ce);
//			}
//		}
		
	}
	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent e) {}
	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseUp(MouseEvent e) {}
	@Override
	public void mouseScrolled(MouseEvent e) {
		MergeNavigator nav = null;
		if(e.widget == sourceTree) {
			nav = (MergeNavigator)sourceTree.getTopItem().getData();
		} else if(e.widget == targetTree) {
			nav = (MergeNavigator)targetTree.getTopItem().getData();
		} else if(e.widget == mergedTree) {
			nav = (MergeNavigator)mergedTree.getTopItem().getData();
		}
		if(nav != null) {
			sourceTree.setTopItem(nav.getSourceItem());
			targetTree.setTopItem(nav.getTargetItem());
			mergedTree.setTopItem(nav.getMergedItem());
		} else {
			Widget source = e.widget;
			for(int i=0; i<4 ; i++) {
				if(source==sourceTree && sourceRoot[i]==sourceTree.getTopItem()) {
					targetTree.setTopItem(targetRoot[i]);
					mergedTree.setTopItem(mergedRoot[i]);
				} else if(source==targetTree && targetRoot[i]==targetTree.getTopItem()) {
					sourceTree.setTopItem(sourceRoot[i]);
					mergedTree.setTopItem(mergedRoot[i]);
				} else if(source==mergedTree && mergedRoot[i]==mergedTree.getTopItem()) {
					sourceTree.setTopItem(sourceRoot[i]);
					targetTree.setTopItem(targetRoot[i]);
				}
			}
		}
	}
}
