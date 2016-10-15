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

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.MergeInfo;
import com.nextep.designer.vcs.model.MergeStatus;
import com.nextep.designer.vcs.ui.VCSImages;

/**
 * @author Christophe Fondacci
 *
 */
public class MergeNavigator extends UntypedNavigator {
	private static final Log log = LogFactory.getLog(MergeNavigator.class);
	private boolean showAllChecks = true;
    private IComparisonItem compItem;
    private TreeItem sourceItem;
    private TreeItem targetItem;
    private TreeItem mergedItem;
    private TreeItem parentSourceItem;
    private TreeItem parentTargetItem;
    private TreeItem parentMergedItem;
    private INavigatorConnector sourceConn;
    private INavigatorConnector targetConn;
    private INavigatorConnector mergedConn;
    private MergeNavigator parentNav;

    // Check status backup
    private boolean sourceChecked = false;
    private boolean targetChecked = false;
    private boolean mergedChecked = false;
    
    private boolean showUnchanged=true;
    //Workaroung debug bug
//    private Merger merger;
    public static enum Proposal { SOURCE,TARGET,MERGE; }
    public MergeNavigator(TreeItem source, TreeItem target, TreeItem merged, IComparisonItem i) {
        super(null,null);
        parentSourceItem=source;
        parentTargetItem=target;
        parentMergedItem=merged;
        this.compItem = i;
        init();
    }
    public MergeNavigator(MergeNavigator parent, IComparisonItem i) {
        super(null,null);
        this.compItem = i;
        this.parentNav=parent;
        init();
    }
    private void init() {
//        merger = (Merger)MergerFactory.getMerger(compItem.getType());
        // Setting model from type to display
        sourceConn = initConnector(compItem.getSource());
        targetConn = initConnector(compItem.getTarget());
        mergedConn = initConnector(compItem.getMergeInfo().getMergeProposal());
    }
    public void showUnchangedItems(boolean showUnchanged) {
    	this.showUnchanged = showUnchanged;
    }
    /**
     * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
     */
    @Override
    public void setModel(Object model) {
        // TODO Auto-generated method stub

    }


    /**
     * @see com.nextep.datadesigner.gui.model.INavigatorConnector#createSWTConnector(org.eclipse.swt.widgets.TreeItem, int)
     */
    @Override
    protected TreeItem createSWTConnector(TreeItem parent, int treeIndex) {
        //Initializing 3 mirrored tree items
        if(parentNav != null) {
            parentSourceItem = parentNav.getSourceItem();
            parentTargetItem = parentNav.getTargetItem();
            parentMergedItem = parentNav.getMergedItem();
        }
        sourceItem = sourceConn.create(parentSourceItem, treeIndex);
        targetItem = targetConn.create(parentTargetItem, treeIndex);
        mergedItem = mergedConn.create(parentMergedItem, treeIndex);
        // Everything is grayed (refreshConnector) will enlight
//        sourceItem.setGrayed(true);
//        targetItem.setGrayed(true);
//        mergedItem.setGrayed(true);
        sourceItem.setData(this);
        targetItem.setData(this);
        mergedItem.setData(this);
        sourceItem.setExpanded(true);
        targetItem.setExpanded(true);
        mergedItem.setExpanded(true);
        // Ensuring mirrored TreeItem disposal on source item disposal
        final TreeItem linkedTargetItem = targetItem;
        final TreeItem linkedMergedItem = mergedItem;
        sourceItem.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if(linkedTargetItem!=null && !linkedTargetItem.isDisposed()) {
                	linkedTargetItem.dispose();
                }
                if(linkedMergedItem!=null && !linkedMergedItem.isDisposed()) {
                	linkedMergedItem.dispose();
                }
            }
        });
        return sourceItem;
    }
    /**
     * @see com.nextep.datadesigner.gui.model.AbstractNavigator#sortConnectors(java.util.List)
     */
    @Override
    protected void sortConnectors(List<INavigatorConnector> conn) {
        // No sorting
    }
    /**
     * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getType()
     */
    @Override
    public IElementType getType() {
        return compItem.getType();
    }

    /**
     * @see com.nextep.datadesigner.gui.model.IConnector#getModel()
     */
    @Override
    public Object getModel() {
        return compItem;
    }

    /**
     * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
     */
    @Override
    public TreeItem getSWTConnector() {
        return sourceItem;
    }
    /**
     * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
     */
    @Override
    public Image getConnectorIcon() {
        // a blank icon since it will be generated in the refresh
        return ImageFactory.ICON_BLANK;
    }
    /**
     * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
     */
    @Override
    public String getTitle() {
        return sourceConn.getTitle();
    }

    /**
     * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
     */
    @Override
    public void refreshConnector() {
    	refreshConnector(true);
    }
    /**
     * Handling graying or not depending on the caller
     * @param grayParents a boolean to ask for handling parents gray state or not
     */
    protected void refreshConnector(boolean grayParents) {
        if(compItem.getSource() != null) {
        	final String srcTitle = sourceConn.getTitle();
        	final int newlineIndex = srcTitle.indexOf('\n');
            sourceItem.setText(srcTitle.substring(0, newlineIndex == -1 ? srcTitle.length() : newlineIndex));
        } else {
            sourceItem.setText("<Removed>");
            sourceItem.setImage(VCSImages.ICON_DIFF_REMOVED_SMALL);
        }
        if(compItem.getTarget() != null) {
        	final String tgtTitle =targetConn.getTitle();
        	final int newlineIndex=tgtTitle.indexOf('\n');
            targetItem.setText(tgtTitle.substring(0, newlineIndex == -1 ? tgtTitle.length() : newlineIndex));
        } else {
            targetItem.setText("<Removed>");
            targetItem.setImage(VCSImages.ICON_DIFF_REMOVED_SMALL);
        }
        final String mgdTitle =mergedConn.getTitle();
    	final int newlineIndex=mgdTitle.indexOf('\n');
        mergedItem.setText(mgdTitle.substring(0, newlineIndex == -1 ? mgdTitle.length() : newlineIndex));
        MergeInfo info =compItem.getMergeInfo();
        // Handling check status
        sourceItem.setChecked(false);
        targetItem.setChecked(false);
        mergedItem.setChecked(false);
        sourceChecked = false;
        targetChecked = false;
        mergedChecked = false;
        // Re-initializing item font / colors
        sourceItem.setFont(Display.getCurrent().getSystemFont());
        sourceItem.setForeground(FontFactory.BLACK);
        targetItem.setFont(Display.getCurrent().getSystemFont());
        targetItem.setForeground(FontFactory.BLACK);
        mergedItem.setFont(Display.getCurrent().getSystemFont());
        mergedItem.setForeground(FontFactory.BLACK);
        mergedItem.setBackground(FontFactory.WHITE);

        // Refreshing
        if( info.getStatus() == MergeStatus.MERGE_RESOLVED && compItem.getMergeInfo().getMergeProposal() == compItem.getTarget()) {
            targetItem.setChecked(true);
            if(!targetItem.getParentItem().getChecked()&& grayParents) {
            	setParentsGrayed(targetItem.getParentItem()); //,true);
            }
            targetChecked = true;
            sourceItem.setFont(FontFactory.FONT_ITALIC);
            sourceItem.setForeground(FontFactory.SHADOW_TEXT_COLOR);
            mergedItem.setImage(targetItem.getImage());
            mergedItem.setText(targetItem.getText());
            // Forcing check status
//            checkAllItems(targetItem, true);
//            checkAllItems(sourceItem,false);
//            checkAllItems(mergedItem,false);
            
//            removeConnectors();
            selectProposal(compItem, Proposal.TARGET, false);
            sourceItem.setExpanded(false);
            targetItem.setExpanded(false);
            mergedItem.setExpanded(false);

        } else if(info.getStatus() == MergeStatus.MERGE_RESOLVED && info.getMergeProposal() == compItem.getSource()) {
            sourceItem.setChecked(true);
            if(!sourceItem.getParentItem().getChecked() && grayParents) {
            	setParentsGrayed(sourceItem.getParentItem()); //,true);
            }
            sourceChecked = true;
            targetItem.setFont(FontFactory.FONT_ITALIC);
            targetItem.setForeground(FontFactory.SHADOW_TEXT_COLOR);
            mergedItem.setImage(sourceItem.getImage());
            // Forcing check status
//            checkAllItems(sourceItem, true);
//            checkAllItems(targetItem,false);
//            checkAllItems(mergedItem,false);
            
//            removeConnectors();
            selectProposal(compItem, Proposal.SOURCE, false);
            sourceItem.setExpanded(false);
            targetItem.setExpanded(false);
            mergedItem.setExpanded(false);
        } else {
            mergedItem.setChecked(true);
            if(!mergedItem.getParentItem().getChecked() && grayParents) {
            	setParentsGrayed(mergedItem.getParentItem()); //,true);
            }
            mergedChecked=true;
            compItem.getMergeInfo().setStatus(Merger.getSubStatus(compItem));
            mergedItem.setText(compItem.getMergeInfo().getStatus().getTitle());
            mergedItem.setFont(FontFactory.FONT_BOLD);
            if(compItem.getMergeInfo().getStatus()!=MergeStatus.MERGE_RESOLVED &&
                    compItem.getSubItems().isEmpty()) {
                mergedItem.setBackground(FontFactory.ERROR_COLOR);
            }

            targetItem.setFont(FontFactory.FONT_ITALIC);
            targetItem.setForeground(FontFactory.SHADOW_TEXT_COLOR);
            sourceItem.setFont(FontFactory.FONT_ITALIC);
            sourceItem.setForeground(FontFactory.SHADOW_TEXT_COLOR);
//            initializeChildConnectors();
            //Expanding item
//            selectProposal(compItem, Proposal.MERGE);
            sourceItem.setExpanded(true);
            targetItem.setExpanded(true);
            mergedItem.setExpanded(true);
        }
        if( compItem.getMergeInfo().getMergeProposal() != null ) {
            mergedConn.setModel(compItem.getMergeInfo().getMergeProposal());
        }
        for(INavigatorConnector c : this.getConnectors()) {
        	if(c instanceof MergeNavigator) {
        		((MergeNavigator) c).showUnchangedItems(showUnchanged);
        		((MergeNavigator) c).refreshConnector(grayParents);
        	} else {
        		c.refreshConnector();
        	}
        }
    }

    /**
     * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
     */
    @Override
    public void handleEvent(ChangeEvent event, IObservable source, Object data) {
//        itemConnector.handleEvent(event, source, data);
        switch(event) {
        case SELECTION_CHANGED:
               MergeStatus initialStatus =compItem.getMergeInfo().getStatus();
               sourceItem.setGrayed(false);
               targetItem.setGrayed(false);
               mergedItem.setGrayed(false);
            if(sourceItem.getChecked() && !sourceChecked) {
                selectProposal(compItem, Proposal.SOURCE, false);
                setParentsGrayed(sourceItem.getParentItem()); //, true);
            } else if( targetItem.getChecked() && !targetChecked) {
                selectProposal(compItem, Proposal.TARGET, false);
                setParentsGrayed(sourceItem.getParentItem()); //, false);
            } else if( mergedItem.getChecked() && !mergedChecked) {
                selectProposal(compItem, Proposal.MERGE, false);
            } else if( (!sourceItem.getChecked() && !targetItem.getChecked() && !mergedItem.getChecked())) {
            	if(showAllChecks && (sourceItem.getItemCount()>0 || targetItem.getItemCount()>0) && compItem.getDifferenceType()==DifferenceType.DIFFER) {
            		// We select the merge proposal only when no check remains after performing the
            		// user action and when the current tree item has children tree items
            		mergedItem.setChecked(true);
            		selectProposal(compItem, Proposal.MERGE, true);
            	} else {
            		// This variable holds the proposal about to be selected
            		final Proposal aboutToSelect = (Boolean)data ? Proposal.TARGET : Proposal.SOURCE;
            		selectProposal(compItem, aboutToSelect, true);
            	}
            } else {
                return;
            }
            //mergedConn replaceConnector(mergedConn,compItem.getMergeInfo().getMergeProposal());
            if( compItem.getMergeInfo().getMergeProposal() != null ) {
                mergedConn.setModel(compItem.getMergeInfo().getMergeProposal());
            } else {
                // If we have a null model we put an arbitrary non null model
                compItem.getMergeInfo().getMergeProposal();
            }

            if(		(initialStatus == MergeStatus.MERGE_RESOLVED && compItem.getMergeInfo().getStatus()!=MergeStatus.MERGE_RESOLVED)
                    ||	(initialStatus != MergeStatus.MERGE_RESOLVED && compItem.getMergeInfo().getStatus()==MergeStatus.MERGE_RESOLVED)) {
                        fullRefresh();
                }
            break;
        }
        refreshConnector(false);
    }
    /**
     * This method selects the specified proposal on a merge navigator. It is in charge of updating
     * the underlying {@link IComparisonItem} model to set the merge proposal to the appropriate
     * {@link Proposal} parameter.<br>
     * If <code>updateChecks</code> parameter is set to <code>true</code>, this method will also
     * take care of updating the checks & gray display of each impacted tree item.
     *  
     * @param item the {@link IComparisonItem} containing the comparison information for this node.
     * @param type the {@link Proposal} which is being selected
     * @param updateChecks a flag indicating whether this call should take care of display updates
     */
    public void selectProposal(IComparisonItem item, Proposal type, boolean updateChecks) {
        IReferenceable proposal=null;
        switch(type) {
        case SOURCE:
            proposal = item.getSource();
            item.getMergeInfo().setMergeProposal(proposal);
            break;
        case TARGET:
            proposal = item.getTarget();
            item.getMergeInfo().setMergeProposal(proposal);
            break;
        case MERGE:
        	if(mergedItem.getChecked()) {
        		item.getMergeInfo().setStatus(Merger.getSubStatus(item));
        		item.getMergeInfo().setMergeProposal(null);
        		return;
        	} else {
        		item.getMergeInfo().restoreMergeProposal();
  				proposal = item.getMergeInfo().getMergeProposal();        		
        	}
            break;
        }
        for(IComparisonItem subItem : item.getSubItems()) {
            selectProposal(subItem, type, false);
        }
        if(updateChecks) {
			switch(type) {
			case TARGET:
				sourceItem.setChecked(false);
				targetItem.setChecked(true);
				mergedItem.setChecked(false);
				setParentsGrayed(sourceItem.getParentItem()); //, false);
				setParentsGrayed(targetItem.getParentItem()); //, true);
				setParentsGrayed(mergedItem.getParentItem()); //, false);
				break;
			case SOURCE:
				sourceItem.setChecked(true);
				targetItem.setChecked(false);
				mergedItem.setChecked(false);
				setParentsGrayed(sourceItem.getParentItem()); //, true);
				setParentsGrayed(targetItem.getParentItem()); //, false);
				setParentsGrayed(mergedItem.getParentItem()); //, false);
				break;
			case MERGE:
				sourceItem.setChecked(false);
				targetItem.setChecked(false);
				mergedItem.setChecked(true);
				setParentsGrayed(sourceItem.getParentItem()); //, false);
				setParentsGrayed(targetItem.getParentItem()); //, false);
				setParentsGrayed(mergedItem.getParentItem()); //, true);
				break;
			}
        }
    }
    public void fullRefresh() {
        //Retrieving the first navigator item (first child of root item)
        TreeItem[] rootItems = sourceItem.getParent().getItems();
        for(TreeItem rootItem : rootItems) {
        	for(TreeItem child : rootItem.getItems()) {
        		if(child.getData() instanceof INavigatorConnector) {
        			((INavigatorConnector)child.getData()).refreshConnector();
        		}
        	}
        }
        
    }
    private IReferenceable getNonNullModel() {
        return compItem.getTarget() != null ? compItem.getTarget() : compItem.getSource() != null ? compItem.getSource() : compItem.getMergeInfo().getMergeProposal();
    }

    private INavigatorConnector initConnector(IReferenceable ref) {
        if(ref == null) {
            ref = getNonNullModel();
//            return new BlankNavigator();
        }
            if(ref instanceof IVersionable<?>) {
                return UIControllerFactory.getController(IElementType.getInstance("VERSIONABLE")).initializeNavigator(ref);
            } else if(ref instanceof IReference ){
                return UIControllerFactory.getController(IElementType.getInstance("REFERENCE")).initializeNavigator(ref);
            } else {
                return UIControllerFactory.getController(compItem.getType()).initializeNavigator(ref);
            }

    }
//    private INavigatorConnector replaceConnector(INavigatorConnector conn, IReferenceable newRef) {
//    	// Locating the item in its parent connector
//    	TreeItem parent = conn.getSWTConnector().getParentItem();
//    	INavigatorConnector parentConn = (INavigatorConnector)parent.getData();
//		int index = parent.indexOf(conn.getSWTConnector());
//		// Clean item removal
//		conn.releaseConnector();
//		conn.getSWTConnector().dispose();
//		parentConn.getConnectors().remove(conn);
//		// Adding item (clean)
//		conn = initConnector(newRef);
//		conn.createSWTConnector(parent, index);
//		//conn.initialize();
//		((INavigatorConnector)parent.getData()).getConnectors().add(index,conn);
//		return conn;
//		// Refresh should be performed by caller
//		//conn.refreshConnector();
//    }
    /**
     * @return the sourceItem
     */
    public TreeItem getSourceItem() {
        return sourceItem;
    }
    /**
     * @return the targetItem
     */
    public TreeItem getTargetItem() {
        return targetItem;
    }
    /**
     * @return the mergedItem
     */
    public TreeItem getMergedItem() {
        return mergedItem;
    }
    public IReferenceable getMergeProposal() {
        return compItem.getMergeInfo().getMergeProposal();
    }

//    private void removeConnectors() {
//        for(INavigatorConnector nav : new ArrayList<INavigatorConnector>(getConnectors())) {
////            nav.releaseConnector();
//            nav.getSWTConnector().dispose();
//        }
//        getConnectors().clear();
//    }
    @Override
	public void initializeChildConnectors() {
        if(getConnectors().isEmpty()) {
            // Registering sub connectors
            for(IComparisonItem subItem : compItem.getSubItems()) {
            	if(showUnchanged || subItem.getDifferenceType()!=DifferenceType.EQUALS) {
            		MergeNavigator n = new MergeNavigator(this,subItem);
            		n.setShowAllChecks(showAllChecks);
            		n.showUnchangedItems(showUnchanged);
            		addConnector(n);
            	}
            }
        }
	}
//    /**
//     * @see com.nextep.datadesigner.gui.model.AbstractNavigator#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
//     */
//    @Override
//    public void widgetDisposed(DisposeEvent event) {
//       	if(targetConn != null && !targetConn.getSWTConnector().isDisposed()) {
//       		targetConn.getSWTConnector().dispose();
//       	} else {
//       		log.debug("Undisposed target connector");
//       	}
//       	if(mergedConn != null && !mergedConn.getSWTConnector().isDisposed()) {
//       		mergedConn.getSWTConnector().dispose();
//       	} else {
//       		log.debug("Undisposed merged connector");
//       	}
//       	super.widgetDisposed(event);
//    }
//    /**
//     * @see com.nextep.datadesigner.gui.impl.ListeningConnector#releaseConnector()
//     */
//    @Override
//    public void releaseConnector() {
//        super.releaseConnector();
//        //LogFactory.getLog(MergeNavigator.class).debug("Releasing <" + compItem + ">");
//
//        if(sourceConn!= null) { //.getSWTConnector()==null) {
//            sourceConn.releaseConnector();
//        }
//        if(targetConn!=null) { //.getSWTConnector()==null ) {
//            targetConn.releaseConnector();
//        }
//        if(mergedConn!=null) { //.getSWTConnector()==null) {
//            mergedConn.releaseConnector();
//        }
////        if(!sourceItem.isDisposed()) {
////            sourceItem.dispose();
////        }
////        if(!targetItem.isDisposed()) {
////            targetItem.dispose();
////        }
////        if(!mergedItem.isDisposed()) {
////            mergedItem.dispose();
////        }
//    }

//    private void checkAllItems(TreeItem item, boolean checked) {
//    	item.setChecked(checked);
//    	for(TreeItem child : item.getItems()) {
//    		checkAllItems(child,checked);
//    	}
//    }
    
    public void setShowAllChecks(boolean showAllCheck) {
    	this.showAllChecks=showAllCheck;
    }
    /**
     * Grays the specified tree item. Designed to be called on parents after a change on one of its
     * child items. This method will recompute the flag of the parent node according to child item
     * status. Accordingly, this method will enforce consistency between the item and the underlying
     * {@link IComparisonItem} information. <br>
     * <b>Example:</b><br>
     * Let's say you have one node with 2 children : one child is checked, the other unchecked, making
     * the parent node grayed :<br>
     * [-] parent<br>
     * &nbsp;&nbsp;[X] child 1<br>
     * &nbsp;&nbsp;[ ] child 2<br>
     * <br>
     * When the user click on child 1 to uncheck it, a call will be made to this method with the <b>
     * parent</b> as argument. This method will uncheck and ungray the parent node. Accordingly, the
     * parent's {@link IComparisonItem}'s merge proposal which was initially <code>null</code> (to let
     * the process consider the children) will be set to the target value of the comparison item.
     * 
     * @param item parent item of an item being checked that needs to get grayed
     * @param grayed <code>true</code> to gray, <code>false</code> to ungray
     */
    private void setParentsGrayed(TreeItem item) {
    	boolean hasOneSelectedChild=false;
    	boolean allChildSelected = true;
    	// Detecting the children selection status
    	// We check if at least one child item is selected
		for(TreeItem child : item.getItems()) {
			if(child.getChecked() || child.getGrayed()) {
				hasOneSelectedChild=true;
			}
			if(!child.getChecked() || child.getGrayed()) {
				allChildSelected = false;
			}
		}
		
		// If all children selected, we have a check
		boolean toCheck, toGray;
		if(allChildSelected) {
			toCheck = true;
			toGray = false;
		} else if( hasOneSelectedChild ) {
			// If not all children selected and at least one child selection, we got a gray
			toCheck = true;
			toGray = true;
		} else {
			// Every child unselected, we unselect the parent
			toCheck = false;
			toGray = false;
		}
		// Optimization: only performing change if needed
		if(item.getChecked()!= toCheck || item.getGrayed()!=toGray) {
			log.trace("Parent TreeItem [" + item.getText() + "]: check=" + (toCheck ? 1:0) + ", gray="+(toGray ? 1:0));
			item.setChecked(toCheck);
			item.setGrayed(toGray);
	    	// Aligning comparison item information
	    	if(item.getData() instanceof MergeNavigator) {
	    		final MergeNavigator n = (MergeNavigator)item.getData();
	    		final IComparisonItem i =(IComparisonItem)n.getModel();
	    		if(item.getGrayed()) {
	    			// If asked to gray, we have child and we reset parent comparison to consider children
	    			if(i.getMergeInfo().getMergeProposal()!=null) {
	    				log.trace("Parent ComparisonItem [" + item.getText() + "]: Setting null proposal");
	    				i.getMergeInfo().setMergeProposal(null);
	    			}
	    		} else if(item.getChecked()) {
	    			log.trace("Parent ComparisonItem [" + item.getText() + "]: Setting source proposal");
	    			i.getMergeInfo().setMergeProposal(i.getSource());
	    		} else {
	    			// Here we are completely unchecked, so target element should be used
	    			log.trace("Parent ComparisonItem [" + item.getText() + "]: Setting target proposal");
	    			i.getMergeInfo().setMergeProposal(i.getTarget());
	    		}
	    	}
	    	
	    	if(item.getParentItem()!=null) {
	    		setParentsGrayed(item.getParentItem());
	    	}
		}
    }
}
