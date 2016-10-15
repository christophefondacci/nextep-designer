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
package com.nextep.datadesigner.vcs.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.vcs.model.IVersionable;

public class VersionableSorter implements Comparator<IVersionable<?>> {

	private static final Log log = LogFactory.getLog(VersionableSorter.class);

	@Override
	public int compare(IVersionable<?> o1, IVersionable<?> o2) {
		// Putting dependencies first
		if (containsAny(o1.getReferenceDependencies(), o2.getReferenceMap().keySet())) {
			return 1;
		} else if (containsAny(o2.getReferenceDependencies(), o1.getReferenceMap().keySet())) {
			return -1;
		} else {
			// We only put tables first
			if ("TABLE".equals(o1.getType().getId()) && "TABLE".equals(o2.getType().getId())) {
				return -1;
			} else if ("TABLE".equals(o1.getType().getId())) {
				return -1;
			} else if ("TABLE".equals(o2.getType().getId())) {
				return 1;
			} else {
				return o1.getName().compareTo(o2.getName());
			}
		}
	}

	private boolean containsAny(Collection<?> lookup, Collection<?> contained) {
		for (Object o : contained) {
			if (lookup.contains(o)) {
				return true;
			}
		}
		return false;
	}

	public static List<IVersionable<?>> sort(Collection<IVersionable<?>> list) {
		List<IVersionable<?>> sortedList = new ArrayList<IVersionable<?>>();
		Map<IReference, IVersionable<?>> invRefMap = new HashMap<IReference, IVersionable<?>>();
		for (IVersionable<?> v : list) {
			invRefMap.put(v.getReference(), v);
			for (IReference r : v.getReferenceMap().keySet()) {
				invRefMap.put(r, v);
			}
		}
		for (IVersionable<?> v : list) {
			processItem(sortedList, invRefMap, v, new ArrayList<IVersionable<?>>());
		}
		return sortedList;
	}

	private static void processItem(List<IVersionable<?>> sortedList,
			Map<IReference, IVersionable<?>> invRefMap, IVersionable<?> toAdd,
			Collection<IVersionable<?>> stack) {
		Collection<IReference> refs = toAdd.getReferenceDependencies();
		Collection<IReference> ownReferences = toAdd.getReferenceMap().keySet();
		// If the stack already contains the versionable to add, dependency deadloop !
		// We stop the process here
		if (stack.contains(toAdd)) {
			log.warn("Deadloop found in schema inner dependencies: unsafe dependency resolution.");
			return;
		}
		stack.add(toAdd);
		for (IReference r : refs) {
			final IVersionable<?> v = invRefMap.get(r);
			if (v != null && !sortedList.contains(v) && !ownReferences.contains(r)
					&& toAdd.getReference() != r) {
				processItem(sortedList, invRefMap, v, stack);
			}
		}
		stack.remove(toAdd);
		// Might have already been imported
		if (!sortedList.contains(toAdd)) {
			sortedList.add(toAdd);
		}
	}
}
