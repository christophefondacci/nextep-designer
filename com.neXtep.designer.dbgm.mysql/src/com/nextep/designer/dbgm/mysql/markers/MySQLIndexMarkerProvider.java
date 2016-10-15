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
package com.nextep.designer.dbgm.mysql.markers;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerProvider;
import com.nextep.designer.core.model.MarkerScope;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.dbgm.mysql.MySQLMessages;
import com.nextep.designer.dbgm.mysql.model.IMySQLIndex;
import com.nextep.designer.dbgm.mysql.model.IMySQLTable;

public class MySQLIndexMarkerProvider implements IMarkerProvider {

	private final static Collection<String> ALLOWED_PREFIX_DATATYPES = Arrays.asList("CHAR", //$NON-NLS-1$
			"VARCHAR", "BINARY", "VARBINARY", "TINYBLOB", "BLOB", "MEDIUMBLOB", "LONGBLOB", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"TINYTEXT", "TEXT", "MEDIUMTEXT", "LONGTEXT", "GEOMETRY"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private final static Collection<String> REQUIRED_PREFIX_DATATYPES = Arrays.asList("TINYBLOB", //$NON-NLS-1$
			"BLOB", "MEDIUMBLOB", "LONGBLOB", "TINYTEXT", "TEXT", "MEDIUMTEXT", "LONGTEXT"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

	@Override
	public Collection<IMarker> getMarkersFor(ITypedObject o) {
		final Collection<IMarker> markers = new LinkedList<IMarker>();
		if (o instanceof IMySQLIndex) {
			final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
			final IMySQLIndex index = (IMySQLIndex) o;
			final Collection<IBasicColumn> cols = index.getColumns();
			// Checking prefix-length integrity
			for (IBasicColumn col : cols) {
				final Integer prefix = index.getColumnPrefixLength(col.getReference());
				if (prefix != null && prefix.intValue() > 0) {
					if (!isColumnValidForPrefix(col)) {
						markers.add(coreFactory.createMarker(index, MarkerType.WARNING,
								MySQLMessages
										.getString("markers.index.mysql.prefixDatatypeWarning"))); //$NON-NLS-1$
					}
				} else if (REQUIRED_PREFIX_DATATYPES.contains(col.getDatatype().getName())) {
					markers.add(coreFactory.createMarker(index, MarkerType.ERROR,
							MySQLMessages.getString("markers.index.mysql.prefixMandatory"))); //$NON-NLS-1$
				}
			}
			// Checking index type requirements
			final IndexType indexType = index.getIndexType();
			if (indexType == IndexType.SPATIAL) {

				if (!isTableStorageEngine(index, "MYISAM")) { //$NON-NLS-1$
					markers.add(coreFactory.createMarker(index, MarkerType.WARNING,
							MySQLMessages.getString("markers.index.mysql.spatialEngineWarning"))); //$NON-NLS-1$
				}
			} else if (indexType == IndexType.HASH) {
				if (!isTableStorageEngine(index, "MEMORY")) { //$NON-NLS-1$
					markers.add(coreFactory.createMarker(index, MarkerType.WARNING,
							MySQLMessages.getString("markers.index.mysql.hashEngineWarning"))); //$NON-NLS-1$
				}
			}
		}
		return markers;
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void invalidate(Object o) {
	}

	@Override
	public MarkerScope getProvidedMarkersScope() {
		return MarkerScope.CONSISTENCY;
	}

	private boolean isColumnValidForPrefix(IBasicColumn c) {
		return ALLOWED_PREFIX_DATATYPES.contains(c.getDatatype().getName());
	}

	private boolean isTableStorageEngine(IMySQLIndex index, String engine) {
		// Retrieving table storage engine
		IMySQLTable t = (IMySQLTable) index.getIndexedTable();
		final String tabEngine = t.getEngine();
		return tabEngine != null && engine.toUpperCase().equals(tabEngine.toUpperCase());
	}
}
