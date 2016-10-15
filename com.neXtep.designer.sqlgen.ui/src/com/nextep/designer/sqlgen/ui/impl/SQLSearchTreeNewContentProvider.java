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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.text.Match;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

public class SQLSearchTreeNewContentProvider implements ITreeContentProvider {
	private SQLSearchResult result;
	private Viewer viewer;
	private Map<IVersionContainer,List<Object>> elements;
	public SQLSearchTreeNewContentProvider(Viewer treeViewer) {
		this.viewer = treeViewer;
	}
	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof IVersionContainer) {
			List<?> list = elements.get(parentElement);
			if(list != null) {
				return list.toArray();
			}
			return null;
		} else if(parentElement instanceof IVersionable<?>) {
			return result.getMatches(parentElement);
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if(element instanceof IVersionContainer) {
			return null;
		} else if( element instanceof IVersionable<?>) {
			return ((IVersionable<?>) element).getContainer();
		} else if( element instanceof Match) {
			return ((Match) element).getElement();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof IVersionContainer) {
			return elements.containsKey(element);
		} else if(element instanceof IVersionable<?>) {
			List<?> list = elements.get(((IVersionable<?>) element).getContainer());
			if(list != null) {
				return list.contains(element);
			}
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return elements.keySet().toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput instanceof SQLSearchResult) {
			result = (SQLSearchResult)newInput;
			elements = new HashMap<IVersionContainer, List<Object>>();
			elementsChanged(result.getElements(),false);
		}
	}

	public synchronized void elementsChanged(Object[] updatedElements, boolean updateViewer) {
		for(Object o : updatedElements) {
			if(o instanceof IVersionable<?>) {
				final IVersionContainer parentContainer = ((IVersionable<?>) o).getContainer();
				List<Object> objs = elements.get(parentContainer);
				if(objs == null) {
					objs = new ArrayList<Object>();
					elements.put(parentContainer, objs);
				}
				objs.add(o);
				if(updateViewer) {
					if(viewer instanceof TreeViewer) {
						TreeViewer treeViewer = (TreeViewer)viewer;
						treeViewer.add(parentContainer, o);
						// For all matches found
						for(Match match : result.getMatches(o)) {
							treeViewer.add(o, match);
						}
					} else if(viewer instanceof TableViewer) {
						TableViewer tabViewer = (TableViewer)viewer;
						tabViewer.add(o);
					}
				}
			}
		}
		viewer.refresh();
	}
}
