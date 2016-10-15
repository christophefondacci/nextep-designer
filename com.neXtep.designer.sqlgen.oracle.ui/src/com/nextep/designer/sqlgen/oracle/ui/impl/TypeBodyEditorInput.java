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
package com.nextep.designer.sqlgen.oracle.ui.impl;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.texteditor.IDocumentProvider;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.ui.factories.ImageFactory;

public class TypeBodyEditorInput implements ISQLEditorInput<IOracleUserType> {

	private IOracleUserType type;

	public TypeBodyEditorInput(IOracleUserType type) {
		this.type = type;
	}

	@Override
	public String getSql() {
		return getModel().getTypeBody();
	}

	@Override
	public void save(IDocumentProvider provider) {
		CorePlugin.getIdentifiableDao().save(getModel());
	}

	@Override
	public void setSql(String sql) {
		getModel().setTypeBody(sql);
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageFactory.getImageDescriptor(getModel().getType().getIcon());
	}

	@Override
	public String getName() {
		return getModel().getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public IOracleUserType getModel() {
		return type;
	}

	@Override
	public void setModel(IOracleUserType model) {
		this.type = model;
	}

}
