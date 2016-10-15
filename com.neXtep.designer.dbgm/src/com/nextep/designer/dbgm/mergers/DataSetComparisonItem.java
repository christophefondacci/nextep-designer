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
package com.nextep.designer.dbgm.mergers;

import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.designer.dbgm.model.IDataDelta;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.model.ComparisonScope;

/**
 * A little extension of a comparison result for data set so that this comparison can hold a
 * {@link IDataDelta} instance which acts as a pointer to the data lines comparison.
 * 
 * @author Christophe Fondacci
 */
public class DataSetComparisonItem extends ComparisonResult {

	private IDataDelta dataDelta;

	public DataSetComparisonItem(IDataSet source, IDataSet target, ComparisonScope scope) {
		super(source, target, scope);
	}

	public void setDataDelta(IDataDelta delta) {
		this.dataDelta = delta;
	}

	public IDataDelta getDataDelta() {
		return dataDelta;
	}
}
