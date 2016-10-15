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
package com.nextep.designer.dbgm.ui.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionable;

public class DiagramLayoutService {
	private static final int H_SPACING = 100;
	private static final int V_SPACING = 50;
	private static final int WIDTH = 200;
	/**
	 * Sorts the elements according to their dependencies for the auto layouting
	 * feature.
	 * 
	 * @author Christophe
	 *
	 */
	private static class TableDependencyComparator implements Comparator<IDiagramItem> {
		private MultiValueMap invRefMap;
		public TableDependencyComparator(MultiValueMap invRefMap) {
			this.invRefMap = invRefMap; 
		}
		@SuppressWarnings("unchecked")
		@Override
		public int compare(IDiagramItem src, IDiagramItem tgt) {
			if(tgt.getItemModel() instanceof IBasicTable && src.getItemModel() instanceof IBasicTable) {
				final IVersionable<IBasicTable> srcTab = (IVersionable<IBasicTable>)src.getItemModel();
				final IVersionable<IBasicTable> tgtTab = (IVersionable<IBasicTable>)tgt.getItemModel();
				final Collection<IReference> srcRefs = srcTab.getReferenceDependencies();
				final Collection<IReference> tgtRefs = tgtTab.getReferenceDependencies();
				final int depSrcCount = countTableDependencies(srcTab.getReference(),srcRefs);
				final int depTgtCount = countTableDependencies(srcTab.getReference(),tgtRefs);
				// Checking if one of the object depend on the other
				if(srcRefs.contains(tgtTab.getReference()) && !tgtRefs.contains(srcTab.getReference())) {
					return 1;
				} else if(tgtRefs.contains(srcTab.getReference()) && !srcRefs.contains(tgtTab.getReference()) ) {
					return -1;
				} else { //if(srcRefs.contains(tgtTab.getReference()) && tgtRefs.contains(srcTab.getReference())) {
					Collection<IReferencer> srcRevRefs = invRefMap.getCollection(srcTab);
					if(srcRevRefs==null) {
						srcRevRefs = Collections.emptyList();
					}
					Collection<IReferencer> tgtRevRefs = invRefMap.getCollection(tgtTab);
					if(tgtRevRefs == null) {
						tgtRevRefs = Collections.emptyList();
					}
					if(tgtRevRefs.size()> 2*srcRevRefs.size()) {
						return 1;
					} else if( srcRevRefs.size()>2*srcRevRefs.size()) {
						return -1;
					} else {
						// Any object with 0 dependency is placed first
						if(depSrcCount==0 && depTgtCount>0) {
							return -1;
						} else if( depTgtCount == 0 && depSrcCount>0) {
							return 1;
						} else {
							return depTgtCount - depSrcCount;
						}
					}
					
				} 
//				else {
//					return srcTab.getName().compareTo(tgtTab.getName());
//				}
			}
			return -1;
		}
		public int countTableDependencies(IReference item, Collection<IReference> refs) {
			int count = 0;
			for(IReference r : refs) {
				if(r.getType()==IElementType.getInstance(IBasicTable.TYPE_ID) && r!=item) {
					count++;
				}
			}
			return count;
		}
	}
	
	/**
	 * Layout the diagram
	 * @param diagram
	 */
	public static void autoLayout(IDiagram diagram) {
		int x = 1;
		int y = 1;
		// Getting diagram items
		List<IDiagramItem> items = new ArrayList<IDiagramItem>(diagram.getItems());
		// Computing reverse dependencies map (one shot, multiple uses)
		final MultiValueMap invRefMap = CorePlugin.getService(IReferenceManager.class).getReverseDependenciesMap(IElementType.getInstance(IBasicTable.TYPE_ID));
		// Shuffling first, since our comparator is not deterministic, we mix our
		// items to provide multiple layout suggestions
        Collections.shuffle(items);
        // Sorting items
        Collections.sort(items, new TableDependencyComparator(invRefMap));
       
        // Hashing diagram items
        Map<IReference,IDiagramItem> refMap = new HashMap<IReference, IDiagramItem>();
        for(IDiagramItem i : items) {
        	if(!(i.getItemModel() instanceof IBasicTable)) {
        		continue;
        	}
        	refMap.put(((IBasicTable)i.getItemModel()).getReference(), i);
        }
        // Building tree
        DependencyTree<IDiagramItem> rootTree = new DependencyTree<IDiagramItem>(null);
        List<IDiagramItem> processed = new ArrayList<IDiagramItem>();
        for(IDiagramItem i : items) {
        	if(!processed.contains(i)) {
        		rootTree.addChild(buildTree(i, items, processed, refMap,invRefMap));
        	}
        }
        // Sorting tree
//        DependencyTreeComparator comparator = new DependencyTreeComparator(rootTree);
////        Collections.sort(rootTree.getChildren(),comparator);
//        DependencyTree next = comparator.getNextTreeToDisplay();
//        while(next!=null) {
//        	y = position(next,x,y) + V_SPACING;
//        	next = comparator.getNextTreeToDisplay();
//        }
        for(DependencyTree<IDiagramItem> treeItem : rootTree.getChildren()) {
        	y = position(treeItem,x,y) + V_SPACING;
        }
//        // Parent tables referenced via foreign keys
//		for(IKeyConstraint k : table.getConstraints()) {
//			switch(k.getConstraintType()) {
//			case FOREIGN:
//				ForeignKeyConstraint fk = (ForeignKeyConstraint)k;
//				if(fk.getRemoteConstraint()!=null) {
//					IBasicTable t = fk.getRemoteConstraint().getConstrainedTable();
//					if(t!=null) {
//						final IDiagramItem fkItem = createTableItem(fk.getRemoteConstraint().getConstrainedTable(),x,y);
//						parentItems.add(fkItem);
//						// Adjusting position variables
//						y+=fkItem.getHeight()+V_SPACING;
//						// MAnaging max height / width
//						if(y>maxHeight) maxHeight = y;
//						if(fkItem.getWidth()> maxWidth) {
//							maxWidth = fkItem.getWidth();
//						}
//						// Registering item to diagrm
//						d.addItem(fkItem);
//					}
//				}
//				break;
//			}
//		}
//		leftHeight = maxHeight;
//		// Central table (current)
//        x+=maxWidth + H_SPACING;
//        y=1;
//        
//        IDiagramItem tabItem = new DiagramItem(VersionHelper.getVersionable(table),x,y);
//        tabItem.setHeight(40 + 21 * table.getColumns().size());
//        d.addItem(tabItem);
//        
//        // Child tables
//        x+=tabItem.getWidth() + H_SPACING;
//        y=1;
//        
//        Collection<IReferencer> children = CorePlugin.getService(IReferenceManager.class).getReverseDependencies(table);
//        for(IReferencer r : children) {
//        	if(r instanceof IBasicTable) {
//        		final IDiagramItem i = createTableItem((IBasicTable)r,x,y);
//        		childItems.add(i);
//        		y+=i.getHeight()+V_SPACING;
//        		if(y>maxHeight) maxHeight = y;
//        		d.addItem(i);
//        	}
//        }
//        
//        // Repositioning central table Y
//        tabItem.setYStart(maxHeight/2 - tabItem.getHeight()/2);
//        
//        // Repositioning minimum height elements
//        if(leftHeight<maxHeight) {
//        	repositionItem(parentItems, leftHeight, maxHeight);
//        } else {
//        	repositionItem(childItems, y, maxHeight);
//        }
        
	}
	
	@SuppressWarnings("unchecked")
	private static DependencyTree<IDiagramItem> buildTree(IDiagramItem currentItem, List<IDiagramItem> items, List<IDiagramItem> processed, Map<IReference,IDiagramItem> refMap, MultiValueMap invRefMap) {
		processed.add(currentItem);
		// Our item node
		DependencyTree<IDiagramItem> tree = new DependencyTree<IDiagramItem>(currentItem);
		// Assuming table items
		final IBasicTable table = (IBasicTable)currentItem.getItemModel();
		Collection<IReferencer> dependencies = invRefMap.getCollection(table.getReference());
		if(dependencies == null) {
			dependencies = Collections.emptyList();
		}
		for(IReferencer r : dependencies) {
			if(r instanceof IBasicTable) {
				final IDiagramItem depItem = refMap.get(((IBasicTable)r).getReference());
				// If our dependency is in our set and is not processed we build the sub Tree
				if(items.contains(depItem) && !processed.contains(depItem)) {
					// Recursively processing sub item
					tree.addChild(buildTree(depItem, items, processed, refMap, invRefMap));
				}
			}
		}
		
		
		// Child dependencies 
		Collection<IReference> refDependencies = VersionHelper.getVersionable(table).getReferenceDependencies();
		for(IReference r : refDependencies) {
			IReferenceable item = VersionHelper.getReferencedItem(r);
			if(item instanceof IBasicTable) {
				final IDiagramItem depItem = refMap.get(((IBasicTable)item).getReference());
				// If our dependency is in our set and is not processed we build the sub Tree
				if(items.contains(depItem) && !processed.contains(depItem)) {
					// Recursively processing sub item
					tree.addChild(buildTree(depItem, items, processed, refMap, invRefMap));
				}
			}
		}
		
		
		// Returning
		return tree;
	}
	private static int position(DependencyTree<IDiagramItem> treeItem, int x, int y) {
		final IDiagramItem i = treeItem.getModel();
		i.setWidth(WIDTH);
		i.setHeight(40 + 21 * ((IBasicTable)treeItem.getModel().getItemModel()).getColumns().size());
		i.setXStart(x);
		i.setYStart(y); //==1 ? y : y+V_SPACING);
		int newY = y;
		int newX = x + i.getWidth() + H_SPACING;
		boolean hasChild = false;
		for(DependencyTree<IDiagramItem> child : treeItem.getChildren()) {
			newY = position(child, newX, newY); // +child.getModel().getHeight() + V_SPACING;
			hasChild=true;
		}
		if(hasChild) {
			if(newY>i.getYStart()+i.getHeight()) {
				i.setYStart(i.getYStart() + (newY-V_SPACING - i.getYStart())/2 -i.getHeight()/2);
				return newY;
			} else {
				return y+i.getHeight() + V_SPACING;
			}
			
		} else {
			return y + i.getHeight() + V_SPACING;
		}
//		if(newY == y) {
//			newY = y + treeItem.getModel().getHeight() + V_SPACING;
//		}
//		return Math.max(newY,y + treeItem.getModel().getHeight());
//		return y + treeItem.getModel().getHeight();
	}
//	private void repositionItem(Collection<IDiagramItem> items, int itemsHeight, int maxHeight) {
//		int deltaY = (maxHeight - itemsHeight) / 2;
//		for(IDiagramItem i : items) {
//			i.setYStart(i.getYStart()+deltaY);
//		}
//	}
}
