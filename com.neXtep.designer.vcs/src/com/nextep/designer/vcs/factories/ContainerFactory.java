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
package com.nextep.designer.vcs.factories;

import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.vcs.impl.VersionContainer;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class ContainerFactory extends VersionableFactory {

	private static ContainerFactory instance = null;
	public ContainerFactory() {}
	public static ContainerFactory getInstance() {
		if(instance == null) {
			instance = new ContainerFactory();
		}
		return instance;
	}
	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#createVersionable()
	 */
	@Override
	public IVersionable createVersionable() {
		final IVersionable<IVersionContainer> v =  new VersionContainer();
		if(VersionHelper.getCurrentView() !=null) {
			v.getVersionnedObject().getModel().setDBVendor(VersionHelper.getCurrentView().getDBVendor());
		}
		return v;
	}

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#rawCopy(com.nextep.designer.vcs.model.IVersionable, com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		IVersionContainer src = (IVersionContainer)source.getVersionnedObject().getModel();
		IVersionContainer dst = (IVersionContainer)destination.getVersionnedObject().getModel();
		for(IVersionable<?> v : src.getContents()) {
			//IVersionable<?> vCopy = VersionableFactory.copy(v);
			dst.getContents().add(v); //Copy);
			try {
				Observable.deactivateListeners();
				v.setContainer(dst);
			} finally {
				Observable.activateListeners();
			}
		}
		dst.setShortName(src.getShortName());
		dst.setDBVendor(src.getDBVendor());
		versionCopy(source,destination);
	}

}
