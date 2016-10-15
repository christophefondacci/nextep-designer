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
package com.nextep.datadesigner.vcs.gui.external;

import com.nextep.datadesigner.gui.model.ITypePersister;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 *
 */
public class VersionablePersister extends TypePersister implements ITypePersister {

	/** Global factory ID as defined in the extension point */
	public static final String FACTORY_ID = "com.neXtep.designer.vcs.ui.versionableFactory";

	/**
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	@Override
	public String getFactoryId() {
		return FACTORY_ID;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.gui.external.TypePersister#getPersistableClass()
	 */
	@Override
	protected Class<?> getPersistableClass() {
		return IVersionable.class;
	}


}
