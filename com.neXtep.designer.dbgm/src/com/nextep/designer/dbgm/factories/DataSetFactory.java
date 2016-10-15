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
package com.nextep.designer.dbgm.factories;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.impl.DataSet;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 */
public class DataSetFactory extends VersionableFactory {

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#createVersionable()
	 */
	@Override
	public IVersionable<?> createVersionable() {
		return new DataSet();
	}

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#rawCopy(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		IDataSet sourceSet = (IDataSet) source.getVersionnedObject().getModel();
		IDataSet destSet = (IDataSet) destination.getVersionnedObject().getModel();
		destSet.setTable(sourceSet.getTable());
		// Copying column definitions
		for (IBasicColumn c : sourceSet.getColumns()) {
			destSet.addColumnRef(c.getReference());
		}
		// Copying line content
		// for (IDataLine l : sourceSet.getDataLines()) {
		// destSet.addDataLine(FactoryUtil.copyDataLine(destSet, l));
		// }
		super.versionCopy(source, destination);

		destSet.setFileGenerated(sourceSet.isFileGenerated());
		destSet.setLoadingMethod(sourceSet.getLoadingMethod());
		destSet.setFieldsTermination(sourceSet.getFieldsTermination());
		destSet.setFieldsEnclosure(sourceSet.getFieldsEnclosure());
		destSet.setOptionalEnclosure(sourceSet.isOptionalEnclosure());
		destSet.setCurrentRowId(sourceSet.getCurrentRowId());
		// for (IRepositoryFile f : sourceSet.getDataFiles()) {
		// destSet.addDataFile(f);
		// }
		for (IReference r : sourceSet.getColumnsRef()) {
			final String mask = sourceSet.getColumnMask(r);
			if (mask != null) {
				destSet.setColumnMask(r, mask);
			}
		}
	}

}
