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

import com.nextep.datadesigner.dbgm.impl.BasicIndex;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 */
public class IndexFactory extends VersionableFactory {

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#createVersionable()
	 */
	@Override
	public IVersionable<?> createVersionable() {
		return new BasicIndex();
	}

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#rawCopy(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public void rawCopy(IVersionable<?> source, IVersionable<?> destination) {
		IIndex src = (IIndex) source.getVersionnedObject().getModel();
		IIndex tgt = (IIndex) destination.getVersionnedObject().getModel();
		tgt.setIndexedTableRef(src.getIndexedTableRef());
		IBasicTable t = (IBasicTable) VersionHelper.getReferencedItem(tgt.getIndexedTableRef());

		tgt.setIndexType(src.getIndexType());
		for (IReference colRef : src.getIndexedColumnsRef()) {
			tgt.addColumnRef(colRef);
		}
		versionCopy((IVersionable<IIndex>) source, (IVersionable<IIndex>) destination);
		// Copying function information
		for (IReference r : src.getIndexedColumnsRef()) {
			final String func = src.getFunction(r);
			if (func != null) {
				tgt.setFunction(r, func);
			}
		}
	}

}
