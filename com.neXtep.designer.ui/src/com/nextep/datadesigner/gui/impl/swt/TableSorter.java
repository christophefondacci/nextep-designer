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
package com.nextep.datadesigner.gui.impl.swt;

import java.text.Collator;
import java.util.Locale;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableSorter implements Listener {

	private Table table;
	private TableSorter(Table table) {
		this.table=table;
	}
	public static void handle(Table table) {
		TableSorter s = new TableSorter(table);
		for(TableColumn c : table.getColumns()) {
			c.addListener(SWT.Selection,s);
		}
	}
	@Override
	public void handleEvent(Event event) {
		TableItem[] items = table.getItems();
        Collator collator = Collator.getInstance(Locale.getDefault());
        TableColumn column = (TableColumn) event.widget;
        int sortOrder = SWT.UP;
        if(table.getSortColumn()==column) {
        	// Inverting order
        	sortOrder = table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
        }
        int index = table.indexOf(column);
        for (int i = 1; i < items.length; i++) {
          String value1 = items[i].getText(index);
          for (int j = 0; j < i; j++) {
            String value2 = items[j].getText(index);
            if (sortOrder == SWT.UP ? collator.compare(value1==null ? "" : value1, value2 == null ? "": value2) < 0 : collator.compare(value1, value2) > 0) {
            	final int colCount = table.getColumnCount();
            	String[] colVals = new String[colCount];
            	for(int k =0 ; k < colCount ; k++) {
            		colVals[k] = items[i].getText(k);
            	}
            	Object data = items[i].getData();
            	Image img = items[i].getImage();
              items[i].dispose();
              TableItem item = new TableItem(table, SWT.NONE, j);
              item.setText(colVals);
              item.setData(data);
              item.setImage(img);
              items = table.getItems();
              break;
            }
          }
        }
        table.setSortColumn(column);
        table.setSortDirection(sortOrder);

	}

}
