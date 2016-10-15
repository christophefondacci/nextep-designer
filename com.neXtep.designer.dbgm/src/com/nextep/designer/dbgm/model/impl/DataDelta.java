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
package com.nextep.designer.dbgm.model.impl;

import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.model.IDataDelta;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * Default bean implementation of the {@link IDataDelta} interface
 * 
 * @author Christophe Fondacci
 */
public class DataDelta implements IDataDelta {

	private IDataSet addedDataSet, deletedDataSet, updatedDataSet;
	private IReference dataSetReference;
	private IVersionInfo fromVersion, toVersion;
	private DifferenceType differenceType;
	private boolean hasUpdates, hasInserts, hasDeletes;

	@Override
	public IDataSet getAddedDataSet() {
		return addedDataSet;
	}

	@Override
	public void setAddedDataSet(IDataSet addedDataSet) {
		this.addedDataSet = addedDataSet;
	}

	@Override
	public IDataSet getDeletedDataSet() {
		return deletedDataSet;
	}

	@Override
	public void setDeletedDataSet(IDataSet deletedDataSet) {
		this.deletedDataSet = deletedDataSet;
	}

	@Override
	public IDataSet getUpdatedDataSet() {
		return updatedDataSet;
	}

	@Override
	public void setUpdatedDataSet(IDataSet updatedDataSet) {
		this.updatedDataSet = updatedDataSet;
	}

	@Override
	public IReference getReference() {
		return dataSetReference;
	}

	@Override
	public void setReference(IReference dataSetReference) {
		this.dataSetReference = dataSetReference;
	}

	@Override
	public IVersionInfo getFromVersion() {
		return fromVersion;
	}

	@Override
	public void setFromVersion(IVersionInfo fromVersion) {
		this.fromVersion = fromVersion;
	}

	@Override
	public IVersionInfo getToVersion() {
		return toVersion;
	}

	@Override
	public void setToVersion(IVersionInfo toVersion) {
		this.toVersion = toVersion;
	}

	@Override
	public void computeDifferenceType(DifferenceType type) {

		if (differenceType == null) {
			differenceType = type;

		}
		switch (type) {
		case EQUALS:
			if (differenceType == null) {
				differenceType = type;
			}
			break;
		case MISSING_SOURCE:
			hasDeletes = true;
			switch (differenceType) {
			case EQUALS:
			case MISSING_TARGET:
			case DIFFER:
				differenceType = DifferenceType.DIFFER;
				break;
			default:
				differenceType = DifferenceType.MISSING_SOURCE;
				break;
			}
			break;
		case MISSING_TARGET:
			hasInserts = true;
			switch (differenceType) {
			case EQUALS:
			case MISSING_SOURCE:
			case DIFFER:
				differenceType = DifferenceType.DIFFER;
				break;
			default:
				differenceType = type;
				break;
			}
			break;
		case DIFFER:
			hasUpdates = true;
			break;
		}
	}

	@Override
	public DifferenceType getDifferenceType() {
		return differenceType == null ? DifferenceType.EQUALS : differenceType;
	}

	@Override
	public boolean hasAdditions() {
		return hasInserts;
	}

	@Override
	public boolean hasUpdates() {
		return hasUpdates;
	}

	@Override
	public boolean hasDeletions() {
		return hasDeletes;
	}
}
