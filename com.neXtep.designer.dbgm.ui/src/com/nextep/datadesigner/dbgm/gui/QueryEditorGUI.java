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
///**
// * Copyright (c) 2008 neXtep Softwares.
// * All rights reserved. Terms of the neXtep licence
// * are available at http://www.nextep-softwares.com
// */
//package com.nextep.datadesigner.dbgm.gui;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.swt.widgets.TableItem;
//
//import com.nextep.datadesigner.dbgm.model.IBasicColumn;
//import com.nextep.datadesigner.dbgm.model.IBasicTable;
//import com.nextep.datadesigner.gui.impl.ImageFactory;
//import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
//import com.nextep.datadesigner.model.ChangeEvent;
//import com.nextep.datadesigner.model.IElementType;
//import com.nextep.datadesigner.model.IObservable;
//import com.nextep.datadesigner.vcs.services.VersionHelper;
//import com.nextep.designer.dbgm.sql.ColumnAlias;
//import com.nextep.designer.dbgm.sql.ISelectStatement;
//import com.nextep.designer.dbgm.sql.SelectStatement;
//import com.nextep.designer.dbgm.sql.TableAlias;
//import com.nextep.designer.vcs.model.IVersionable;
//
///**
// * @author Christophe Fondacci
// *
// */
//public class QueryEditorGUI extends ControlledDisplayConnector implements SelectionListener {
//	private Composite editor = null;  //  @jve:decl-index=0:visual-constraint="10,10"
//	private Table eligibleTablesTable = null;
//	private Button addTableButton = null;
//	private Button removeTableButton = null;
//	private Table tablesTable = null;
//	private Table eligibleColsTable = null;
//	private Table selectedColsTable = null;
//	private Button addColButton = null;
//	private Button removeColButton = null;
//	private Label selectCollabel = null;
//	private Label selectResultLabel = null;
////	private StyledText sqlText = null;
//	private Map<String,IBasicTable> tablesMap;
//	private Map<String,IBasicColumn> columnsMap;
//	
//	public QueryEditorGUI(String query) {
//		super(new SelectStatement(query),null);
//		tablesMap = new HashMap<String, IBasicTable>();
//		columnsMap = new HashMap<String, IBasicColumn>();
//	}
//	/**
//	 * This method initializes sShell
//	 */
//	private void createEditor(Composite parent) {
//
//		GridData gridData15 = new GridData();
//		gridData15.horizontalSpan = 2;
//		GridData gridData14 = new GridData();
//		gridData14.grabExcessVerticalSpace = true;
//		gridData14.verticalAlignment = GridData.BEGINNING;
//		gridData14.horizontalAlignment = GridData.CENTER;
//		GridData gridData13 = new GridData();
//		gridData13.grabExcessVerticalSpace = true;
//		gridData13.verticalAlignment = GridData.END;
//		gridData13.horizontalAlignment = GridData.CENTER;
//		GridData gridData12 = new GridData();
//		gridData12.grabExcessVerticalSpace = true;
//		gridData12.verticalAlignment = GridData.BEGINNING;
//		gridData12.horizontalAlignment = GridData.CENTER;
//		GridData gridData11 = new GridData();
//		gridData11.horizontalAlignment = GridData.CENTER;
//		gridData11.grabExcessVerticalSpace = true;
//		gridData11.verticalAlignment = GridData.END;
//		GridData gridData10 = new GridData();
//		gridData10.horizontalAlignment = GridData.FILL;
//		gridData10.horizontalSpan = 4;
//		gridData10.grabExcessHorizontalSpace = true;
//		gridData10.grabExcessVerticalSpace = true;
//		gridData10.verticalAlignment = GridData.FILL;
//		gridData10.minimumHeight=100;
//		GridData gridData9 = new GridData();
//		gridData9.horizontalSpan = 4;
//		GridData gridData7 = new GridData();
//		gridData7.horizontalAlignment = GridData.FILL;
//		gridData7.verticalSpan = 2;
//		gridData7.grabExcessVerticalSpace = true;
//		gridData7.verticalAlignment = GridData.FILL;
//		GridData gridData4 = new GridData();
//		gridData4.horizontalAlignment = GridData.FILL;
//		gridData4.verticalSpan = 2;
//		gridData4.grabExcessVerticalSpace = true;
//		gridData4.horizontalSpan = 2;
//		gridData4.verticalAlignment = GridData.FILL;
//		gridData4.minimumHeight=150;
//		GridData gridData1 = new GridData();
//		gridData1.verticalSpan = 2;
//		gridData1.verticalAlignment = GridData.FILL;
//		gridData1.grabExcessHorizontalSpace = true;
//		gridData1.grabExcessVerticalSpace = true;
//		gridData1.horizontalAlignment = GridData.FILL;
//		GridData gridData = new GridData();
//		gridData.verticalSpan = 2;
//		gridData.verticalAlignment = GridData.FILL;
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.grabExcessVerticalSpace = true;
//		gridData.horizontalSpan = 2;
//		gridData.horizontalAlignment = GridData.FILL;
//		gridData.minimumHeight=150;
//		GridLayout gridLayout = new GridLayout();
//		gridLayout.numColumns = 4;
//		editor = new Composite(parent,SWT.NONE);
//		editor.setLayout(gridLayout);
////		editor.setSize(new Point(592, 299));
//		eligibleTablesTable = new Table(editor, SWT.NONE);
//		eligibleTablesTable.setHeaderVisible(true);
//		eligibleTablesTable.setLayoutData(gridData);
//		eligibleTablesTable.setLinesVisible(true);
//		TableColumn tableColumn = new TableColumn(eligibleTablesTable, SWT.NONE);
//		tableColumn.setWidth(160);
//		tableColumn.setText("Eligible tables");
//		TableColumn tableColumn3 = new TableColumn(eligibleTablesTable, SWT.NONE);
//		tableColumn3.setWidth(130);
//		tableColumn3.setText("Container");
//		addTableButton = new Button(editor, SWT.NONE);
//		addTableButton.setLayoutData(gridData11);
//		addTableButton.setImage(ImageFactory.ICON_RIGHT_TINY);
//		addTableButton.addSelectionListener(this);
//		tablesTable = new Table(editor, SWT.NONE);
//		tablesTable.setHeaderVisible(true);
//		tablesTable.setLayoutData(gridData1);
//		tablesTable.setLinesVisible(true);
//		tablesTable.addSelectionListener(this);
//		TableColumn tableColumn1 = new TableColumn(tablesTable, SWT.NONE);
//		tableColumn1.setWidth(160);
//		tableColumn1.setText("Selected tables");
//		TableColumn tableColumn2 = new TableColumn(tablesTable, SWT.NONE);
//		tableColumn2.setWidth(60);
//		tableColumn2.setText("Alias");
//		removeTableButton = new Button(editor, SWT.NONE);
//		removeTableButton.setLayoutData(gridData12);
//		removeTableButton.setImage(ImageFactory.ICON_LEFT_TINY);
//		removeTableButton.addSelectionListener(this);
//		selectCollabel = new Label(editor, SWT.NONE);
//		selectCollabel.setText("Select a table from 'selected tables' list to view eligible columns and add it to the view columns");
//		selectCollabel.setLayoutData(gridData9);
//		eligibleColsTable = new Table(editor, SWT.NONE);
//		eligibleColsTable.setHeaderVisible(true);
//		eligibleColsTable.setLayoutData(gridData4);
//		eligibleColsTable.setLinesVisible(true);
//		addColButton = new Button(editor, SWT.NONE);
//		addColButton.setLayoutData(gridData13);
//		addColButton.setImage(ImageFactory.ICON_RIGHT_TINY);
//		addColButton.addSelectionListener(this);
//		selectedColsTable = new Table(editor, SWT.NONE);
//		selectedColsTable.setHeaderVisible(true);
//		selectedColsTable.setLayoutData(gridData7);
//		selectedColsTable.setLinesVisible(true);
//		removeColButton = new Button(editor, SWT.NONE);
//		removeColButton.setLayoutData(gridData14);
//		removeColButton.setImage(ImageFactory.ICON_LEFT_TINY);
//		removeColButton.addSelectionListener(this);
//		selectResultLabel = new Label(editor, SWT.NONE);
//		selectResultLabel.setText("Resulting SQL statement :");
//		Label filler = new Label(editor, SWT.NONE);
//		Label filler1 = new Label(editor, SWT.NONE);
////		sqlText = new StyledText(editor, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
////		sqlText.setLayoutData(gridData10);
////		ColorFocusListener.handle(sqlText);
////		StyledTextEditor.handle(sqlText, ChangeEvent.SOURCE_CHANGED, this);
//		TableColumn tableColumn8 = new TableColumn(selectedColsTable, SWT.NONE);
//		tableColumn8.setWidth(180);
//		tableColumn8.setText("Selected columns");
//
//		TableColumn fromTabColumn = new TableColumn(selectedColsTable,SWT.NONE);
//		fromTabColumn.setWidth(100);
//		fromTabColumn.setText("From table");
//		TableColumn tableColumn5 = new TableColumn(eligibleColsTable, SWT.NONE);
//		tableColumn5.setWidth(160);
//		tableColumn5.setText("Eligible columns");
//		TableColumn tableColumn6 = new TableColumn(eligibleColsTable, SWT.NONE);
//		tableColumn6.setWidth(130);
//		tableColumn6.setText("Description");
//		initEligibleTables();
//
//	}
//	private void initEligibleTables() {
//		ISelectStatement stmt = (ISelectStatement)getModel();
//		List<IVersionable<?>> tables = VersionHelper.getAllVersionables(VersionHelper.getCurrentView(), IElementType.getInstance(IBasicTable.TYPE_ID));
//		for(IVersionable<?> v : tables) {
//			IBasicTable t = (IBasicTable)v.getVersionnedObject().getModel();
//			TableItem i = new TableItem(eligibleTablesTable,SWT.NONE);
//			i.setText(notNull(t.getName()));
//			i.setText(1,notNull(v.getContainer().getName()));
//			i.setData(t);
//			tablesMap.put(t.getName(), t);
//		}
//		List<TableAlias> tabs = stmt.getFromTables();
//		Map<String,IBasicTable> tabAliasMap = new HashMap<String, IBasicTable>();
//		List<IBasicTable> allTables = new ArrayList<IBasicTable>();
//		for(TableAlias a : tabs) {
//			TableItem i = new TableItem(tablesTable,SWT.NONE);
//			i.setText(a.getTableName());
//			i.setText(1,notNull(a.getTableAlias()));
//			IBasicTable t = tablesMap.get(a.getTableName());
//			i.setData(t);
//			a.setTable(t);
//			tabAliasMap.put(a.getTableAlias(), t);
//			allTables.add(t);
//		}
//		List<ColumnAlias> cols = stmt.getSelectedColumns();
////		Iterator<String> aliasIt = ((IView)getModel()).getColumnAliases().iterator();
//		for(ColumnAlias c : cols) {
//			TableItem i = new TableItem(selectedColsTable,SWT.NONE);
//			i.setText(notNull(c.getColumnName()));
//			// Retrieving parent table
//			IBasicTable t = null;
//			if(c.getTableAlias()!=null) {
//				t=tabAliasMap.get(c.getTableAlias());
//				for(IBasicColumn col : t.getColumns()) {
//					if(col.getName().equals(c.getColumnName())) {
//						c.setColumn(col);
//					}
//				}
//			} else {
//				for(IBasicTable tab : allTables) {
//					for(IBasicColumn col : tab.getColumns()) {
//						if(col.getName().equals(c.getColumnName())) {
//							c.setColumn(col);
//							t=tab;
//							break;
//						}
//					}
//				}
//			}
//			// Setting column text
//			i.setText(1,t==null ? "<unparsable>" : t.getName());
//			i.setData(c.getColumn());
////			if(aliasIt.hasNext()) {
////				i.setText(1,aliasIt.next());
////			}
////			
//		}
//	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
//	 */
//	@Override
//	public Control createSWTControl(Composite parent) {
//		createEditor(parent);
//		return editor;
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
//	 */
//	@Override
//	public Control getSWTConnector() {
//		return editor;
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
//	 */
//	@Override
//	public void refreshConnector() {
//		ISelectStatement stmt = (ISelectStatement)getModel();
////		sqlText.setText(notNull(stmt.getSQL()));
//
//	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
//	 */
//	@Override
//	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
//		ISelectStatement stmt = (ISelectStatement)getModel();
//		switch(event) {
//		case SOURCE_CHANGED:
//			stmt.setSQL((String)data);
//			break;
//		case MODEL_CHANGED:
//			refreshConnector();
//			break;
//		}
//	}
//
//	/**
//	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
//	 */
//	@Override
//	public void widgetDefaultSelected(SelectionEvent e) {
//		widgetSelected(e);
//	}
//	/**
//	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
//	 */
//	@Override
//	public void widgetSelected(SelectionEvent e) {
//		ISelectStatement stmt = (ISelectStatement)getModel();
//		if(e.getSource() == addTableButton) {
//			TableItem[] sel = eligibleTablesTable.getSelection();
//			if(sel.length>0) {
//				IBasicTable t = (IBasicTable)sel[0].getData();
//				TableItem i = addFromTable(t);
//				tablesTable.setSelection(i);
//				TableAlias ta = new TableAlias(t.getName());
//				if(t.getShortName()!=null) {
//					ta.setAlias(t.getShortName());
//				}
//				stmt.addFromTable(ta);
//				refreshTableSelection();
//			}
//		} else if( e.getSource() == removeTableButton) {
//			TableItem[] sel = tablesTable.getSelection();
//			if(sel.length>0) {
//				IBasicTable t = (IBasicTable)sel[0].getData();
//				sel[0].dispose();
//				stmt.removeFromTable(t.getName(), t.getShortName());
//			}
//			
//		} else if( e.getSource() == tablesTable) {
//			refreshTableSelection();
//		} else if( e.getSource() == addColButton) {
//			TableItem[] sel = eligibleColsTable.getSelection();
//			if(sel.length>0) {
//				IBasicColumn c = (IBasicColumn)sel[0].getData();
//				TableItem i = new TableItem(selectedColsTable,SWT.NONE);
//				i.setText(c.getName());
//				i.setText(1,c.getParent().getName());
//				i.setData(c);
//				selectedColsTable.setSelection(i);
//				ColumnAlias a = new ColumnAlias();
//				a.setColumnName(c.getName());
//				a.setTableAlias(c.getParent().getShortName());
//				stmt.addSelectedColumn(a);
//			}
//		} else if( e.getSource() == removeColButton) {
//			TableItem[] sel = selectedColsTable.getSelection();
//			if(sel.length>0) {
//				IBasicColumn c = (IBasicColumn)sel[0].getData();
//				stmt.removeSelectedColumn(c.getName(),null);
//				sel[0].dispose();
//			}
//		}
////		((IView)getModel()).setSQLDefinition(stmt.getSQL());
//		refreshConnector();
//	}
//	
//	private TableItem addFromTable(IBasicTable t ) {
//		TableItem i = new TableItem(tablesTable,SWT.NONE);
//		i.setText(t.getName());
//		i.setText(1,notNull(t.getShortName()));
//		i.setData(t);
//		return i;
//	}
//	private void refreshTableSelection() {
//		TableItem[] sel = tablesTable.getSelection();
//		if(sel.length>0) {
//			IBasicTable t = (IBasicTable)sel[0].getData();
//			// FIXME Handle view selection
//			if(t ==null) return;
//			eligibleColsTable.removeAll();
//			for(IBasicColumn c : t.getColumns()) {
//				TableItem i = new TableItem(eligibleColsTable,SWT.NONE);
//				i.setText(c.getName());
//				i.setText(1,notNull(c.getDescription()));
//				i.setData(c);
//			}
//		}
//	}
//	public Table getSelectedColsTable() {
//		return selectedColsTable;
//	}
//
//}
