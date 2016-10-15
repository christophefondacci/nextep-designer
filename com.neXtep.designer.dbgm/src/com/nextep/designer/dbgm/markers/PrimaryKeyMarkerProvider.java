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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.markers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.DBGMMessages;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerProvider;
import com.nextep.designer.core.model.MarkerScope;
import com.nextep.designer.core.model.MarkerType;

/**
 * This class provides warning markers on primary keys which have NULLABLE columns.
 * 
 * @author Christophe Fondacci
 */
public class PrimaryKeyMarkerProvider implements IMarkerProvider {

	private static final Log LOGGER = LogFactory.getLog(PrimaryKeyMarkerProvider.class);

	@Override
	public Collection<IMarker> getMarkersFor(ITypedObject o) {
		if (o instanceof UniqueKeyConstraint) {
			final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
			// Retrieving table's primary key
			final UniqueKeyConstraint uk = (UniqueKeyConstraint) o;
			final Collection<IMarker> markers = new ArrayList<IMarker>();
			if (uk != null && uk.getConstraintType() == ConstraintType.PRIMARY) {
				boolean isPKMarked = false;
				// Browsing PK columns
				for (IReference colRef : uk.getConstrainedColumnsRef()) {
					try {
						final IBasicColumn c = (IBasicColumn) VersionHelper
								.getReferencedItem(colRef);
						// We check whether the column is NOT NULL, if not we raise a warning
						if (!c.isNotNull()) {
							if (!isPKMarked) {
								// Marking a warning on the PK, only once since there could be more
								// than one problematic column
								final IMarker marker = coreFactory.createMarker(
										uk,
										MarkerType.ERROR,
										MessageFormat.format(
												DBGMMessages.getString("markers.pk.pkNotNull"), //$NON-NLS-1$
												getPrimaryKeyName(uk)));
								markers.add(marker);
								isPKMarked = true;
							}
							// Marking a warning on every NULLABLE columns of the PK
							final IMarker marker = coreFactory.createMarker(
									c,
									MarkerType.WARNING,
									MessageFormat.format(
											DBGMMessages.getString("markers.pk.colNotNull"), //$NON-NLS-1$
											getColumnName(c)));
							markers.add(marker);
						}
					} catch (ErrorException e) {
						LOGGER.warn(MessageFormat.format(
								DBGMMessages.getString("markers.pk.computeError"), //$NON-NLS-1$
								getPrimaryKeyName(uk)));
					}
				}
			}
			return markers;
		}
		return Collections.emptyList();
	}

	private String getColumnName(IBasicColumn c) {
		return c.getParent().getName() + "." + c.getName(); //$NON-NLS-1$
	}

	private String getPrimaryKeyName(UniqueKeyConstraint uk) {
		return uk.getConstrainedTable().getName() + "." + uk.getName(); //$NON-NLS-1$
	}

	@Override
	public void invalidate() {
		// Nothing to do
	}

	@Override
	public void invalidate(Object o) {
		// Nothing to do
	}

	@Override
	public MarkerScope getProvidedMarkersScope() {
		return MarkerScope.CONSISTENCY;
	}

}
