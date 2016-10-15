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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import com.nextep.datadesigner.dbgm.gui.editors.IAnnotatedInput;
import com.nextep.datadesigner.dbgm.gui.editors.ISubmitable;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;

/**
 * Extension of input type for SQL scripts so they can be submitted directly to the database.
 * 
 * @author Christophe
 */
public class SQLScriptEditorInput extends SQLEditorInput implements ISubmitable, IAnnotatedInput {

	public SQLScriptEditorInput(ISQLScript s) {
		super(s);
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
	public Map<Annotation, Position> getAnnotationMap(IDocument doc) {
		return SQLEditorUIServices.getInstance().getCompilationMarkersFor(getModel(), doc,
				null);
	}

	@Override
	public Collection<String> getAnnotationTypes() {
		return Arrays.asList("org.eclipse.ui.workbench.texteditor.error",
				"org.eclipse.ui.workbench.texteditor.warning");
	}
}
