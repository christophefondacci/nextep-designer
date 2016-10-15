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

import org.eclipse.ui.texteditor.IDocumentProvider;
import com.nextep.datadesigner.dbgm.gui.editors.SQLComparisonEditorInput;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.sqlgen.impl.merge.PackageMerger;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.vcs.gui.rcp.IComparisonItemEditorInput;
import com.nextep.designer.core.factories.ControllerFactory;

/**
 * @author Christophe Fondacci
 */
public class SpecEditorInput extends PackageEditorInput {

	/**
	 * 
	 */
	public SpecEditorInput(IPackage pkg) {
		super(pkg);
	}

	/**
	 * @see com.nextep.designer.sqlgen.ui.PackageEditorInput#getSql()
	 */
	@Override
	public String getSql() {
		return getModel().getSpecSourceCode();
	}

	/**
	 * @see com.nextep.designer.sqlgen.ui.PackageEditorInput#setSql(java.lang.String)
	 */
	@Override
	public void setSql(String sql) {
		getModel().setSpecSourceCode(sql.replace("\r", "").replaceAll("\n( )+\n", "\n\n").trim());
	}

	/**
	 * @see com.nextep.designer.sqlgen.ui.PackageEditorInput#save(org.eclipse.ui.texteditor.IDocumentProvider)
	 */
	@Override
	public void save(IDocumentProvider provider) {
		ControllerFactory.getController(getModel().getType()).save(getModel());
	}

	@Override
	public String getDatabaseType() {
		return "PACKAGE";
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IComparisonItemEditorInput.class) {
			SQLComparisonEditorInput input = new SQLComparisonEditorInput(this,
					PackageMerger.ATTR_SPEC);
			return input;
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SpecEditorInput && getModel() == ((IModelOriented<?>) obj).getModel();
	}

	@Override
	public int hashCode() {
		return getModel().hashCode();
	}
}
