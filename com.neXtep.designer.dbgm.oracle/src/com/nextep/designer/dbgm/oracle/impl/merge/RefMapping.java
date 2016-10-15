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
package com.nextep.designer.dbgm.oracle.impl.merge;

import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;

/**
 * This class is an intermediate class which allows us to convert our Map<IReference, IReference> to
 * a list of RefMapping instances which contains accessors to key and value pairs. A merger for this
 * class is provided to compare / build resulting objects. <br>
 * This class is only used while processing mapping comparison to transfer comparison data.
 * 
 * @author Christophe Fondacci
 */
public class RefMapping extends NamedObservable implements IReferenceable, ITypedObject {

	public static final String TYPE_ID = "REF_MAPPING"; //$NON-NLS-1$

	private IReference clusterCol;
	private IReference tableCol;
	private IReference internalDummyRef;

	public RefMapping(IReference clusterCol, IReference tableCol) {
		this.clusterCol = clusterCol;
		this.tableCol = tableCol;
		internalDummyRef = new Reference(getType(), null, null);
		internalDummyRef.setUID(clusterCol.getReferenceId());
		// internalDummyRef.setInstance(this)
	}

	@Override
	public IReference getReference() {
		return internalDummyRef; // clusterCol != null ? clusterCol.getReference() : null;
	}

	@Override
	public void setReference(IReference ref) {
	}

	public IReference getClusterCol() {
		return clusterCol;
	}

	public IReference getTableCol() {
		return tableCol;
	}

	public void setClusterCol(IReference ref) {
		this.clusterCol = ref;
	}

	public void setTableCol(IReference ref) {
		this.tableCol = ref;
	}

	@Override
	public String getName() {
		final INamedObject clusCol = (INamedObject) VersionHelper.getReferencedItem(clusterCol);
		return clusCol != null ? clusCol.getName() : ""; //$NON-NLS-1$
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RefMapping) {
			final RefMapping other = (RefMapping) obj;
			boolean clusterColEq = false;
			if (getClusterCol() == null) {
				clusterColEq = (other.getClusterCol() == null);
			} else {
				clusterColEq = (getClusterCol() == other.getClusterCol());
			}
			boolean tableColEq = false;
			if (getClusterCol() == null) {
				tableColEq = (other.getTableCol() == null);
			} else {
				tableColEq = (getTableCol() == other.getTableCol());
			}
			return clusterColEq && tableColEq;
		}
		return false;
	}

}
