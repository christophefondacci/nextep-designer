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
package com.nextep.designer.vcs.model.base;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IImportPolicy;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * The policy will handle conflict on the pair (type, name) and provide a resolution depending on
 * the implementation.<br>
 * Note that container contents are analyzed each time this strategy is called, which may induce
 * performance problems when importing large sets of elements, in which case a dedicated strategy
 * should be used.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractImportPolicy implements IImportPolicy {

	private class TypeNameEntry extends MultiKey {

		private static final long serialVersionUID = 1L;

		public TypeNameEntry(IElementType type, String name) {
			super(type.getId(), name);
		}
	}

	private Map<TypeNameEntry, IVersionable<?>> buildContentsMap(IVersionContainer c) {
		final Map<TypeNameEntry, IVersionable<?>> entriesMap = new HashMap<TypeNameEntry, IVersionable<?>>();
		for (IVersionable<?> v : c.getContents()) {
			entriesMap.put(new TypeNameEntry(v.getType(), v.getName()), v);
			if (v instanceof IVersionContainer) {
				entriesMap.putAll(buildContentsMap((IVersionContainer) v));
			}
		}
		return entriesMap;
	}

	private IVersionable<?> getExistingItem(IVersionContainer c, IVersionable<?> v) {
		Map<TypeNameEntry, IVersionable<?>> containerContents = buildContentsMap(c);
		final TypeNameEntry entry = new TypeNameEntry(v.getType(), v.getName());
		return containerContents.get(entry);
	}

	@Override
	public boolean importVersionable(IVersionable<?> v, IVersionContainer c, IActivity activity) {
		beforeImport(v, c, activity);
		final IVersionContainer collisionsContainer = getContainerForExistenceCheck(c);
		IVersionable<?> existingItem = getExistingItem(collisionsContainer, v);
		if (existingItem != null) {
			return existingObject(v, existingItem, activity);
		} else {
			return unexistingObject(v, c);
		}
	}

	/**
	 * Returns the container to consider when looking for existence of the objecT.
	 * 
	 * @param targetContainer container where the element needs to be imported.
	 * @return the container to consider when looking for colliding elements
	 */
	protected IVersionContainer getContainerForExistenceCheck(IVersionContainer targetContainer) {
		return targetContainer;
	}

	/**
	 * Method called when an existing object has been found in the target container. It is the
	 * implementor's role to know what to do in this situation, depending on its policy.
	 * 
	 * @param importing the currently importing versionable item
	 * @param existing the item found in the target container which matches with the importing one
	 * @param activity activity to use for optional checkout actions
	 * @return <code>true</code> if versionable has been added, else <code>false</code>
	 */
	protected abstract boolean existingObject(IVersionable<?> importing, IVersionable<?> existing,
			IActivity activity);

	/**
	 * Method called when no versioned item has been found in the parent container which could
	 * interfere with the importing one.
	 * 
	 * @param importing the currently importing versionable item
	 * @param targetContainer the target container in which the item has to be imported
	 * @return <code>true</code> if versionable has been added, else <code>false</code>
	 */
	protected abstract boolean unexistingObject(IVersionable<?> importing,
			IVersionContainer targetContainer);

	/**
	 * Allows the policy to performs all the necessary check and optional operations to prepare the
	 * import.
	 * 
	 * @param v versionable to import
	 * @param c target container to which the versionable will be imported
	 * @param activity activity to use for optional check outs
	 */
	protected abstract void beforeImport(IVersionable<?> v, IVersionContainer c, IActivity activity);
}
