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
package com.nextep.designer.dbgm.ui.jface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.model.IColumnBinding;
import com.nextep.designer.dbgm.ui.model.impl.ColumnBinding;

/**
 * @author Christophe Fondacci
 */
public class ForeignKeyColumnsContentProvider implements IStructuredContentProvider, IEventListener {

	private ForeignKeyConstraint foreignKey;
	private Viewer viewer;

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		Designer.getListenerService().unregisterListeners(this);
		if (newInput instanceof ForeignKeyConstraint) {
			foreignKey = (ForeignKeyConstraint) newInput;
		}
		this.viewer = viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
		final IMarkerService markerService = CorePlugin.getService(IMarkerService.class);
		final List<IColumnBinding> columnBindings = new ArrayList<IColumnBinding>();

		if (foreignKey == null) {
			return new Object[] {};
		} else if (foreignKey.getRemoteConstraintRef() == null) {
			return new Object[] {};
		} else {
			final IKeyConstraint remoteKey = foreignKey.getRemoteConstraint();
			final Collection<IMarker> fkMarkers = markerService.fetchMarkersFor(foreignKey);
			// Then we synchronize our 2 columns collection (remote and local) to add correspondent
			// items
			final Iterator<IBasicColumn> remoteIt = remoteKey.getColumns().iterator();
			final Iterator<IBasicColumn> fkIt = foreignKey.getColumns().iterator();
			// Looping on remote constraint columns
			while (remoteIt.hasNext()) {
				final IBasicColumn remoteColumn = remoteIt.next();
				// FK (right) mapped section setup (we may have no mapping)
				if (fkIt.hasNext()) {
					IBasicColumn tableColumn = fkIt.next();
					if (isMapped(foreignKey, tableColumn)) {
						// If we have a table mapping, we set the mapped column information
						Designer.getListenerService().registerListener(this, tableColumn, this);
						// Trying to extract the column marker from FK markers
						IMarker columnMarker = null;
						for (IMarker fkMarker : fkMarkers) {
							if (fkMarker.getMessage().contains(tableColumn.getName())) {
								columnMarker = fkMarker;
							}
						}
						columnBindings.add(new ColumnBinding(remoteColumn, tableColumn,
								columnMarker));
						// Checking datatype "perfect" match for warning
						// final String remoteColType = remoteColumn.getDatatype().toString();
						// final String fkColType = tableColumn.getDatatype().toString();
						// if (!remoteColType.equals(fkColType)) {
						// i.setText(2, MessageFormat.format(DBGMUIMessages
						// .getString("fk.editor.warnings.inconsistenDatatypes"),
						// remoteColType, fkColType));
						// i.setImage(2, UIImages.ICON_MARKER_WARNING);
						// } else {
						// i.setText(2, "");
						// i.setImage(2, null);
						// }
					} else {
						// Incorrect mapping, we display an error
						final IMarker marker = coreFactory.createMarker(foreignKey,
								MarkerType.ERROR,
								DBGMUIMessages.getString("fk.editor.unmappedColumn"));
						final IColumnBinding binding = new ColumnBinding(remoteColumn, null, marker);

						columnBindings.add(binding);
					}
				} else {
					// Incorrect mapping, we display an error
					final IMarker marker = coreFactory.createMarker(foreignKey, MarkerType.ERROR,
							DBGMUIMessages.getString("fk.editor.unmappedColumn"));
					final IColumnBinding binding = new ColumnBinding(remoteColumn, null, marker);

					columnBindings.add(binding);
				}
			}
		}
		return columnBindings.toArray();
	}

	/**
	 * Indicates whether the specified table column of a foreign key should be considered as an
	 * appropriate column mapping.
	 * 
	 * @param foreignKey foreign key on which consistency should be verified
	 * @param tableColumn table column to check (a column of the foreign key)
	 * @return <code>true</code> if the column is a valid mapping, else <code>false</code>
	 */
	private boolean isMapped(ForeignKeyConstraint foreignKey, IBasicColumn tableColumn) {
		return tableColumn != null && tableColumn.getParent() != null
				&& tableColumn.getParent().equals(foreignKey.getConstrainedTable());
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		viewer.refresh();
	}

}
