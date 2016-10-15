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
package com.nextep.designer.sqlgen.ui;

import java.sql.Connection;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.texteditor.IDocumentProvider;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.gui.editors.ISubmitable;
import com.nextep.datadesigner.dbgm.gui.editors.SQLComparisonEditorInput;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.merge.SQLScriptMerger;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.vcs.gui.rcp.IComparisonItemEditorInput;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.ui.model.IConnectable;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class SQLEditorInput implements ISQLEditorInput<ISQLScript>, ISubmitable, IConnectable {

	// private static final Log log = LogFactory.getLog(SQLEditorInput.class);
	private ISQLScript editedScript;
	private IPersistableElement persistor;
	private Connection sqlConnection;
	private IConnection connection;

	public SQLEditorInput(ISQLScript script) {
		this.editedScript = script;
		persistor = new SQLScriptEditorFactory(script);
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	@Override
	public boolean exists() {
		return true;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(SQLGenImages.ICON_SQL);
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	@Override
	public String getName() {
		return editedScript.getName();
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	@Override
	public IPersistableElement getPersistable() {
		return persistor;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		if (editedScript.isExternal()) {
			return editedScript.getAbsolutePathname();
		} else {
			IVersionable<ISQLScript> v = VersionHelper.getVersionable(editedScript);
			String prefix = "";
			if (v != null) {
				prefix = v.getContainer() + "/";
			}
			return prefix + editedScript.getFilename();
		}
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IComparisonItemEditorInput.class) {
			return new SQLComparisonEditorInput(this, SQLScriptMerger.ATTR_SQL);
		}
		return null;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ISQLEditorInput<?>) {
			return getModel() == ((ISQLEditorInput<?>) obj).getModel();
		}
		return false;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#getModel()
	 */
	@Override
	public ISQLScript getModel() {
		return editedScript;
	}

	/**
	 * @see com.nextep.datadesigner.model.IModelOriented#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(ISQLScript model) {
		editedScript = model;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#getSql()
	 */
	@Override
	public String getSql() {
		return editedScript.getSql();
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#save()
	 */
	@Override
	public void save(IDocumentProvider provider) {
		ControllerFactory.getController(IElementType.getInstance(ISQLScript.TYPE_ID)).save(
				editedScript);

	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#setSql(java.lang.String)
	 */
	@Override
	public void setSql(String sql) {
		editedScript.setSql(sql);
	}

	@Override
	public String getDatabaseType() {
		return "";
	}

	@Override
	public boolean showSubmit() {
		return true;
	}

	@Override
	public Connection getSqlConnection() {
		return sqlConnection;
	}

	@Override
	public void setSqlConnection(Connection sqlConnection) {
		this.sqlConnection = sqlConnection;
	}

	@Override
	public IConnection getConnection() {
		return connection;
	}

	@Override
	public void setConnection(IConnection connection) {
		this.connection = connection;
	}
}
