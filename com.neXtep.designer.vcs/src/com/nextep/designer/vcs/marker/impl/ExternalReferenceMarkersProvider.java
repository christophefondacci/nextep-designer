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
package com.nextep.designer.vcs.marker.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerProvider;
import com.nextep.designer.core.model.MarkerScope;
import com.nextep.designer.core.model.MarkerType;

/**
 * This marker provider checks every {@link IReferencer} instance : for every dependent reference,
 * it tries to resolve it and build an error marker when resolution fails.
 * 
 * @author Christophe Fondacci
 */
public class ExternalReferenceMarkersProvider extends Observable implements IMarkerProvider {

	@Override
	public Collection<IMarker> getMarkersFor(ITypedObject o) {
		final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
		// Preparing our markers map
		Collection<IMarker> markers = Collections.emptyList();
		// We only check referencers : we need to resolve each reference dependency
		if (o instanceof IReferencer) {
			final IReferencer referencer = (IReferencer) o;
			// References which needs to be checked
			try {
				// Initializing markers
				markers = new LinkedList<IMarker>();
				final Collection<IReference> references = referencer.getReferenceDependencies();
				for (IReference r : references) {
					if (r != null) {
						try {
							VersionHelper.getReferencedItem(r);
						} catch (ErrorException e) {
							markers.add(coreFactory.createMarker(referencer, MarkerType.ERROR,
									e.getMessage()));
						}
					}
				}
			} catch (RuntimeException e) {
				markers.add(coreFactory.createMarker(referencer, MarkerType.ERROR, e.getMessage()));
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
}
