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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IDiagramItem;

/**
 * A comparator which orders dependency trees.
 * The root of the tree should be given in the constructor while
 * root children should be compared.<br>
 * This comparator will order items in the following manner :<br>
 * - Computing dependencies of nodes to outside elements
 * - Ordering items by their dependency.
 * 
 * @author Christophe
 *
 */
public class DependencyTreeComparator {

	private Map<DependencyTree,List<IReference>> dependencyMap;
	private Map<DependencyTree,List<IReference>> innerRefsMap;
	private List<DependencyTree> displayed;
	private List<IReference> displayedReferences;
	private List<IReference> displayedDependencies;
	public DependencyTreeComparator(DependencyTree<IDiagramItem> tree) {
		dependencyMap = new HashMap<DependencyTree, List<IReference>>();
		innerRefsMap = new HashMap<DependencyTree, List<IReference>>();
		displayed = new ArrayList<DependencyTree>();
		displayedReferences = new ArrayList<IReference>();
		displayedDependencies = new ArrayList<IReference>();
		initializeMap(null,tree);
	}
	
	private void initializeMap(DependencyTree<IDiagramItem> rootNode, DependencyTree<IDiagramItem> tree) {
		if(tree.getModel()!=null) {
			final IBasicTable t = (IBasicTable)tree.getModel().getItemModel();
			List<IReference> deps = dependencyMap.get(rootNode);
			if(deps==null) {
				deps = new ArrayList<IReference>();
				dependencyMap.put(rootNode, deps);
			}
			List<IReference> innerRefs = innerRefsMap.get(rootNode);
			if(innerRefs==null) {
				innerRefs = new ArrayList<IReference>();
				innerRefsMap.put(rootNode, innerRefs);
			}
			// Adding reference
			innerRefs.add(t.getReference());
			// Adding current dependencies
			addTableDependencies(t,deps);
			// Filling with children
			for(DependencyTree<IDiagramItem> child : tree.getChildren()) {
				initializeMap(rootNode, child);
			}
		} else {
			// Handling root node
			for(DependencyTree<IDiagramItem> child : tree.getChildren()) {
				initializeMap(child, child);
			}
		}
	}
	
	public DependencyTree<IDiagramItem> getNextTreeToDisplay() {
		DependencyTree<IDiagramItem> next = null;
		if(displayed.isEmpty()) {
			for(DependencyTree<IDiagramItem> t : innerRefsMap.keySet()) {
				if(next==null || innerRefsMap.get(t).size()>innerRefsMap.get(next).size()) {
					next = t;
				}
			}
		} else {
			// Building current 
			
			int maxReferences = 0;
			for(DependencyTree<IDiagramItem> eligibleTree : innerRefsMap.keySet()) {
				// looping when already displayed
				if(displayed.contains(eligibleTree)) {
					continue;
				}
				final int count = countContains(displayedDependencies, innerRefsMap.get(eligibleTree));
				if(next==null || count>maxReferences) {
					next = eligibleTree;
					maxReferences = count;
				}
			}
		}
		// Adding to displayed table
		if(next == null ) { 
			return null;
		}
		displayed.add(next);
		displayedReferences.addAll(innerRefsMap.get(next));
		displayedDependencies.addAll(dependencyMap.get(next));
		return next;

	}
	private void addTableDependencies(IBasicTable t, List<IReference> deps) {
		Collection<IReference> refs = VersionHelper.getVersionable(t).getReferenceDependencies();
		for(IReference r : refs) {
			if(r!=null && r.getType()==IElementType.getInstance(IBasicTable.TYPE_ID)) {
				deps.add(r);
			}
		}
	}
	
	public int compare(DependencyTree<IDiagramItem> t1, DependencyTree<IDiagramItem> t2) {
		// Counting t1 dependencies on t2 tree
		final List<IReference> t1Deps = dependencyMap.get(t1);
		final int t1DepsCount = countContains(innerRefsMap.get(t2), t1Deps);
		// Counting t2 dependencies on t1 tree
		final List<IReference> t2Deps = dependencyMap.get(t2);
		final int t2DepsCount = countContains(innerRefsMap.get(t1), t2Deps);
		if(t1DepsCount>0 || t2DepsCount>0) {
			return t1DepsCount-t2DepsCount;
		} else {
			return t2Deps.size()-t1Deps.size();
		}
		
	}

	private int countContains(Collection<?> lookupList, Collection<?> containedList) {
		int count = 0;
		for(Object o : containedList) {
			if(lookupList.contains(o)) {
				count++;
			}
		}
		return count;
	}
}
