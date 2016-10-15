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
package com.nextep.designer.dbgm.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.CheckBoxEditor;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.gui.impl.editors.TextColumnEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * An generic base class for all physical properties editors which implement the edition of common
 * elements.<br>
 * Specializations of IPhysicalProperties should extend this class to add specific attribute
 * edition.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PhysicalPropertiesEditor extends ControlledDisplayConnector {

	private Composite editor = null; // @jve:decl-index=0:visual-constraint="10,10"
	private Label tablespaceLabel = null;
	private Combo tablespaceCombo = null;
	private Table attrTable;
	private Label loggingLabel = null;
	private Label compressLabel = null;
	private Button loggingCheck = null;
	private Button compressCheck = null;

	private Label tableLabel = null;
	private Combo tableCombo = null;
	private boolean showParent = true;

	private boolean showAttributes, showLoggingCompress;

	public PhysicalPropertiesEditor(IPhysicalProperties props, ITypedObjectUIController controller,
			boolean showAttributes, boolean showLoggingCompress) {
		super(props, controller);
		this.showAttributes = showAttributes;
		this.showLoggingCompress = showLoggingCompress;
	}

	/**
	 * This method must be call before control creation to have an effect. Call it to hide the first
	 * parent read-only field. Should be used when this editor is embedded in another.
	 * 
	 * @param show
	 */
	public void setShowParent(boolean show) {
		this.showParent = show;
	}

	/**
	 * This method initializes tablespaceCombo
	 */
	private void createTablespaceCombo() {
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		tablespaceCombo = new Combo(editor, SWT.NONE);
		tablespaceCombo.setLayoutData(gridData);
		ComboEditor.handle(tablespaceCombo, ChangeEvent.TABLESPACE_CHANGED, this);
	}

	/**
	 * This method initializes tableCombo
	 */
	private void createTableCombo() {
		GridData gridData10 = new GridData();
		gridData10.horizontalAlignment = GridData.FILL;
		gridData10.horizontalSpan = 3;
		gridData10.grabExcessHorizontalSpace = true;
		gridData10.verticalAlignment = GridData.CENTER;
		tableCombo = new Combo(editor, SWT.READ_ONLY);
		tableCombo.setLayoutData(gridData10);
	}

	private void createLoggingCombo() {
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.minimumWidth = 150;
		loggingCheck = new Button(editor, SWT.CHECK);
		loggingCheck.setLayoutData(gridData);
		loggingCheck.setText("On");
		CheckBoxEditor.handle(loggingCheck, ChangeEvent.CUSTOM_5, this);
		// loggingCombo = new Combo(editor, SWT.READ_ONLY);
		// loggingCombo.setLayoutData(gridData);
		// loggingCombo.add("true");
		// loggingCombo.add("false");
		// ComboEditor.handle(loggingCombo, ChangeEvent.CUSTOM_5, this);
	}

	private void createCompressCombo() {
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.minimumWidth = 150;
		compressCheck = new Button(editor, SWT.CHECK);
		compressCheck.setLayoutData(gridData);
		compressCheck.setText("On");
		CheckBoxEditor.handle(compressCheck, ChangeEvent.CUSTOM_6, this);

	}

	@Override
	protected Control createSWTControl(Composite parent) {
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.FILL;
		gridData11.horizontalSpan = 2;
		gridData11.verticalAlignment = GridData.CENTER;
		GridData gridData9 = new GridData();
		gridData9.horizontalAlignment = GridData.FILL;
		gridData9.horizontalSpan = 2;
		gridData9.verticalAlignment = GridData.CENTER;
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = GridData.FILL;
		gridData8.verticalAlignment = GridData.CENTER;
		GridData gridData7 = new GridData();
		gridData7.horizontalAlignment = GridData.FILL;
		gridData7.verticalAlignment = GridData.CENTER;
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.FILL;
		gridData6.verticalAlignment = GridData.CENTER;
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.verticalAlignment = GridData.CENTER;
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.horizontalSpan = 2;
		gridData4.verticalAlignment = GridData.CENTER;
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.horizontalSpan = 2;
		gridData3.verticalAlignment = GridData.CENTER;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.horizontalSpan = 2;
		gridData2.verticalAlignment = GridData.CENTER;

		GridData gridData20 = new GridData();
		gridData20.horizontalAlignment = GridData.FILL;
		GridData gridData21 = new GridData();
		gridData21.horizontalAlignment = GridData.FILL;
		GridData gridData22 = new GridData();
		gridData22.horizontalAlignment = GridData.FILL;
		GridData gridData23 = new GridData();
		gridData23.horizontalAlignment = GridData.FILL;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(gridLayout);
		if (showParent) {
			tableLabel = new Label(editor, SWT.RIGHT);
			IPhysicalProperties props = (IPhysicalProperties) getModel();
			if (props != null && props.getParent() != null) {
				tableLabel.setText(props.getParent().getType().getName() + " : "); //$NON-NLS-1$
			} else {
				tableLabel.setText("Parent : ");
			}
			tableLabel.setLayoutData(gridData9);
			createTableCombo();
			new Label(editor, SWT.NONE);
		}
		tablespaceLabel = new Label(editor, SWT.RIGHT);
		tablespaceLabel.setText("Tablespace : ");
		tablespaceLabel.setLayoutData(gridData2);
		createTablespaceCombo();

		// new Label(editor, SWT.NONE);
		createSWTControlBeforeAttributes(editor);

		// Temporary handling attributes show / hide
		if (showAttributes) {
			attrTable = new Table(editor, SWT.BORDER | SWT.FULL_SELECTION);
			GridData attrData = new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1);
			attrData.minimumHeight = 200;
			attrTable.setLayoutData(attrData);
			attrTable.setLinesVisible(true);
			attrTable.setHeaderVisible(true);
			TableColumn attrNameCol = new TableColumn(attrTable, SWT.NONE);
			attrNameCol.setWidth(150);
			attrNameCol.setText("Storage attribute");
			TableColumn attrValueCol = new TableColumn(attrTable, SWT.NONE);
			attrValueCol.setWidth(70);
			attrValueCol.setText("Value");

			for (PhysicalAttribute attr : PhysicalAttribute.values()) {
				TableItem i = new TableItem(attrTable, SWT.NONE);
				i.setText(attr.name());
				i.setData(attr);
				attrTable.setData(attr.name(), i);
			}
			NextepTableEditor tabEditor = NextepTableEditor.handle(attrTable);
			TextColumnEditor.handle(tabEditor, 1, ChangeEvent.PHYSICAL_ATTR_CHANGED, this);
		}

		if (showLoggingCompress) {
			loggingLabel = new Label(editor, SWT.RIGHT);
			loggingLabel.setText("Logging : ");
			loggingLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			createLoggingCombo();
			new Label(editor, SWT.NONE);
			new Label(editor, SWT.NONE);
			new Label(editor, SWT.NONE);

			compressLabel = new Label(editor, SWT.RIGHT);
			compressLabel.setText("Compress : ");
			compressLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			createCompressCombo();
			new Label(editor, SWT.NONE);
			new Label(editor, SWT.NONE);
			new Label(editor, SWT.NONE);
		}

		// Creating partition pane
		createSWTPartitionPane();
		// Returning our main control
		return editor;
	}

	private void createSWTPartitionPane() {

	}

	/**
	 * Extend this method to add controls before the storage attributes table
	 * 
	 * @param editor SWT composite container
	 */
	protected void createSWTControlBeforeAttributes(Composite editor) {
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		IPhysicalProperties props = (IPhysicalProperties) getModel();
		if (showParent) {
			tableCombo.removeAll();
			tableCombo.add(notNull(props.getParent() != null ? props.getParent().getName()
					: "Orphaned")); //$NON-NLS-1$
			tableCombo.select(0);
		}
		tablespaceCombo.setText(notNull(props.getTablespaceName()));
		// Enablement
		boolean b = true;
		if (props.getParent() instanceof ILockable) {
			b = !((ILockable<?>) props.getParent()).updatesLocked();
		}
		tablespaceCombo.setEnabled(b);

		boolean partitioned = false;
		if (props instanceof IPartitionable) {
			partitioned = (((IPartitionable) props).getPartitioningMethod() != PartitioningMethod.NONE);
		}
		if (showAttributes) {
			for (PhysicalAttribute a : PhysicalAttribute.values()) {
				TableItem i = (TableItem) attrTable.getData(a.name());
				if (!partitioned) {
					i.setText(1, strVal(props.getAttribute(a)));
				} else {
					// We do not display attribute values for partitioned tables
					// since they will never be used. But they are not erased for
					// user convenience.
					i.setText(1, ""); //$NON-NLS-1$
				}
			}
			if (!partitioned) {
				attrTable.setEnabled(b);
			} else {
				attrTable.setEnabled(false);
			}
		}
		if (showLoggingCompress) {
			loggingCheck.setSelection(props.isLogging());
			compressCheck.setSelection(props.isCompressed());
			loggingCheck.setEnabled(b);
			compressCheck.setEnabled(b);
		}

	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IPhysicalProperties props = (IPhysicalProperties) getModel();
		try {
			switch (event) {
			case TABLESPACE_CHANGED:
				props.setTablespaceName((String) data);
				break;
			case PHYSICAL_ATTR_CHANGED:
				props.setAttribute((PhysicalAttribute) source,
						data == null || "".equals(((String) data).trim()) ? null : Integer //$NON-NLS-1$
								.parseInt((String) data));
				break;
			case CUSTOM_5:
				if (data != null) {
					props.setLogging((Boolean) data);
				}
				break;
			case CUSTOM_6:
				if (data != null) {
					props.setCompressed((Boolean) data);
				}
				break;
			default:
				refreshConnector();
				break;
			}
		} catch (NumberFormatException e) {
			throw new ErrorException("Invalid value, please specify an integer value.");
		}
	}

}
