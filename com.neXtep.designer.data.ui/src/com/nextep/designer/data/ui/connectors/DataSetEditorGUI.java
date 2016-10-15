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
package com.nextep.designer.data.ui.connectors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.dbgm.gui.ColumnsDisplayConnector;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.LoadingMethod;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.editors.ButtonEditor;
import com.nextep.datadesigner.gui.impl.editors.TextColumnEditor;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.gui.external.VersionedTableEditor;
import com.nextep.datadesigner.vcs.services.VCSFiles;
import com.nextep.designer.data.ui.controllers.DataSetUIController;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IRepositoryFile;

/**
 * @author Christophe Fondacci
 */
public class DataSetEditorGUI extends ControlledDisplayConnector implements SelectionListener {

	private Composite editor; // virtual parent
	private Composite workarea = null;
	private Button downButton = null;
	private Button upButton = null;
	private Composite dataSetComposite = null;
	private CLabel nameLabel = null;
	private Text nameText = null;
	private CLabel descriptionLabel = null;
	private Text descriptionText = null;
	private CLabel cLabel = null;
	private Combo tableCombo = null;

	private IDisplayConnector columnEditor;
	private IDisplayConnector setColumnEditor;

	private TableColumn maskCol;

	// File-based section
	private Composite fileBasedEditor;
	private Table fileBasedList;
	private Button addFileButton;
	private Button removeFileButton;

	// File-generation section
	private Composite fileEditor;
	private Button fileDataSetButton;
	private Button insertLoadTypeRadio;
	private Button replaceLoadTypeRadio;
	private Button appendLoadTypeRadio;
	private Button truncateLoadTypeRadio;
	private Text terminationText;
	private Text enclosedText;
	private Button optionallyEnclosed;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// private Button importFileButton;
	public DataSetEditorGUI(IDataSet dataSet, DataSetUIController controller) {
		super(dataSet, controller);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		addNoMarginLayout(editor, 1);
		editor.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		createDataSetComposite();
		createWorkarea();
		return workarea;
	}

	/**
	 * This method initializes workarea
	 */
	private void createWorkarea() {
		SashForm colSash = new SashForm(editor, SWT.VERTICAL);
		GridData colSashData = new GridData(SWT.FILL, SWT.FILL, true, false);
		colSashData.heightHint = 150;
		colSashData.minimumHeight = 150;
		colSash.setLayoutData(colSashData);
		// gridData2.heightHint=400;
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.END;
		gridData11.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.makeColumnsEqualWidth = false;
		gridLayout.marginBottom = gridLayout.marginHeight = gridLayout.marginLeft = gridLayout.marginRight = gridLayout.marginTop = gridLayout.marginWidth = 0;
		GridData colData = new GridData();
		colData.horizontalAlignment = GridData.FILL;
		colData.grabExcessHorizontalSpace = true;
		colData.horizontalSpan = 1;
		colData.verticalSpan = 2;
		colData.verticalAlignment = GridData.FILL;
		colData.heightHint = 50;
		colData.minimumHeight = 50;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;

		workarea = new Composite(colSash, SWT.NONE);
		workarea.setLayoutData(gridData);
		workarea.setLayout(gridLayout);

		IDataSet dataSet = (IDataSet) getModel();
		columnEditor = new ColumnsDisplayConnector(dataSet.getTable().getColumns(),
				"Eligible Columns");
		Control c = columnEditor.create(workarea);
		for (IBasicColumn col : dataSet.getColumns()) {
			columnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, col, null);
		}
		c.setLayoutData(colData);

		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.verticalAlignment = GridData.END;
		gridData5.grabExcessVerticalSpace = true;
		downButton = new Button(workarea, SWT.NONE);
		downButton.setLayoutData(gridData5);
		downButton.setImage(ImageFactory.ICON_RIGHT_TINY);
		downButton.addSelectionListener(this);

		setColumnEditor = new ColumnsDisplayConnector(dataSet.getColumns(), "Dataset Columns");
		c = setColumnEditor.create(workarea);
		GridData setColData = new GridData();
		setColData.horizontalAlignment = GridData.FILL;
		setColData.grabExcessHorizontalSpace = true;
		setColData.horizontalSpan = 1;
		setColData.verticalSpan = 2;
		setColData.verticalAlignment = GridData.FILL;
		setColData.heightHint = 50;
		setColData.minimumHeight = 50;
		c.setLayoutData(setColData);
		setColumnEditor.refreshConnector();

		// Adding column mask column
		// And adding a column for function definition
		maskCol = new TableColumn((Table) c, SWT.NONE);
		maskCol.setWidth(200);
		maskCol.setText("Mask");
		NextepTableEditor editor = VersionedTableEditor.handle((Table) c, getModel());
		TextColumnEditor.handle(editor, ((Table) c).indexOf(maskCol), ChangeEvent.CUSTOM_8, this);
		editor.setVersionedParent(getModel());

		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.FILL;
		gridData6.verticalAlignment = GridData.BEGINNING;
		gridData6.grabExcessVerticalSpace = true;
		upButton = new Button(workarea, SWT.NONE);
		upButton.setLayoutData(gridData6);
		upButton.setImage(ImageFactory.ICON_LEFT_TINY);
		upButton.addSelectionListener(this);

		// // Creating the data set file component
		// SashForm form = new SashForm(colSash, SWT.HORIZONTAL);
		// GridData formData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		// formData.heightHint = 300;
		// formData.minimumHeight = 250;
		// form.setLayoutData(formData);
		// createDataFileSection(form);
		// createImportFileSection(form);
		// form.setWeights(new int[] {3,4});
		// downButton = new Button(workarea, SWT.NONE);
		// downButton.setLayoutData(gridData11);
		// upButton = new Button(workarea, SWT.NONE);
		// createUpDownButtons();

	}

	private void createDataFileSection(Composite parent) {
		fileEditor = new Composite(parent, SWT.BORDER);
		fileEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		fileEditor.setLayout(new GridLayout(4, false));

		fileDataSetButton = new Button(fileEditor, SWT.CHECK);
		fileDataSetButton.setText(DBGMUIMessages.getString("datasetFileCheck"));
		fileDataSetButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		ButtonEditor.handle(fileDataSetButton, ChangeEvent.CUSTOM_1, this);
		// Spacing filler
		Label filler = new Label(fileEditor, SWT.NONE);
		filler.setText("    ");
		// Options
		Label loadTypeLbl = new Label(fileEditor, SWT.RIGHT);
		loadTypeLbl.setText(DBGMUIMessages.getString("datasetLoadingMethod"));
		loadTypeLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		insertLoadTypeRadio = new Button(fileEditor, SWT.RADIO);
		insertLoadTypeRadio.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		insertLoadTypeRadio.setText(LoadingMethod.INSERT.getLabel());
		ButtonEditor.handle(insertLoadTypeRadio, ChangeEvent.CUSTOM_2, this);
		replaceLoadTypeRadio = new Button(fileEditor, SWT.RADIO);
		replaceLoadTypeRadio.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		replaceLoadTypeRadio.setText(LoadingMethod.REPLACE.getLabel());
		ButtonEditor.handle(replaceLoadTypeRadio, ChangeEvent.CUSTOM_3, this);
		new Label(fileEditor, SWT.NONE);
		new Label(fileEditor, SWT.NONE);
		appendLoadTypeRadio = new Button(fileEditor, SWT.RADIO);
		appendLoadTypeRadio.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		appendLoadTypeRadio.setText(LoadingMethod.APPEND.getLabel());
		ButtonEditor.handle(appendLoadTypeRadio, ChangeEvent.CUSTOM_12, this);
		truncateLoadTypeRadio = new Button(fileEditor, SWT.RADIO);
		truncateLoadTypeRadio.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		truncateLoadTypeRadio.setText(LoadingMethod.TRUNCATE.getLabel());
		ButtonEditor.handle(replaceLoadTypeRadio, ChangeEvent.CUSTOM_13, this);

		// Termination
		new Label(fileEditor, SWT.NONE);
		Label terminationLbl = new Label(fileEditor, SWT.RIGHT);
		terminationLbl.setText(DBGMUIMessages.getString("datasetFieldTermination"));
		terminationLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		terminationText = new Text(fileEditor, SWT.BORDER);
		terminationText.setTextLimit(1);
		GridData tData = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
		tData.widthHint = 25;
		terminationText.setLayoutData(tData);
		ColorFocusListener.handle(terminationText);
		TextEditor.handle(terminationText, ChangeEvent.CUSTOM_10, this);
		new Label(fileEditor, SWT.NONE);

		// Enclosure
		new Label(fileEditor, SWT.NONE);
		Label enclosedLbl = new Label(fileEditor, SWT.RIGHT);
		enclosedLbl.setText(DBGMUIMessages.getString("datasetFieldEnclosure"));
		enclosedLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		enclosedText = new Text(fileEditor, SWT.BORDER);
		enclosedText.setTextLimit(1);
		GridData eData = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
		eData.widthHint = 25;
		enclosedText.setLayoutData(eData);
		TextEditor.handle(enclosedText, ChangeEvent.CUSTOM_11, this);
		ColorFocusListener.handle(enclosedText);
		optionallyEnclosed = new Button(fileEditor, SWT.CHECK);
		optionallyEnclosed.setText(DBGMUIMessages.getString("datasetFieldEnclosedOptional"));
		ButtonEditor.handle(optionallyEnclosed, ChangeEvent.CUSTOM_4, this);

	}

	private void createImportFileSection(final Composite parent) {
		fileBasedEditor = new Composite(parent, SWT.BORDER);
		fileBasedEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		fileBasedEditor.setLayout(new GridLayout(1, false));

		final Label fileBasedDesc = new Label(fileBasedEditor, SWT.WRAP);
		GridData lblData = new GridData(SWT.FILL, SWT.FILL, true, false);
		lblData.widthHint = 100;
		lblData.minimumWidth = 100;
		Composite toolbar = new Composite(fileBasedEditor, SWT.NONE);
		addNoMarginLayout(toolbar, 2);
		addFileButton = new Button(toolbar, SWT.PUSH);
		addFileButton.setImage(DBGMImages.ICON_ADD_FILE);
		addFileButton.setToolTipText(DBGMUIMessages.getString("datasetAddFileTooltip"));

		removeFileButton = new Button(toolbar, SWT.PUSH);
		removeFileButton.setImage(DBGMImages.ICON_DEL_FILE);
		removeFileButton.setToolTipText(DBGMUIMessages.getString("datasetDelFileTooltip"));
		fileBasedDesc.setLayoutData(lblData);
		fileBasedDesc.setText(DBGMUIMessages.getString("datasetFileBasedDesc"));
		fileBasedList = new Table(fileBasedEditor, SWT.BORDER | SWT.FULL_SELECTION);
		fileBasedList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fileBasedList.setHeaderVisible(true);
		fileBasedList.setLinesVisible(true);
		fileBasedList.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				TableItem[] i = fileBasedList.getSelection();
				if (i.length > 0 && i[0].getData() instanceof IRepositoryFile) {
					final IRepositoryFile f = (IRepositoryFile) i[0].getData();
					UIControllerFactory.getController(f).defaultOpen(f);
				}
			}
		});
		TableColumn fileNameCol = new TableColumn(fileBasedList, SWT.NONE);
		fileNameCol.setWidth(150);
		fileNameCol.setText("Filename");
		TableColumn fileSizeCol = new TableColumn(fileBasedList, SWT.NONE);
		fileSizeCol.setWidth(50);
		fileSizeCol.setText("Size");
		TableColumn dateCol = new TableColumn(fileBasedList, SWT.NONE);
		dateCol.setWidth(100);
		dateCol.setText("Import date");

		addFileButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(parent.getShell());
				fd.setFilterExtensions(new String[] { "*.csv", ".dat" });
				fd.setText("Pick a file to import in repository");
				String filePath = fd.open();
				if (filePath == null)
					return;
				IRepositoryFile f = VCSFiles.getInstance().createFromLocalFile(filePath);
				IDataSet dataSet = (IDataSet) getModel();
				dataSet.addDataFile(f);
			}
		});
		removeFileButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] sel = fileBasedList.getSelection();
				if (sel.length > 0) {
					if (sel[0].getData() instanceof IRepositoryFile) {
						((IDataSet) getModel()).removeDataFile((IRepositoryFile) sel[0].getData());
					}
				}
			}
		});
	}

	// /**
	// * Creates the up & down buttons which transfer
	// * eligible items to key columns
	// */
	// private void createUpDownButtons() {
	// GridData gridData5 = new GridData();
	// gridData5.horizontalAlignment = GridData.END;
	// gridData5.verticalAlignment = GridData.CENTER;
	// gridData5.grabExcessHorizontalSpace=true;
	// downButton = new Button(workarea, SWT.NONE);
	// downButton.setLayoutData(gridData5);
	// downButton.setImage(ImageFactory.ICON_DOWN_TINY);
	// downButton.addSelectionListener(this);
	// GridData gridData6 = new GridData();
	// gridData6.horizontalAlignment = GridData.BEGINNING;
	// gridData6.verticalAlignment = GridData.CENTER;
	// gridData6.grabExcessHorizontalSpace=true;
	// upButton = new Button(workarea, SWT.NONE);
	// upButton.setLayoutData(gridData6);
	// upButton.setImage(ImageFactory.ICON_UP_TINY);
	// upButton.addSelectionListener(this);
	// // new Label(keysGroup,SWT.NONE);
	// }
	/**
	 * This method initializes dataSetComposite
	 */
	private void createDataSetComposite() {
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = GridData.FILL;
		gridData8.verticalAlignment = GridData.CENTER;
		GridData gridData7 = new GridData();
		gridData7.grabExcessHorizontalSpace = true;
		gridData7.verticalAlignment = GridData.CENTER;
		gridData7.horizontalAlignment = GridData.FILL;
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.FILL;
		gridData6.verticalAlignment = GridData.CENTER;
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.grabExcessHorizontalSpace = true;
		gridData5.verticalAlignment = GridData.CENTER;
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.verticalAlignment = GridData.FILL;
		dataSetComposite = new Composite(editor, SWT.NONE);
		dataSetComposite.setLayoutData(gridData3);
		dataSetComposite.setLayout(gridLayout1);
		nameLabel = new CLabel(dataSetComposite, SWT.RIGHT);
		nameLabel.setText("Name of this data set : ");
		nameLabel.setLayoutData(gridData4);
		nameText = new Text(dataSetComposite, SWT.BORDER);
		nameText.setLayoutData(gridData5);
		ColorFocusListener.handle(nameText);
		TextEditor.handle(nameText, ChangeEvent.NAME_CHANGED, this);
		descriptionLabel = new CLabel(dataSetComposite, SWT.RIGHT);
		descriptionLabel.setText("Description : ");
		descriptionLabel.setLayoutData(gridData6);
		descriptionText = new Text(dataSetComposite, SWT.BORDER);
		descriptionText.setLayoutData(gridData7);
		ColorFocusListener.handle(descriptionText);
		TextEditor.handle(descriptionText, ChangeEvent.DESCRIPTION_CHANGED, this);
		cLabel = new CLabel(dataSetComposite, SWT.RIGHT);
		cLabel.setText("Related table : ");
		cLabel.setLayoutData(gridData8);
		createTableCombo();
	}

	/**
	 * This method initializes tableCombo
	 */
	private void createTableCombo() {
		GridData gridData9 = new GridData();
		gridData9.horizontalAlignment = GridData.FILL;
		gridData9.grabExcessHorizontalSpace = true;
		gridData9.verticalAlignment = GridData.CENTER;
		tableCombo = new Combo(dataSetComposite, SWT.READ_ONLY);
		tableCombo.setEnabled(true);
		IDataSet dataSet = (IDataSet) getModel();
		if (dataSet.getTable() != null) {
			tableCombo.add(dataSet.getTable().getName());
		}
		tableCombo.setToolTipText("Once set, this information cannot be changed");
		tableCombo.setLayoutData(gridData9);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return DBGMImages.ICON_DATASET;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return workarea;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		return ((INamedObject) getModel()).getName() + " Data set editor";
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		IDataSet dataSet = (IDataSet) getModel();
		nameText.setText(dataSet.getName() == null ? "" : dataSet.getName());
		descriptionText.setText(dataSet.getDescription() == null ? "" : dataSet.getDescription());
		tableCombo.select(0);// (dataSet.getTable() == null ? "" : dataSet.getTable().getName());
		columnEditor.refreshConnector();

		// // Updating datafiles section
		// fileBasedList.removeAll();
		// for (IRepositoryFile file : dataSet.getDataFiles()) {
		// TableItem i = new TableItem(fileBasedList, SWT.NONE);
		// i.setImage(DBGMImages.ICON_FILE);
		// i.setText(file.getName());
		// i.setText(1, String.valueOf(file.getFileSizeKB()) + "K");
		// i.setText(2, dateFormat.format(file.getImportDate()));
		// i.setData(file);
		// }
		final Table colsTable = (Table) setColumnEditor.getSWTConnector();
		for (TableItem i : colsTable.getItems()) {
			if (i.getData() instanceof IBasicColumn) {
				final String mask = dataSet.getColumnMask(((IBasicColumn) i.getData())
						.getReference());
				i.setText(colsTable.indexOf(maskCol), notNull(mask));
			}
		}
		// // Updating file section
		// fileDataSetButton.setSelection(dataSet.isFileGenerated()
		// || !dataSet.getDataFiles().isEmpty());
		// insertLoadTypeRadio.setSelection(false);
		// replaceLoadTypeRadio.setSelection(false);
		// appendLoadTypeRadio.setSelection(false);
		// truncateLoadTypeRadio.setSelection(false);
		// switch (dataSet.getLoadingMethod()) {
		// case INSERT:
		// insertLoadTypeRadio.setSelection(true);
		// break;
		// case REPLACE:
		// replaceLoadTypeRadio.setSelection(true);
		// break;
		// case TRUNCATE:
		// truncateLoadTypeRadio.setSelection(true);
		// break;
		// case APPEND:
		// appendLoadTypeRadio.setSelection(true);
		// }
		// terminationText.setText(notNull(dataSet.getFieldsTermination()));
		// enclosedText.setText(notNull(dataSet.getFieldsEnclosure()));
		// optionallyEnclosed.setSelection(dataSet.isOptionalEnclosure());
		//
		// boolean fileEnab = dataSet.isFileGenerated() && !dataSet.updatesLocked();
		// insertLoadTypeRadio.setEnabled(fileEnab);
		// replaceLoadTypeRadio.setEnabled(fileEnab);
		// appendLoadTypeRadio.setEnabled(fileEnab);
		// truncateLoadTypeRadio.setEnabled(fileEnab);
		// terminationText.setEnabled(fileEnab);
		// enclosedText.setEnabled(fileEnab);
		// optionallyEnclosed.setEnabled(fileEnab);

		boolean enab = !dataSet.updatesLocked();
		// addFileButton.setEnabled(enab);
		// removeFileButton.setEnabled(enab);
		// fileDataSetButton.setEnabled(enab);
		nameText.setEnabled(enab);
		descriptionText.setEnabled(enab);
		tableCombo.setEnabled(enab);
		upButton.setEnabled(enab);
		downButton.setEnabled(enab);
		// if (!dataSet.getDataFiles().isEmpty()) {
		// fileDataSetButton.setEnabled(false);
		// }
		// linesEditor.getSWTConnector().setEnabled(enab);
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IDataSet dataSet = (IDataSet) getModel();
		switch (event) {
		case NAME_CHANGED:
			dataSet.setName((String) data);
			break;
		case DESCRIPTION_CHANGED:
			dataSet.setDescription((String) data);
			break;
		case COLUMN_ADDED:
			columnAdded((IBasicColumn) data);
			break;
		case COLUMN_REMOVED:
			columnRemoved((IBasicColumn) data);
			break;
		case CUSTOM_1:
			dataSet.setFileGenerated(fileDataSetButton.getSelection());
			break;
		case CUSTOM_2:
			dataSet.setLoadingMethod(LoadingMethod.INSERT);
			break;
		case CUSTOM_3:
			dataSet.setLoadingMethod(LoadingMethod.REPLACE);
			break;
		case CUSTOM_12:
			dataSet.setLoadingMethod(LoadingMethod.APPEND);
			break;
		case CUSTOM_13:
			dataSet.setLoadingMethod(LoadingMethod.TRUNCATE);
			break;
		case CUSTOM_8:
			final IBasicColumn c = (IBasicColumn) source;
			dataSet.setColumnMask(c.getReference(), (String) data);
			break;
		case CUSTOM_10:
			dataSet.setFieldsTermination(terminationText.getText());
			break;
		case CUSTOM_11:
			dataSet.setFieldsEnclosure(enclosedText.getText());
			break;
		case CUSTOM_4:
			dataSet.setOptionalEnclosure(optionallyEnclosed.getSelection());
			break;
		case GENERIC_CHILD_ADDED:
			if (data instanceof IRepositoryFile && !dataSet.isFileGenerated()) {
				dataSet.setFileGenerated(true);
			} else {
				refreshConnector();
			}
			break;
		case MODEL_CHANGED:
		default:
			refreshConnector();
			break;
		}

	}

	private void columnAdded(IBasicColumn c) {
		columnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, c, null);
		setColumnEditor.handleEvent(ChangeEvent.COLUMN_ADDED, c, null);
	}

	private void columnRemoved(IBasicColumn c) {
		columnEditor.handleEvent(ChangeEvent.COLUMN_ADDED, c, null);
		setColumnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, c, null);
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		IDataSet dataSet = (IDataSet) getModel();
		if (e.getSource() == downButton) {
			// Retrieving column selection
			IBasicColumn c = (IBasicColumn) columnEditor.getModel();
			if (c != null) {
				dataSet.addColumn(c);
				// columnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, c, null);
			}
		} else if (e.getSource() == upButton) {
			IBasicColumn c = (IBasicColumn) setColumnEditor.getModel();
			if (c != null) {
				// Note that this may fail
				dataSet.removeColumn(c);
			}
		}
	}

}
