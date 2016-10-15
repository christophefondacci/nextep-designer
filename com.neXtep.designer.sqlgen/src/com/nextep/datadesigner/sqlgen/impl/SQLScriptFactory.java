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
package com.nextep.datadesigner.sqlgen.impl;

import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class SQLScriptFactory extends VersionableFactory {
	private static SQLScriptFactory instance;
	public SQLScriptFactory() {}
	public static VersionableFactory getInstance() {
		if(instance == null) {
			instance = new SQLScriptFactory();
		}
		return instance;
	}

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#createVersionable()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IVersionable createVersionable() {
		return new VersionedSQLScript();
	}

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#rawCopy(com.nextep.designer.vcs.model.IVersionable, com.nextep.designer.vcs.model.IVersionable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		ISQLScript d = (ISQLScript)destination;
		ISQLScript s = (ISQLScript)source;
		d.setScriptType(s.getScriptType());
		d.setSql(s.getSql());
		versionCopy(source, destination);
	}

}
