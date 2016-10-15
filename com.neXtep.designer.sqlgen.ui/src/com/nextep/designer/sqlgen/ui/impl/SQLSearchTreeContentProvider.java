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
package com.nextep.designer.sqlgen.ui.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

public class SQLSearchTreeContentProvider implements ITreeContentProvider {


	private final Object[] EMPTY_ARR= new Object[0];

	private AbstractTextSearchResult fResult;
//	private ISearchPage fPage;
	private AbstractTreeViewer fTreeViewer;
	private Map<Object,Set<Object>> fChildrenMap;
	
	SQLSearchTreeContentProvider( AbstractTreeViewer viewer) {
//		fPage= page;
		fTreeViewer= viewer;
	}
	
	public Object[] getElements(Object inputElement) {
		Object[] children= getChildren(inputElement);
		int elementLimit= getElementLimit();
		if (elementLimit != -1 && elementLimit < children.length) {
			Object[] limitedChildren= new Object[elementLimit];
			System.arraycopy(children, 0, limitedChildren, 0, elementLimit);
			return limitedChildren;
		}
		return children;
	}
	
	private int getElementLimit() {
		return 0; //fPage.getElementLimit().intValue();
	}
	
	public void dispose() {
		// nothing to do
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof SQLSearchResult) {
			initialize((SQLSearchResult) newInput);
		}
	}
	
	private synchronized void initialize(AbstractTextSearchResult result) {
		fResult= result;
		fChildrenMap= new HashMap<Object, Set<Object>>();
		boolean showLineMatches= true; // !((SQLSearchQuery) fResult.getQuery()).isFileNameSearch();
		
		if (result != null) {
			Object[] elements= result.getElements();
			for (int i= 0; i < elements.length; i++) {
				if (showLineMatches) {
					Match[] matches= result.getMatches(elements[i]);
					for (int j= 0; j < matches.length; j++) {
						insert(matches[j].getElement(), false);
					}
				} else {
					insert(elements[i], false);
				}
			}
		}
	}

	private void insert(Object child, boolean refreshViewer) {
		Object parent= getParent(child);
		while (parent != null) {
			if (insertChild(parent, child)) {
				if (refreshViewer)
					fTreeViewer.add(parent, child);
			} else {
				if (refreshViewer)
					fTreeViewer.refresh(parent);
				return;
			}
			child= parent;
			parent= getParent(child);
		}
		if (insertChild(fResult, child)) {
			if (refreshViewer)
				fTreeViewer.add(fResult, child);
		}
	}

	/**
	 * Adds the child to the parent.
	 * 
	 * @param parent the parent
	 * @param child the child
	 * @return <code>true</code> if this set did not already contain the specified element

	 */
	private boolean insertChild(Object parent, Object child) {
		Set<Object> children= fChildrenMap.get(parent);
		if (children == null) {
			children= new HashSet<Object>();
			fChildrenMap.put(parent, children);
		}
		return children.add(child);
	}
	
//	private boolean hasChild(Object parent, Object child) {
//		Set<Object> children= fChildrenMap.get(parent);
//		return children != null && children.contains(child);
//	}
	

	private void remove(Object element, boolean refreshViewer) {
		// precondition here:  fResult.getMatchCount(child) <= 0
	
		if (hasChildren(element)) {
			if (refreshViewer)
				fTreeViewer.refresh(element);
		} else {
			if (!hasMatches(element)) {
				fChildrenMap.remove(element);
				Object parent= getParent(element);
				if (parent != null) {
					removeFromSiblings(element, parent);
					remove(parent, refreshViewer);
				} else {
					removeFromSiblings(element, fResult);
					if (refreshViewer)
						fTreeViewer.refresh();
				}
			} else {
				if (refreshViewer) {
					fTreeViewer.refresh(element);
				}
			}
		}
	}

	private boolean hasMatches(Object element) {
		return fResult.getMatchCount(element) > 0;
	}
	
	
	private void removeFromSiblings(Object element, Object parent) {
		Set<Object> siblings= fChildrenMap.get(parent);
		if (siblings != null) {
			siblings.remove(element);
		}
	}

	public Object[] getChildren(Object parentElement) {
		Set<Object> children= fChildrenMap.get(parentElement);
		if (children == null)
			return EMPTY_ARR;
		return children.toArray();
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.search.internal.ui.text.IFileSearchContentProvider#elementsChanged(java.lang.Object[])
	 */
	public synchronized void elementsChanged(Object[] updatedElements) {
		for (int i= 0; i < updatedElements.length; i++) {
				// change events to elements are reported in file search
				if (fResult.getMatchCount(updatedElements[i]) > 0)
					insert(updatedElements[i], true);
				else 
					remove(updatedElements[i], true);
		}
	}

	public void clear() {
		initialize(fResult);
		fTreeViewer.refresh();
	}

	public Object getParent(Object element) {
		if (element instanceof Match) {
			Match match= (Match) element;
			return match.getElement();
		}
		return null;
	}



}
