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

import com.nextep.datadesigner.dbgm.impl.View;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class ViewFactory extends VersionableFactory {

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#createVersionable()
	 */
	@Override
	public IVersionable<?> createVersionable() {
		return new View();
	}

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#rawCopy(com.nextep.designer.vcs.model.IVersionable, com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public void rawCopy(IVersionable<?> source, IVersionable<?> destination) {
		IView src = (IView)source.getVersionnedObject().getModel();
		IView tgt = (IView)destination.getVersionnedObject().getModel();
		
		tgt.setSQLDefinition(src.getSQLDefinition());
		versionCopy((IVersionable<IView>)source,(IVersionable<IView>)destination);

	}

}
