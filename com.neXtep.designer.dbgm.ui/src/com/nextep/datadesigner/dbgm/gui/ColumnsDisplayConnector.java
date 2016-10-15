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
/**
 *
 */
package com.nextep.datadesigner.dbgm.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.gui.impl.TableDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.helper.DatatypeHelper;

/**
 * @author Christophe Fondacci
 *
 */
public class ColumnsDisplayConnector extends TableDisplayConnector  {

    private List<IBasicColumn> columnsList;
    private Table columnsTable;
    private TableColumn columnHeader;
    private TableColumn columnDatatype;
    private String header;
    private IBasicColumn nextSelection;
    public ColumnsDisplayConnector(List<IBasicColumn> columnsList, String header) {
    	super(null,null);
        this.header=header;
        // Handling null list and instantiating new list
        this.columnsList=(columnsList == null ? new ArrayList<IBasicColumn>() : new ArrayList<IBasicColumn>(columnsList));
    }

    /**
     * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
     */
    protected Control createSWTControl(Composite parent) {
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.FILL;
        gridData3.grabExcessVerticalSpace = true;
        gridData3.grabExcessHorizontalSpace= true;
        gridData3.verticalAlignment = GridData.FILL;
        columnsTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        columnsTable.setLayoutData(gridData3);
        columnsTable.setHeaderVisible(true);
        columnHeader = new TableColumn(columnsTable,SWT.NONE);
        columnHeader.setText(header);
        columnHeader.setWidth(150);
        columnDatatype = new TableColumn(columnsTable,SWT.NONE);
        columnDatatype.setText("Datatype");
        columnDatatype.setWidth(100);

        // Registering table
        initializeTable(columnsTable, null);
        return columnsTable;
    }

    /**
     * @see com.nextep.datadesigner.gui.model.IDisplayConnector#focus(com.nextep.datadesigner.gui.model.IDisplayConnector)
     */
    public void focus(IDisplayConnector childFocus) {
        columnsTable.setFocus();

    }

    /**
     * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getSWTConnector()
     */
    public Control getSWTConnector() {
        return columnsTable;
    }

    /**
     * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getConnectorIcon()
     */
    public Image getConnectorIcon() {
        return DBGMImages.ICON_COLUMN_TYPE;
    }

    /**
     * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getTitle()
     */
    public String getTitle() {
        return "Custom columns display";
    }

    /**
     * A specific implementation of getModel which will return the selected
     * item in the columns table, or <code>null</code> if no table selection.
     *
     * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getModel()
     */
    public Object getModel() {
        TableItem[] selection = columnsTable.getSelection();
        if(selection.length>0) {
            return selection[0].getData();
        } else {
            return null;
        }
    }

    /**
     * @see com.nextep.datadesigner.gui.model.IDisplayConnector#refreshConnector()
     */
    public void refreshConnector() {
        for(IBasicColumn c : columnsList) {
            TableItem i = getOrCreateItem(c,columnsList.indexOf(c));
            if(c==null) {
            	i.setText("<Unresolved> column!");
            	continue;
            }
            i.setText(c.getName());
            i.setText(1,DBGMHelper.getDatatypeLabel(c.getDatatype()));
            i.setImage(DatatypeHelper.getDatatypeIcon(c.getDatatype(),true));
            if(nextSelection==c) {
            	columnsTable.select(columnsTable.indexOf(i));
            	nextSelection=null;
            }
        }
    }
//    private TableItem getOrCreateItem(IBasicColumn c) {
//        TableItem columnItem = columnsItems.get(c);
//        if(columnItem == null) {
//            columnItem = new TableItem(columnsTable,SWT.NONE,columnsList.indexOf(c));
//            columnItem.setData(c);
//            columnsItems.put(c, columnItem);
//        }
//        return columnItem;
//    }


    /**
     * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
     */
    public void handleEvent(ChangeEvent event, IObservable source, Object data) {
        switch(event) {
        case SET_COLUMNS:
        	if(columnsTable.getSelection().length>0) {
        		nextSelection = (IBasicColumn)columnsTable.getSelection()[0].getData();
        	}
        	this.clean(columnsTable);
            //Instantiating new list
            if(data!=null) {
            	columnsList = new ArrayList<IBasicColumn>((Collection<IBasicColumn>)data);
            } else {
            	columnsList = new ArrayList<IBasicColumn>();
            }

            break;
        case COLUMN_ADDED:
            if(data==null) {
                columnsList.add((IBasicColumn)source);
            } else {
            	columnsList.add((Integer)data,(IBasicColumn)source);
            }
            nextSelection = (IBasicColumn)source;
            break;
        case COLUMN_REMOVED:
            IBasicColumn c = (IBasicColumn)source;
            TableItem i = this.getTableItem(c);
            // Removing previous item
            if(i!= null) {
                // Managing next selection
                int index = columnsTable.indexOf(i);
                if(index<columnsTable.getItemCount()-1) {
                	nextSelection = (IBasicColumn)columnsTable.getItem(index+1).getData();
                } else if(index>0) {
                	nextSelection = (IBasicColumn)columnsTable.getItem(index-1).getData();
                }
                // Then disposing
                this.removeTableItem(c);
            }
            columnsList.remove(c);

            break;
        }
        refreshConnector();
    }

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(Object model) {
		// This connector is not attached to a model

	}

}
