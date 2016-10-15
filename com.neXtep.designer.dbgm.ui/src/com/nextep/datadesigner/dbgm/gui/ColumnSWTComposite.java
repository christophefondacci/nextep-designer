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
package com.nextep.datadesigner.dbgm.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class ColumnSWTComposite extends Composite {
	private Table columnsTable = null;
	private TableColumn deletionColumn = null;
	private TableColumn colNameColumn = null;
	private TableColumn colTypeColumn = null;
	private TableColumn colSizeColumn = null;
	private TableColumn colPrecisionColumn = null;
	private TableColumn colDescriptionColumn = null;
	private Button addNewButton = null;
	private Button upButton = null;
	private Button downButton = null;

	
	/**
	 * @return the columnsTable
	 */
	public Table getColumnsTable() {
		return columnsTable;
	}

	/**
	 * @return the addNewButton
	 */
	public Button getAddNewButton() {
		return addNewButton;
	}

	/**
	 * @return the upButton
	 */
	public Button getUpButton() {
		return upButton;
	}

	/**
	 * @return the downButton
	 */
	public Button getDownButton() {
		return downButton;
	}

	/**
	 * 
	 */
	public ColumnSWTComposite(Composite parent, int colSpan, SelectionListener selListener) {
		super(parent,SWT.NONE);
		// Setting a layout with no margin
		GridLayout globalLayout = new GridLayout();
		globalLayout.marginBottom=globalLayout.marginHeight=globalLayout.marginLeft=globalLayout.marginRight=
			globalLayout.marginTop=globalLayout.marginWidth=0;
		setLayout(globalLayout);
		GridData globalData = new GridData();
		if(colSpan > 1) {
			globalData.horizontalSpan = colSpan;
		}
		globalData.grabExcessVerticalSpace=true;
		globalData.grabExcessHorizontalSpace=true;
		globalData.horizontalAlignment = GridData.FILL;
		globalData.verticalAlignment = GridData.FILL;
		setLayoutData(globalData);
		
		// Building control
		Composite toolbox = new Composite(this,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginBottom=layout.marginHeight=layout.marginLeft=layout.marginRight=
			layout.marginTop=layout.marginWidth=0;
		layout.numColumns=3;
		toolbox.setLayout(layout);
		addNewButton = new Button(toolbox,SWT.PUSH);
		addNewButton.setImage(DBGMImages.ICON_NEWCOLUMN);
		addNewButton.setToolTipText("Add a new column");
		addNewButton.addSelectionListener(selListener);
		upButton = new Button(toolbox, SWT.PUSH);
		upButton.setImage(ImageFactory.ICON_UP_TINY);
		upButton.setToolTipText("Column order up");
		upButton.addSelectionListener(selListener);
		downButton = new Button(toolbox, SWT.PUSH);
		downButton.setImage(ImageFactory.ICON_DOWN_TINY);
		downButton.setToolTipText("Column order down");
		downButton.addSelectionListener(selListener);


		//Creating the table columns editor table
		createColumnsTable(this);
		//Creating the delete column
		createDeletionColumn();
		//Creating the column name column
		createColNameColumn();
		// Creating the column type column
		createColTypeColumn();
		// Creating the column size column
		createColSizeColumn();
		createColPrecisionColumn();

		// Creating the column description column
		createColDescriptionColumn();

	}

	private void createColDescriptionColumn() {
		colDescriptionColumn = new TableColumn(columnsTable, SWT.NONE);
		colDescriptionColumn.setWidth(70);
		colDescriptionColumn.setText("Description");
	}

	private void createColNameColumn() {
		colNameColumn = new TableColumn(columnsTable, SWT.NONE);
		colNameColumn.setWidth(150);
		colNameColumn.setText("Column Name");
	}

	private void createColPrecisionColumn() {
		colPrecisionColumn = new TableColumn(columnsTable, SWT.NONE);
		colPrecisionColumn.setWidth(60);
		colPrecisionColumn.setText("Precision");
		//colPrecisionColumn.setAlignment(SWT.CENTER);
	}

	private void createColSizeColumn() {
		colSizeColumn = new TableColumn(columnsTable, SWT.NONE);
		colSizeColumn.setWidth(50);
		//colSizeColumn.setAlignment(SWT.CENTER);
		colSizeColumn.setText("Length");
	}

	private void createColTypeColumn() {
		colTypeColumn = new TableColumn(columnsTable, SWT.NONE);
		colTypeColumn.setWidth(100);
		colTypeColumn.setText("Column Type");
	}

	private void createColumnsTable(Composite parent) {
		GridData gridData4 = new GridData();
		//gridData4.horizontalSpan = 1;
		gridData4.verticalAlignment = GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.grabExcessVerticalSpace = true;
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.minimumHeight=100;
		columnsTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		columnsTable.setHeaderVisible(true);
		columnsTable.setLayoutData(gridData4);
		columnsTable.setLinesVisible(true);
//		columnsTable.addMouseListener(this);

	}

	private void createDeletionColumn() {
		deletionColumn = new TableColumn(columnsTable, SWT.NONE);
		deletionColumn.setWidth(20);
		deletionColumn.setText("Del");
		deletionColumn.setResizable(false);
	}
}
