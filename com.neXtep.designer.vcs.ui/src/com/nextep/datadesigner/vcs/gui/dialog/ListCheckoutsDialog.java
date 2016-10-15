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
package com.nextep.datadesigner.vcs.gui.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.gui.impl.TableDisplayConnector;
import com.nextep.datadesigner.gui.impl.swt.TableSorter;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.services.ICoreVersioningService;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 */
public class ListCheckoutsDialog extends TableDisplayConnector implements SelectionListener {

	// private Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,10"
	private Composite group = null;
	private CLabel listLabel = null;
	private Text fromText = null;
	private Table checkoutsTable = null;
	private TableColumn nameCol = null;
	private TableColumn typeCol = null;
	private TableColumn releaseCol = null;
	private TableColumn dateCol = null;
	private TableColumn statusCol = null;
	private TableColumn userCol = null;
	private TableColumn branchCol = null;
	private Button selectAllButton = null;
	private Button unselectAllButton = null;
	private Composite actionsComposite = null;
	private Button checkInButton = null;
	private Button undoCheckoutButton = null;
	private List<IVersionable<?>> checkoutList;
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy/MM/dd hh:mm:ss");
	private boolean readOnly;
	private String introText;
	private int heightHint;

	public ListCheckoutsDialog(IVersionContainer container, List<IVersionable<?>> checkouts) {
		super(container, UIControllerFactory.getController(IElementType.getInstance("VERSIONABLE")));
		this.checkoutList = checkouts;
		this.readOnly = false;
		this.heightHint = 400;
	}

	public ListCheckoutsDialog(IVersionContainer container, List<IVersionable<?>> checkouts,
			boolean readOnly, String introText, int heightHint) {
		this(container, checkouts);
		this.readOnly = readOnly;
		this.introText = introText;
		this.heightHint = heightHint;
	}

	public void initializeGUI(Composite parent) {
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.FILL;
		gridData11.verticalAlignment = GridData.CENTER;
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.verticalAlignment = GridData.CENTER;
		GridData gridData2 = new GridData();
		gridData2.horizontalSpan = 3;
		gridData2.verticalAlignment = GridData.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.grabExcessVerticalSpace = true;
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.heightHint = heightHint;
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.verticalAlignment = GridData.CENTER;
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		group = new Composite(parent, SWT.NONE);
		// sShell.setText(title);
		group.setLayout(gridLayout);
		// sShell.setSize(new Point(857, 288));
		listLabel = new CLabel(group, SWT.RIGHT);
		listLabel.setText(readOnly ? introText : "List of checkouts from : ");
		listLabel.setLayoutData(gridData1);
		if (!readOnly) {
			fromText = new Text(group, SWT.BORDER);
			fromText.setEditable(false);
			fromText.setLayoutData(gridData);
		} else {
			gridData1.horizontalSpan = 2;
			listLabel.setLayoutData(gridData1);
		}
		if (!readOnly) {
			selectAllButton = new Button(group, SWT.NONE);
			selectAllButton.setText("Select all");
			selectAllButton.setLayoutData(gridData3);
			selectAllButton.addSelectionListener(this);
			unselectAllButton = new Button(group, SWT.NONE);
			unselectAllButton.setText("Unselect all");
			unselectAllButton.addSelectionListener(this);
			new Label(group, SWT.NONE);
		}
		checkoutsTable = new Table(group, SWT.BORDER | (readOnly ? SWT.NONE : SWT.CHECK)
				| SWT.FULL_SELECTION);
		checkoutsTable.setHeaderVisible(true);
		checkoutsTable.setLayoutData(gridData2);
		checkoutsTable.setLinesVisible(true);

		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		createActionsComposite();
		nameCol = new TableColumn(checkoutsTable, SWT.NONE);
		nameCol.setWidth(200);
		nameCol.setText("Name");
		typeCol = new TableColumn(checkoutsTable, SWT.NONE);
		typeCol.setWidth(120);
		typeCol.setText("Type");
		releaseCol = new TableColumn(checkoutsTable, SWT.NONE);
		releaseCol.setWidth(80);
		releaseCol.setText("Release");
		branchCol = new TableColumn(checkoutsTable, SWT.NONE);
		branchCol.setWidth(80);
		branchCol.setText("Branch");
		statusCol = new TableColumn(checkoutsTable, SWT.NONE);
		statusCol.setWidth(100);
		statusCol.setText("Status");
		dateCol = new TableColumn(checkoutsTable, SWT.NONE);
		dateCol.setWidth(100);
		dateCol.setText("Date");
		userCol = new TableColumn(checkoutsTable, SWT.NONE);
		userCol.setWidth(150);
		userCol.setText("User");
		// TableSorter.handle(checkoutsTable);
	}

	/**
	 * This method initializes actionsComposite
	 */
	private void createActionsComposite() {
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.END;
		gridData5.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.marginWidth = 1;
		gridLayout1.verticalSpacing = 1;
		gridLayout1.horizontalSpacing = 1;
		gridLayout1.numColumns = 2;
		gridLayout1.marginHeight = 1;
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.END;
		gridData4.grabExcessHorizontalSpace = false;
		gridData4.verticalAlignment = GridData.FILL;
		if (!readOnly) {
			actionsComposite = new Composite(group, SWT.NONE);
			actionsComposite.setLayoutData(gridData4);
			actionsComposite.setLayout(gridLayout1);
			undoCheckoutButton = new Button(actionsComposite, SWT.NONE);
			undoCheckoutButton.setText("Undo checkout of selected items");
			undoCheckoutButton.addSelectionListener(this);
			checkInButton = new Button(actionsComposite, SWT.NONE);
			checkInButton.setText("Checkin selected items");
			checkInButton.setLayoutData(gridData5);
			checkInButton.addSelectionListener(this);
		}
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
		if (e.getSource() == selectAllButton) {
			for (TableItem i : checkoutsTable.getItems()) {
				i.setChecked(true);
			}
		} else if (e.getSource() == unselectAllButton) {
			for (TableItem i : checkoutsTable.getItems()) {
				i.setChecked(false);
			}
		} else if (e.getSource() == undoCheckoutButton) {
			List<ICommand> undoCommands = new ArrayList<ICommand>();
			final Map<IVersionable<?>, TableItem> itemsMap = new HashMap<IVersionable<?>, TableItem>();
			for (final TableItem i : checkoutsTable.getItems()) {
				if (i.getChecked()) {
					final IVersionable<?> v = (IVersionable<?>) i.getData();
					undoCommands.add(new ICommand() {

						@Override
						public Object execute(Object... parameters) {
							getVersioningService().undoCheckOut(null, v);
							itemsMap.put(v, i);
							return null;
						}

						@Override
						public String getName() {
							return "Undoing check-out of " + v.getType().getName().toLowerCase()
									+ " <" + v.getName() + ">";
						}
					});
				}
			}
			try {
				CommandProgress.runWithProgress(false,
						undoCommands.toArray(new ICommand[undoCommands.size()]));
			} finally {
				refreshConnector();
			}
			// MessageDialog.openError(getShell(), "Unimplemented feature", "Not yet implemented");
		} else if (e.getSource() == checkInButton) {
			final List<IVersionable<?>> elementsToCheckIn = new ArrayList<IVersionable<?>>();
			final Map<IVersionable<?>, TableItem> itemsMap = new HashMap<IVersionable<?>, TableItem>();
			for (TableItem i : checkoutsTable.getItems()) {
				if (i.getChecked()) {
					final IVersionable<?> v = (IVersionable<?>) i.getData();
					elementsToCheckIn.add(v);
					itemsMap.put(v, i);
				}
			}
			// Building command list
			try {
				// Executing
				getVersioningService().commit(null,
						elementsToCheckIn.toArray(new IVersionable<?>[elementsToCheckIn.size()]));
			} finally {
				refreshConnector();
			}
		}

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		initializeGUI(parent);
		// Registering table
		initializeTable(checkoutsTable, null);
		TableSorter.handle(checkoutsTable);
		return group;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return group;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		return ((INamedObject) getModel()).getName() + " Checkouts list";
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		IVersionContainer container = (IVersionContainer) getModel();
		if (!readOnly) {
			fromText.setText(container.getName());
		}
		// checkoutsTable.removeAll();
		clean(checkoutsTable);
		for (IVersionable<?> v : checkoutList) {
			TableItem i = getOrCreateItem(v); // new TableItem(checkoutsTable,SWT.NONE);
			refreshItem(i, v);
		}

	}

	private void refreshItem(TableItem i, IVersionable<?> v) {
		i.setText(v.getName());
		i.setText(1, v.getType().getName());
		i.setText(2, v.getVersion().getLabel());
		i.setText(3, v.getVersion().getBranch().getName());
		i.setText(4, v.getVersion().getStatus().getLabel());

		i.setText(5, dateFormatter.format(v.getVersion().getUpdateDate()));
		i.setText(6, v.getVersion().getUser().getName());
		i.setImage(ImageFactory.getImage(v.getType().getIcon()));
		i.setData(v);
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();

	}

	private ICoreVersioningService getVersioningService() {
		return VCSUIPlugin.getVersioningUIService();
	}
}
