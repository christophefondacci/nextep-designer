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
package com.nextep.designer.dbgm.mysql.impl;

import java.util.HashMap;
import java.util.Map;
import com.nextep.datadesigner.dbgm.impl.BasicIndex;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.mysql.model.IMySQLIndex;

/**
 * Implementation of the mysql-speficic index features
 * 
 * @author Christophe Fondacci
 */
public class MySQLIndex extends BasicIndex implements IMySQLIndex {

	private Map<IReference, MySQLIndexProperties> colPropsMap;

	public MySQLIndex() {
		colPropsMap = new HashMap<IReference, MySQLIndexProperties>();
	}

	@Override
	public Integer getColumnPrefixLength(IReference indexColumn) {
		final MySQLIndexProperties props = colPropsMap.get(indexColumn);
		if (props != null) {
			return props.getPrefixLength();
		}
		return null;
	}

	@Override
	public void setColumnPrefixLength(IReference indexColumn, Integer prefixLength) {
		MySQLIndexProperties props = colPropsMap.get(indexColumn.getReference());
		if (props == null) {
			props = new MySQLIndexProperties();
			colPropsMap.put(indexColumn, props);
		}
		final Integer beforeVal = props.getPrefixLength();
		props.setPrefixLength(prefixLength);
		notifyIfChanged(beforeVal, prefixLength, ChangeEvent.MODEL_CHANGED);
	}

	protected void setColumnPropertiesMap(Map<IReference, MySQLIndexProperties> propertiesMap) {
		this.colPropsMap = propertiesMap;
	}

	protected Map<IReference, MySQLIndexProperties> getColumnPropertiesMap() {
		return colPropsMap;
	}

	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		boolean isUpdated = false;
		if (colPropsMap.keySet().contains(oldRef)) {
			final MySQLIndexProperties props = colPropsMap.get(oldRef);
			colPropsMap.remove(oldRef);
			colPropsMap.put(newRef, props);
			isUpdated = true;
		}
		final boolean isParentUpdated = super.updateReferenceDependencies(oldRef, newRef);
		return isUpdated || isParentUpdated;
	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		super.checkConsistency();
		// Mysql-specific consistency check

		// Spatial indexes only available for MyIsam tables
		if (getIndexType() == IndexType.SPATIAL) {

		}
	}
}
