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
package com.nextep.designer.dbgm.oracle.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.nextep.datadesigner.dbgm.impl.SynchedVersionable;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.model.CollectionType;
import com.nextep.designer.dbgm.model.IUserCollection;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class UserCollection extends SynchedVersionable<IUserCollection> implements IUserCollection {

	private CollectionType collectionType = CollectionType.NESTED_TABLE;
	private IDatatype datatype;
	private int size;

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public CollectionType getCollectionType() {
		return collectionType;
	}

	@Override
	public IDatatype getDatatype() {
		return datatype;
	}

	@Override
	public void setCollectionType(CollectionType collectionType) {
		this.collectionType = collectionType;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setDatatype(IDatatype datatype) {
		this.datatype = datatype;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public void setSize(int size) {
		this.size = size;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public Collection<IReference> getReferenceDependencies() {
		List<IVersionable<?>> types = VersionHelper.getAllVersionables(
				VersionHelper.getCurrentView(), IElementType.getInstance(IUserType.TYPE_ID));
		for (IVersionable<?> t : types) {
			if (datatype != null && datatype.getName() != null && t.getName() != null
					&& datatype.getName().toUpperCase().equals(t.getName().toUpperCase())) {
				return Arrays.asList(t.getReference());
			}
		}
		return Collections.emptyList();
	}

}
