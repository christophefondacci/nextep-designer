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
package com.nextep.designer.vcs.ui.dialogs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.swt.TableColumnSorter;
import com.nextep.datadesigner.gui.model.IDesignerGUI;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.core.services.IRepositoryService;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.editors.RepositoryConnectionEditor;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.impl.Workspace;
import com.nextep.designer.vcs.services.IWorkspaceService;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.jface.WorkspaceContentProvider;
import com.nextep.designer.vcs.ui.jface.WorkspaceLabelProvider;
import com.nextep.designer.vcs.ui.swt.ViewSelectorTable;

/**
 * @author Christophe Fondacci
 */
public class ViewSelectorDialog implements IDesignerGUI, IEventListener {

	private Log log = LogFactory.getLog(ViewSelectorDialog.class);
	private List<IWorkspace> views;
	private boolean isOK = false;

	private Shell sShell = null;
	private CLabel selectLabel = null;
	private Table viewsTable = null;
	private TableViewer viewer = null;
	private Button cancelButton = null;
	private Button okButton = null;
	private Button newButton = null;
	private Label statusLabel;

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#initializeGUI(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	public void initializeGUI(Shell parentGUI) {
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.END;
		gridData3.verticalAlignment = GridData.CENTER;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.END;
		gridData2.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		// gridLayout.makeColumnsEqualWidth = true;

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		gridData.verticalAlignment = GridData.CENTER;
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.END;
		gridData11.verticalAlignment = GridData.CENTER;

		sShell = new Shell(parentGUI, SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		sShell.setText(VCSUIMessages.getString("dialog.viewSelector.title")); //$NON-NLS-1$
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(400, 500));
		sShell.setImage(VCSImages.ICON_CHANGE_VIEW);

		// Creating view selection controls
		selectLabel = new CLabel(sShell, SWT.NONE);
		selectLabel.setText(VCSUIMessages.getString("dialog.viewSelector.introLabel")); //$NON-NLS-1$
		selectLabel.setLayoutData(gridData);
		viewsTable = ViewSelectorTable.create(sShell);
		configureTableViewer();

		new Label(sShell, SWT.NONE);
		new Label(sShell, SWT.NONE);
		newButton = new Button(sShell, SWT.NONE);
		newButton.setText(VCSUIMessages.getString("dialog.viewSelector.createWorkspaceButton")); //$NON-NLS-1$
		newButton.setLayoutData(gridData3);
		cancelButton = new Button(sShell, SWT.NONE);
		cancelButton.setText(VCSUIMessages.getString("dialog.viewSelector.cancel")); //$NON-NLS-1$
		new Label(sShell, SWT.NONE);
		okButton = new Button(sShell, SWT.NONE);
		okButton.setText(VCSUIMessages.getString("dialog.viewSelector.ok")); //$NON-NLS-1$
		okButton.setLayoutData(gridData2);
		sShell.setDefaultButton(okButton);
		// Create the OK button listeners
		createUIListenersButtonOK();
		// Create the cancel button listeners
		createUIListenersButtonCancel();
		newButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				try {
					UIControllerFactory.getController(
							IElementType.getInstance(IWorkspace.TYPE_ID)).newInstance(null);
				} catch (CancelException e) {
					if (getViewService().getCurrentWorkspace() != null) {
						getViewService().setCurrentWorkspace(null);
					}
					refreshGUI();
					return;
				}
				// Opening new view
				if (VCSPlugin.getViewService().getCurrentWorkspace() != null) {
					refreshGUI();
					isOK = true;
				} else {
					refreshGUI();
				}
			}
		});
		// Creating status line
		GridData statusData = new GridData();
		statusData.horizontalAlignment = GridData.FILL;
		statusData.grabExcessHorizontalSpace = true;
		statusData.horizontalSpan = 3;
		statusLabel = new Label(sShell, SWT.BORDER);
		statusLabel.setLayoutData(statusData);
		sShell.layout();
		sShell.pack();
		Rectangle r = parentGUI.getBounds();
		Rectangle s = sShell.getBounds();
		Point loc = new Point(r.x + (r.width / 2) - (s.width / 2), r.y + (r.height / 2)
				- (s.height / 2));
		sShell.setLocation(loc);
		refreshGUI();
	}

	/**
	 * Configures the viewer which displays workspaces.<br>
	 * The setup is a bit tricky as we need a "button" behaviour which reacts on the deletion
	 * column, thus allowing the user to remove a view. We do this by adding an editor which fires
	 * the deletion on activation. The way JFace performs editor checks impose us to add cell
	 * modifier and label properties as well.<br>
	 * Because of JFace, the order in which elements are added are <b>very important</b> as editors
	 * and modifiers need to be added <u>before</u> label providers.
	 */
	private void configureTableViewer() {
		viewer = new TableViewer(viewsTable);
		viewer.setCellEditors(new CellEditor[] { null, null, null, new CellEditor() {

			@Override
			public void activate() {
				ISelection s = viewer.getSelection();
				if (s instanceof IStructuredSelection) {
					final IWorkspace view = (IWorkspace) ((IStructuredSelection) s)
							.getFirstElement();
					boolean confirm = MessageDialog.openQuestion(
							viewsTable.getShell(),
							MessageFormat.format(VCSUIMessages.getString("delViewConfirmTitle"), //$NON-NLS-1$
									view.getName()),
							MessageFormat.format(
									VCSUIMessages.getString("delViewConfirm"), view.getName())); //$NON-NLS-1$
					if (confirm) {
						final IWorkspace originalView = getViewService().getCurrentWorkspace();
						try {
							getViewService().setCurrentWorkspace(view);
							deleteView(view);
						} finally {
							getViewService().setCurrentWorkspace(originalView);
						}
						refreshGUI();
					}
				}
			}

			@Override
			protected Control createControl(Composite parent) {
				return null;
			}

			@Override
			protected Object doGetValue() {
				return null;
			}

			@Override
			protected void doSetFocus() {
			}

			@Override
			protected void doSetValue(Object value) {
			}
		} });
		viewer.setColumnProperties(new String[] { "name", "vendor", "desc", "del" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		viewer.setCellModifier(new ICellModifier() {

			@Override
			public void modify(Object element, String property, Object value) {
			}

			@Override
			public Object getValue(Object element, String property) {
				return null;
			}

			@Override
			public boolean canModify(Object element, String property) {
				return "del".equals(property); //$NON-NLS-1$
			}
		});
		viewer.setComparator(new TableColumnSorter(viewsTable, viewer));
		viewer.setContentProvider(new WorkspaceContentProvider());
		viewer.setLabelProvider(new WorkspaceLabelProvider());
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#getDisplay()
	 */
	@Override
	public Display getDisplay() {
		return sShell.getDisplay();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#getShell()
	 */
	@Override
	public Shell getShell() {
		return sShell;
	}

	/**
    *
    */
	private void createUIListenersButtonCancel() {
		cancelButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleButtonCancelWidgetSelected();
			}
		});
	}

	/**
    *
    */
	private void handleButtonCancelWidgetSelected() {
		throw new CancelException(VCSUIMessages.getString("dialog.viewSelector.selectionCancelled")); //$NON-NLS-1$
	}

	/**
    *
    */
	private void createUIListenersButtonOK() {
		okButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleButtonOKWidgetSelected();
			}
		});
		viewsTable.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				handleButtonOKWidgetSelected();
			}
		});
	}

	/**
    *
    */
	protected void handleButtonOKWidgetSelected() {
		final ISelection selection = viewer.getSelection();
		if (!selection.isEmpty()) {
			final IWorkspace selectedView = (IWorkspace) ((IStructuredSelection) selection)
					.getFirstElement();
			getViewService().setCurrentWorkspace(selectedView);
			isOK = true;
		} else {
			MessageDialog.openError(getShell(),
					VCSUIMessages.getString("dialog.viewSelector.selectionFailed"), //$NON-NLS-1$
					VCSUIMessages.getString("dialog.viewSelector.noSelectionMsg")); //$NON-NLS-1$
			sShell.setFocus();
		}
	}

	/**
    *
    */
	private void refreshGUI() {
		views = new ArrayList<IWorkspace>();
		Connection connection = null;
		while (connection == null) {
			// Loading all views
			try {
				statusLabel.setText(VCSUIMessages.getString("dialog.viewSelector.connectionInit")); //$NON-NLS-1$
				final IRepositoryService repositoryService = getRepositoryService();
				final IDatabaseConnector dbConnector = repositoryService.getRepositoryConnector();
				final IConnection repoConn = repositoryService.getRepositoryConnection();
				statusLabel.setText(MessageFormat.format(
						VCSUIMessages.getString("dialog.viewSelector.connectionAttempt"), //$NON-NLS-1$
						dbConnector.getConnectionURL(repoConn)));
				connection = dbConnector.connect(repoConn);
				statusLabel.setText(VCSUIMessages.getString("dialog.viewSelector.connected")); //$NON-NLS-1$
			} catch (SQLException e) {
				statusLabel
						.setText(VCSUIMessages.getString("dialog.viewSelector.failure") + e.getMessage()); //$NON-NLS-1$
				editRepository();
			} catch (Exception e) {
				statusLabel
						.setText(VCSUIMessages.getString("dialog.viewSelector.failure") + e.getMessage()); //$NON-NLS-1$
				log.error(e);
				handleButtonCancelWidgetSelected();
			}
		}
		try {
			Statement stmt = connection.createStatement();
			ResultSet rset = stmt
					.executeQuery("select view_id,view_name,description,dbvendor from REP_VERSION_VIEWS order by view_name"); //$NON-NLS-1$
			try {
				// Fetching results and temporarily store them in the map
				while (rset.next()) {
					long id = rset.getLong(1);
					String name = rset.getString(2);
					String description = rset.getString(3);
					IWorkspace view = new Workspace(name, description);
					view.setId(id);
					DBVendor vendor = null;
					try {
						vendor = DBVendor.valueOf(rset.getString(4));
					} catch (IllegalArgumentException e) {
						vendor = null;
					}
					view.setDBVendor(vendor);
					views.add(view);
				}
			} finally {
				rset.close();
				stmt.close();
			}
			// createSShell();
		} catch (Exception e) {
			log.error(e);
			handleButtonCancelWidgetSelected();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new ErrorException(e);
			}
		}
		viewer.setInput(views);
		viewer.refresh();
		// // Filling table
		// viewsTable.removeAll();
		// for (Button b : delButtons) {
		// b.dispose();
		// }
		// for (Object o : views) {
		// final IVersionView view = (IVersionView) o;
		// TableItem i = new TableItem(viewsTable, SWT.NONE);
		// Button delButton = new Button(viewsTable, SWT.PUSH);
		// delButtons.add(delButton);
		// delButton.setImage(ImageFactory.ICON_DELETE);
		// delButton.addSelectionListener(new SelectionAdapter() {
		//
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// boolean confirm = MessageDialog.openQuestion(viewsTable.getShell(),
		// MessageFormat.format(VCSUIMessages.getString("delViewConfirmTitle"),
		// view.getName()), MessageFormat.format(
		// VCSUIMessages.getString("delViewConfirm"), view.getName()));
		// if (confirm) {
		// final IVersionView originalView = VersionHelper.getCurrentView();
		// try {
		// VersionHelper.setCurrentView(view);
		// deleteView(view);
		// } finally {
		// VersionHelper.setCurrentView(originalView);
		// }
		// refreshGUI();
		// }
		// }
		// });
		//
		// TableEditor e = new TableEditor(viewsTable);
		// e.grabHorizontal = true;
		// e.setEditor(delButton, i, 3);
		// i.setText(view.getName());
		// i.setText(1, view.getDBVendor() == null ? "Undefined" : view.getDBVendor().toString());
		// i.setText(2, view.getDescription() == null ? "" : view.getDescription());
		// i.setData(view);
		// i.setImage(VCSImages.ICON_VERSION_NAVIGATOR);
		// }

	}

	private IRepositoryService getRepositoryService() {
		return CoreUiPlugin.getRepositoryUIService();
	}

	private void editRepository() {
		AbstractUIController.newWizardEdition("", //$NON-NLS-1$
				new RepositoryConnectionEditor());
	}

	public boolean isOK() {
		return isOK;
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// TODO Auto-generated method stub

	}

	private void deleteView(IWorkspace view) {
		if (view == null)
			return;
		// Checking checkout state
		boolean hasCheckouts = false;
		view = (IWorkspace) CorePlugin.getIdentifiableDao().load(Workspace.class, view.getUID());
		for (IVersionable<?> v : view.getContents()) {
			if (v.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
				hasCheckouts = true;
				break;
			}
		}
		if (hasCheckouts) {
			final boolean confirmed = MessageDialog.openQuestion(null,
					VCSUIMessages.getString("delViewWithCheckoutsConfirmTitle"), //$NON-NLS-1$
					VCSUIMessages.getString("delViewWithCheckoutsConfirm")); //$NON-NLS-1$
			if (!confirmed) {
				return;
			}
		}
		ControllerFactory.getController(view.getType()).modelDeleted(view);
	}

	private IWorkspaceService getViewService() {
		return VCSPlugin.getService(IWorkspaceService.class);
	}
}
