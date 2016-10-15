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
package com.nextep.designer.sqlgen.gui.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.gui.impl.editors.ComboColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionContainer;

/**
 * @author Christophe Fondacci
 *
 */
public class PrefixContainerMatcher extends WizardDisplayConnector {

	private static final String NEW_TEXT = "<Create new container>";
	private Map<String,IVersionContainer> prefixMap;
	private Composite dialog = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Label introLabel = null;
	private Table prefixTable = null;
	private TableColumn prefixCol = null;
	private TableColumn containerCol = null;

	public PrefixContainerMatcher(Map<String,IVersionContainer> prefixMap) {
		super("PrefixContainerMatcher","Matching database prefixes with repository containers",null);
		this.prefixMap = prefixMap;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createSWTControl(Composite parent) {
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.verticalAlignment = GridData.FILL;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		dialog = new Composite(parent,SWT.NONE);
//		sShell.setText("Shell");
//		sShell.setSize(new Point(413, 200));
		dialog.setLayout(new GridLayout());
		addNoMarginLayout(dialog, 1, false);
		introLabel = new Label(dialog, SWT.NONE);
		introLabel.setText("Please match database objects prefix with repository containers.");
		introLabel.setLayoutData(gridData);
		prefixTable = new Table(dialog, SWT.NONE);
		prefixTable.setHeaderVisible(true);
		prefixTable.setLayoutData(gridData1);
		prefixTable.setLinesVisible(true);
		prefixCol = new TableColumn(prefixTable,SWT.NONE);
		prefixCol.setText("Prefix");
		prefixCol.setWidth(80);
		containerCol = new TableColumn(prefixTable,SWT.NONE);
		containerCol.setText("Repository container");
		containerCol.setWidth(300);

		// Handling table editors
		NextepTableEditor editor = NextepTableEditor.handle(prefixTable);
		List<Object> containers = new ArrayList<Object>(VersionHelper.getAllVersionables(VersionHelper.getCurrentView(), IElementType.getInstance("CONTAINER")));
		containers.add(NEW_TEXT);
		ComboColumnEditor.handle(editor,1,ChangeEvent.CONTAINER_CHANGED,this,containers);

		// Creating lines
		for(final String prefix : prefixMap.keySet()) {
			TableItem i = new TableItem(prefixTable,SWT.NONE);
			i.setText( prefix);
			i.setData( new NamedObservable() {
				public String getName() {
					return prefix;
				}
			});
			if(prefixMap.get(prefix) != null) {
				i.setText(1,prefixMap.get(prefix).getName());
			} else {
				i.setText(1,NEW_TEXT);
			}
		}
		return dialog;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		super.refreshConnector();
		for(TableItem i : prefixTable.getItems()) {
			if(prefixMap.get(i.getText())!=null) {
				i.setText(1,prefixMap.get(i.getText()).getName());
			} else {
				i.setText(1,NEW_TEXT);
			}
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getModel()
	 */
	@Override
	public Object getModel() {
		return prefixMap;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return dialog;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case CONTAINER_CHANGED:
			if(data instanceof IVersionContainer) {
				prefixMap.put(((INamedObject)source).getName(), (IVersionContainer)data);
			} else {
				prefixMap.put(((INamedObject)source).getName(), null);
			}
			break;
		}
		refreshConnector();
	}

}
