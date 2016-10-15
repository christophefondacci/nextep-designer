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

import java.sql.Time;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.sqlclient.ui.SQLClientImages;
import com.nextep.designer.sqlclient.ui.SQLClientMessages;
import com.nextep.designer.sqlclient.ui.connectors.SQLResultConnector;
import com.nextep.designer.sqlclient.ui.helpers.ExportHelper;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;

/**
 * Label provider for SQL rows column contents
 * 
 * @author Christophe Fondacci
 */
public class SQLResultLabelProvider extends CellLabelProvider {

	// private List<Integer> columnWidths = new ArrayList<Integer>();
	// private Table table;
	//
	// private SQLResultLabelProvider(Table table) {
	// this.table = table;
	// }
	private int columnIndex;
	private SQLResultConnector connector;

	public SQLResultLabelProvider(SQLResultConnector connector, int columnIndex) {
		this.columnIndex = columnIndex;
		this.connector = connector;
	}

	private int getMetadataIndex() {
		return connector.getSQLQuery().getDisplayedColumnsCount();
	}

	@Override
	public String getToolTipText(Object element) {
		final int metadataIndex = getMetadataIndex();
		if (metadataIndex > 0 && element instanceof ISQLRowResult) {
			final ISQLRowResult row = (ISQLRowResult) element;
			final List<Object> values = row.getValues();
			if (values.size() > metadataIndex) {
				Object metaElt = row.getValues().get(columnIndex + getMetadataIndex());
				return MessageFormat
						.format(SQLClientMessages.getString("lblProvider.sqlResult.previousValue"), formatToString(metaElt)); //$NON-NLS-1$
			}
		}
		return super.getToolTipText(element);
	}

	@Override
	public void update(ViewerCell cell) {
		final Object elt = cell.getElement();
		final int colIndex = cell.getColumnIndex();
		final String text = getColumnText(elt, colIndex);
		cell.setText(text);
		final Image img = getColumnImage(elt, colIndex);
		cell.setImage(img);
		final Color background = getBackground(elt, colIndex);
		cell.setBackground(background);
	}

	private Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof ISQLRowResult) {
			final ISQLRowResult row = (ISQLRowResult) element;
			try {
				Object o = row.getValues().get(columnIndex);
				return o == null ? SQLClientImages.ICON_NULL : null;
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}
		return null;
	}

	public static String getColumnText(Object element, int columnIndex) {
		if (element instanceof ISQLRowResult) {
			final ISQLRowResult row = (ISQLRowResult) element;
			try {
				Object o = row.getValues().get(columnIndex);
				return formatToString(o);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}
		return null;
	}

	private static String formatToString(Object o) {
		String s = null;
		if (o instanceof Time) {
			s = ((Time) o).toString();
		} else if (o instanceof Date) {
			s = ExportHelper.formatDate((Date) o);
		} else {
			s = strVal(o);
		}
		return s;
	}

	// private void adjustWidth(String s, int columnIndex) {
	// Integer maxWidth = columnWidths.get(columnIndex);
	// if (maxWidth == null) {
	// final TableColumn column = table.getColumn(columnIndex);
	// int colNameSize = column.getText().length();
	// maxWidth = colNameSize * 8;
	// }
	// }

	protected static String strVal(Object o) {
		return o == null ? "" : notNull(o.toString()); //$NON-NLS-1$
	}

	protected static String notNull(String s) {
		return s == null ? "" : s; //$NON-NLS-1$
	}

	private Color getBackground(Object element, int index) {
		final int metadataIndex = getMetadataIndex();
		if (metadataIndex < 0) {
			if (element instanceof ISQLRowResult) {
				// We highlight pending rows in light yellow
				final ISQLRowResult row = (ISQLRowResult) element;
				return row.isPending() ? FontFactory.LIGHT_YELLOW : null;
			}
		} else {
			final ISQLRowResult row = (ISQLRowResult) element;
			if (row.getValues().size() > index + metadataIndex) {
				Object metaElt = row.getValues().get(index + metadataIndex);
				Object refElt = row.getValues().get(index);
				if ((metaElt == null && refElt != null)
						|| (metaElt != null && !metaElt.equals(refElt))) {
					return FontFactory.LIGHT_YELLOW;
				} else {
					return null;
				}
			}
		}
		return null;
	}

}
