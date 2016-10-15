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
package com.nextep.designer.sqlgen.mysql.impl;

import java.util.Collection;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerProvider;
import com.nextep.designer.core.model.MarkerScope;
import com.nextep.designer.core.model.MarkerType;

public class MySQLErrorsMarkerProvider extends Observable implements IMarkerProvider {

	private static MySQLErrorsMarkerProvider instance;
	private static final Log log = LogFactory.getLog(MySQLErrorsMarkerProvider.class);
	private MultiValueMap markersMap;

	public MySQLErrorsMarkerProvider() {
		instance = this;
		markersMap = new MultiValueMap();
	}

	public static MySQLErrorsMarkerProvider getInstance() {
		return instance;
	}

	// @SuppressWarnings("unchecked")
	// @Override
	// public Collection<IMarker> getMarkers() {
	// return markersMap.values();
	// }

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IMarker> getMarkersFor(ITypedObject o) {
		return markersMap.getCollection(o);
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void invalidate(Object o) {
	}

	public void addErrorMarker(ITypedObject object, String errorLine) {
		final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
		// Building marker
		final IMarker marker = coreFactory.createMarker(object, MarkerType.ERROR, errorLine);
		final int index = errorLine.indexOf("line ");
		int line = 0;
		if (index > -1) {
			final int end = errorLine.indexOf(' ', index + 5);
			if (end > 0) {
				final String lineNbStr = errorLine.substring(index + 5, end);
				try {
					line = Integer.valueOf(lineNbStr);
				} catch (NumberFormatException e) {
					log.error("Line error", e);
				}
			}
		}
		marker.setAttribute(IMarker.ATTR_LINE, line);
		markersMap.put(object, marker);
	}

	@Override
	public MarkerScope getProvidedMarkersScope() {
		return MarkerScope.DEFAULT;
	}

}
