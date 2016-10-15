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
package com.nextep.datadesigner.vcs.impl;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IMerger;

/**
 * A factory for element type mergers.
 * 
 * @author Christophe Fondacci
 * 
 */
public class MergerFactory {
	private static final String EXTENSION_ID = "com.neXtep.designer.vcs.MergerProvider";

	// private static Map<IElementType,IMerger> mergersMap= new
	// HashMap<IElementType, IMerger>();

	/**
	 * @deprecated use
	 */
	@Deprecated
	public static IMerger getMerger(IReferenceable ref) {
		if (ref instanceof IReference) {
			return getMerger(IElementType.getInstance(IReference.TYPE_ID));
		} else {
			return getMerger(ref.getReference().getType());
		}
	}

	/**
	 * Retrieves the merger of the specified {@link IReferenceable} object. This
	 * is a convenience method.
	 * 
	 * @param ref
	 *            referenceable whose type will be used to look for an
	 *            appropriate merger
	 * @param scope
	 *            scope of the current merge operation
	 * @return the corresponding merger or <code>null</code> if no merger is
	 *         defined for the type of the specified {@link IReferenceable}
	 */
	public static IMerger getMerger(IReferenceable ref, ComparisonScope scope) {
		if (ref instanceof IReference) {
			return getMerger(IElementType.getInstance(IReference.TYPE_ID), scope);
		} else {
			return getMerger(ref.getReference().getType(), scope);
		}
	}

	/**
	 * Retrieves the merger of the specified type
	 * 
	 * @param type
	 *            type of the merger to retrieve
	 * @return the merger handling the specified type
	 * @deprecated use the
	 *             {@link MergerFactory#getMerger(IElementType, ComparisonScope)}
	 *             method instead
	 */
	@Deprecated
	public static IMerger getMerger(IElementType type) {
		return getMerger(type, ComparisonScope.REPOSITORY);
	}

	/**
	 * Retrieves the merger of the specified type
	 * 
	 * @param type
	 *            type of the merger to look for
	 * @param scope
	 *            scope of the merge operation (database, repository, ...)
	 * @return the corresponding merger or <code>null</code> if no merger is
	 *         defined for the specified type.
	 */
	public static IMerger getMerger(IElementType type, ComparisonScope scope) {
		// //Trying to get merger from cache
		// IMerger m = mergersMap.get(type);
		// if(m!=null || type == null) {
		// return m;
		// }
		// Otherwise we load extension
		Collection<IConfigurationElement> confs = Designer.getInstance().getExtensions(
				EXTENSION_ID, "typeId", type.getId());
		IConfigurationElement c = null;
		for (IConfigurationElement conf : confs) {

			// We stop at the first non null context
			String context = conf.getAttribute("context");
			if (context != null && !"".equals(context.trim())
					&& context.equals(Designer.getInstance().getContext())) {
				c = conf;
				break;
			} else if (context == null || "".equals(context)) {
				c = conf;
			}
		}
		// Checking if we have any merger
		if (c == null) {
			return null;
		}
		try {
			IMerger merger = (IMerger) c.createExecutableExtension("mergerClass");
			// Setting strategy
			merger.setMergeStrategy(MergeStrategy.create(scope));
			// We update the cache
			// mergersMap.put(type, merger);
			return merger;
		} catch (CoreException e) {
			throw new ErrorException(e);
		}
	}

	/**
	 * Registers the given listener to all known mergers
	 * 
	 * @param l
	 *            listener to register
	 */
	public static void registerMergeListener(IEventListener l) {
		// Browsing all mergers and add the listener
		for (IElementType t : IElementType.values()) {
			IMerger m = getMerger(t);
			if (m != null) {
				Designer.getListenerService().registerListener(null, m, l);
			}
		}
	}

	/**
	 * Unregisters the given listener from all known mergers
	 * 
	 * @param l
	 *            listener to unregister
	 */
	public static void unregisterMergeListener(IEventListener l) {
		// Browsing all mergers and add the listener
		for (IElementType t : IElementType.values()) {
			IMerger m = getMerger(t);
			if (m != null) {
				m.removeListener(l);
			}
		}
	}
}
