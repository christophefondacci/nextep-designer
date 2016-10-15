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
package com.nextep.designer.vcs.ui.navigators;

import org.eclipse.jface.viewers.IElementComparer;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.ui.model.ITypedNode;

public class VersionableNavigatorComparer implements IElementComparer {

	@Override
	public boolean equals(Object a, Object b) {
		if (a instanceof ITypedNode && b instanceof ITypedNode) {
			final ITypedNode nodeA = (ITypedNode) a;
			final ITypedNode nodeB = (ITypedNode) b;
			return nodeA.getNodeType().equals(nodeB.getNodeType())
					&& equals(nodeA.getParent(), nodeB.getParent());
		} else if (a instanceof IReferenceable && b instanceof IReferenceable
				&& !(a instanceof IReference) && !(a instanceof IComparisonItem)
				&& !(b instanceof IReference) && !(b instanceof IComparisonItem)) {
			final IReference aRef = ((IReferenceable) a).getReference();
			final IReference bRef = ((IReferenceable) b).getReference();
			if (aRef != null && bRef != null) {
				return ((IReferenceable) a).getReference().equals(
						((IReferenceable) b).getReference());
			}
		}
		return (a != null && a.equals(b)) || (a == null && b == null);
	}

	@Override
	public int hashCode(Object element) {
		if (element instanceof ITypedNode) {
			final ITypedNode typedNode = (ITypedNode) element;
			return hashCode(typedNode.getParent()) + typedNode.getNodeType().hashCode();
		} else if (element instanceof IReferenceable && !(element instanceof IReference)
				&& !(element instanceof IComparisonItem)) {
			final IReference ref = ((IReferenceable) element).getReference();
			if (ref != null) {
				return ((IReferenceable) element).getReference().hashCode();
			}
		}
		return element != null ? element.hashCode() : 1;
	}

}
