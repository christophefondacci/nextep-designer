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
package com.nextep.designer.dbgm.factories;

import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.TypeColumn;
import com.nextep.datadesigner.dbgm.impl.UserType;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class UserTypeFactory extends VersionableFactory {

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#createVersionable()
	 */
	@Override
	public IVersionable<?> createVersionable() {
		return new UserType();
	}

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#rawCopy(com.nextep.designer.vcs.model.IVersionable, com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public void rawCopy(IVersionable<?> source, IVersionable<?> destination) {
		IUserType src = (IUserType)source.getVersionnedObject().getModel();
		IUserType tgt = (IUserType)destination.getVersionnedObject().getModel();
		
		tgt.setName(src.getName());
		tgt.setDescription(src.getDescription());
		tgt.setReference(src.getReference());
		for(ITypeColumn c : src.getColumns()) {
			ITypeColumn copiedCol = new TypeColumn();
			copiedCol.setName(c.getName());
			copiedCol.setDescription(c.getDescription());
			copiedCol.setDatatype(new Datatype(c.getDatatype()));
			copiedCol.setReference(c.getReference());
			tgt.addColumn(copiedCol);
		}
		versionCopy(VersionHelper.getVersionable(src), VersionHelper.getVersionable(tgt));
	}

}
