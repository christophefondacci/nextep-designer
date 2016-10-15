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
package com.nextep.designer.sqlclient.ui.jface;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.connectors.SQLResultConnector;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLRowModificationStatus;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;
import com.nextep.designer.sqlclient.ui.model.ISQLTypeSerializer;
import com.nextep.designer.sqlclient.ui.model.IStatusAccessor;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;

/**
 * The modifier for {@link ISQLRowResult} instances. This modifier supports enabling / disabling
 * modifications and dispatches database updates through the executor service as soon as a value has
 * been modified.
 * 
 * @author Christophe Fondacci
 */
public class SQLRowResultCellModifier implements ICellModifier {

	private boolean isModifiable;
	private TableViewer viewer;
	private ISQLQuery query;
	private IStatusAccessor statusAccessor;

	public SQLRowResultCellModifier(boolean isModifiable, TableViewer viewer,
			IStatusAccessor statusAccessor) {
		this.isModifiable = isModifiable;
		this.viewer = viewer;
		this.statusAccessor = statusAccessor;
	}

	public void setModifiable(boolean isModifiable) {
		this.isModifiable = isModifiable;
	}

	public void setSQLQuery(ISQLQuery query) {
		this.query = query;
	}

	@Override
	public boolean canModify(Object element, String property) {
		final ISQLRowResult result = (ISQLRowResult) element;
		final int modifyIndex = Integer.valueOf(property);
		return isModifiable && modifyIndex < result.getValues().size();
	}

	@Override
	public Object getValue(Object element, String property) {
		final int modifyIndex = Integer.valueOf(property);
		return SQLResultLabelProvider.getColumnText(element, modifyIndex);
	}

	private boolean hasChanged(ISQLRowResult row, int index, ISQLTypeSerializer serializer,
			Object newValue) {
		// Checking whether the value has changed
		final Object previousValue = row.getValues().get(index);
		String previousValueStr = null;
		if (serializer != null) {
			previousValueStr = serializer.serialize(previousValue);
		} else if (previousValue != null) {
			previousValueStr = previousValue.toString();
		}
		if (previousValueStr == null) {
			return newValue == null;
		} else {
			return !previousValueStr.equals(newValue);
		}
	}

	@Override
	public void modify(Object element, String property, Object value) {
		if (value != null && query != null) {
			final TableItem item = (TableItem) element;
			final ISQLRowResult row = (ISQLRowResult) item.getData();
			final int modifyIndex = Integer.valueOf(property);
			// Retrieving any deserializer for user input (@see ISQLTypeDeserializer)
			final TableColumn col = item.getParent().getColumn(modifyIndex);
			ISQLTypeSerializer serializer = null;
			if (col != null) {
				serializer = (ISQLTypeSerializer) col.getData(SQLResultConnector.KEY_DESERIALIZER);
			}
			if (hasChanged(row, modifyIndex, serializer, value)) {
				try {
					// Deserializing user value
					Object columnValue = serializer != null ? serializer
							.deserialize((String) value) : (String) value;
					// Updating database
					final ISQLRowModificationStatus status = SQLClientPlugin.getService(
							ISQLClientService.class).updateQueryValue(query, row, modifyIndex,
							columnValue);
					if (status.isModifiable()) {
						// If successful we publish to the model and refresh status / table item
						statusAccessor.setStatus("1 row updated", false);
						row.setValue(modifyIndex, columnValue);
					} else {
						if (row.isPending()) {
							statusAccessor
									.setStatus(
											"Row was NOT updated : all NOT NULL columns need values",
											false);
						} else {
							statusAccessor.setStatus(status.getMessage(), true);
						}
					}
					viewer.refresh(row);
				} catch (Exception e) {
					if (statusAccessor != null) {
						statusAccessor.setStatus(
								"Unable to update database value: " + e.getMessage(), true);
					}
				}
			} else {
				statusAccessor.setStatus(null, false);
			}
		}
	}
}
