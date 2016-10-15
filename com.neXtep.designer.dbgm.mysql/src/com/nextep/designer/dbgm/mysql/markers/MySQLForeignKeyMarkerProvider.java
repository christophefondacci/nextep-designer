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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IDatabaseRawObject;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerProvider;
import com.nextep.designer.core.model.MarkerScope;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.dbgm.mysql.MySQLMessages;

/**
 * Provides consistency markers for MySQL foreign keys
 * 
 * @author Christophe Fondacci
 */
public class MySQLForeignKeyMarkerProvider implements IMarkerProvider {

	@Override
	public Collection<IMarker> getMarkersFor(ITypedObject o) {
		if (o instanceof ForeignKeyConstraint) {
			final ForeignKeyConstraint fk = (ForeignKeyConstraint) o;
			final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
			final List<IMarker> fkMarkers = new ArrayList<IMarker>();

			// Checks for validating indexes
			final Collection<IDatabaseRawObject> indexes = fk.getEnforcingIndex();
			if (indexes.isEmpty()) {
				fkMarkers.add(coreFactory.createMarker(fk, MarkerType.WARNING,
						MySQLMessages.getString("markers.fk.mysql.noEnforcingIndex"))); //$NON-NLS-1$
			}

			// Comparing columns between local and remote tables
			IKeyConstraint remoteConstraint = null;
			try {
				remoteConstraint = fk.getRemoteConstraint();
			} catch (ErrorException e) {
				final IMarker m = coreFactory.createMarker(fk, MarkerType.ERROR,
						"Unable to locate remote unique key because of the following problem : "
								+ e.getMessage());
				fkMarkers.add(m);
			}
			if (remoteConstraint != null) {
				Iterator<IBasicColumn> remoteColIt = remoteConstraint.getColumns().iterator();
				Iterator<IBasicColumn> constraintColIt = fk.getColumns().iterator();

				while (remoteColIt.hasNext()) {
					IBasicColumn remoteCol = remoteColIt.next();
					try {
						IBasicColumn constraintCol = constraintColIt.next();
						// Checking datatype compatibility
						if (remoteCol != null && constraintCol != null
								&& !remoteCol.getDatatype().equals(constraintCol.getDatatype())) {
							fkMarkers
									.add(coreFactory.createMarker(
											fk,
											MarkerType.ERROR,
											MessageFormat.format(
													MySQLMessages
															.getString("markers.fk.mysql.error.inconsistentDatatypes"), //$NON-NLS-1$
													fk.getConstrainedTable().getName(),
													constraintCol.getName(), remoteCol.getParent()
															.getName(), remoteCol.getName(),
													DBGMHelper.getDatatypeLabel(constraintCol
															.getDatatype()), DBGMHelper
															.getDatatypeLabel(remoteCol
																	.getDatatype()))));
						} else {
							// Comparing full data types between local and remote constrained
							// columns
							final String remoteColType = remoteCol.getDatatype().toString();
							final String fkColType = constraintCol.getDatatype().toString();
							if (!remoteColType.equals(fkColType)) {
								fkMarkers
										.add(coreFactory.createMarker(
												fk,
												MarkerType.WARNING,
												MessageFormat.format(
														MySQLMessages
																.getString("markers.fk.mysql.warning.inconsistentDatatypes"), //$NON-NLS-1$
														fk.getConstrainedTable().getName(),
														constraintCol.getName(), remoteCol
																.getParent().getName(), remoteCol
																.getName(), fkColType,
														remoteColType)));
							}
						}
					} catch (NoSuchElementException e) {
						// If constraint columns are less than remote columns, foreign key is not
						// consistent
						fkMarkers.add(coreFactory.createMarker(fk, MarkerType.ERROR,
								MySQLMessages.getString("markers.fk.mysql.tooFewColumns"))); //$NON-NLS-1$
					}
				}
				// Ensure that we have no more items in constraint either
				if (constraintColIt.hasNext()) {
					fkMarkers.add(coreFactory.createMarker(fk, MarkerType.ERROR,
							MySQLMessages.getString("markers.fk.mysql.tooManyColumns"))); //$NON-NLS-1$
				}
			}
			return fkMarkers;
		}
		return Collections.emptyList();
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
