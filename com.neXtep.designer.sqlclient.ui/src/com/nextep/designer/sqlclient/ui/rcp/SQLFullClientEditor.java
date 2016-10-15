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
package com.nextep.designer.sqlclient.ui.rcp;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.menus.IMenuService;
import com.nextep.datadesigner.Designer;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.ui.editors.SQLEditor;
import com.nextep.designer.ui.helpers.UIHelper;

/**
 * This class need to be refactored : Problems of SQLClientTypedEditorInput versus
 * SQLClientEditorInput. Problems of connection initialization
 * 
 * @author Christophe Fondacci
 */
public class SQLFullClientEditor extends SQLEditor {

	public static final String EDITOR_ID = "com.neXtep.designer.sqlclient.ui.SQLFullClientEditor"; //$NON-NLS-1$
	private String title = null;
	private IConnection connection;
	private ToolBarManager tbm;
	private Composite editor;

	/**
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setSite(site);
		if (input instanceof SQLClientEditorInput) {
			final SQLClientEditorInput sqlInput = (SQLClientEditorInput) input;
			connection = sqlInput.getConnection();
			if (connection != null) {
				title = connection.getName();
				setTitleImage(UIHelper.getVendorIcon(connection.getDBVendor()));
			} else {
				title = sqlInput.getName();
			}
			setInput(sqlInput);
			setTitleToolTip(getTitleToolTip());
		}
		setHelpContextId("com.neXtep.designer.sqlclient.ui.SQLClient"); //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		final IMenuService service = (IMenuService) getSite().getService(IMenuService.class);
		service.releaseContributions(tbm);
		super.dispose();
	}

	@Override
	public String getTitleToolTip() {
		return "Query the database. Hit CTRL+ENTER or F8 to execute.";
	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		// never dirty except during termination (repository connection lost)
		return Designer.getTerminationSignal();
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		gl.marginBottom = gl.marginHeight = gl.marginLeft = gl.marginRight = gl.marginTop = gl.marginWidth = gl.verticalSpacing = 0;
		editor.setLayout(gl);
		final ToolBar tb = new ToolBar(editor, SWT.NONE);
		tb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		tbm = new ToolBarManager(tb);
		tbm.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		final IMenuService menuService = (IMenuService) getSite().getService(IMenuService.class);
		menuService.populateContributionManager(tbm, "toolbar:" + getContributionId());
		tbm.update(true);
		Composite sqlEditorPane = new Composite(editor, SWT.BORDER);
		FillLayout fl = new FillLayout();
		fl.marginHeight = fl.marginWidth = 0;
		sqlEditorPane.setLayout(fl);
		sqlEditorPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		super.createPartControl(sqlEditorPane);

		// if (connection == null) {
		// MessageDialog
		// .openError(
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
		// "No development target",
		// "No development target database has been defined. Contents viewer needs a database connection to query SQL data. Please define a development target connection and try again.");
		// throw new ErrorException("No development database defined.");
		// }
	}

	@Override
	public String getPartName() {
		return title; // + " Query Editor";
	}

	protected String getContributionId() {
		return EDITOR_ID;
	}

}
