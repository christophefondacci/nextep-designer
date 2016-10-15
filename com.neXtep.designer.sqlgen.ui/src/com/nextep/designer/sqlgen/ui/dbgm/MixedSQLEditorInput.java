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
package com.nextep.designer.sqlgen.ui.dbgm;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.MultiEditorInput;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.impl.rcp.RCPTypedEditor;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.sqlgen.ui.editors.SQLEditor;

/**
 * A special *multi* input to provide editors and inputs for the MixedSqlEditor composed of a typed
 * neXtep editor combined with a SQL editor.
 * 
 * @author Christophe Fondacci
 */
public class MixedSQLEditorInput extends MultiEditorInput implements ITypedObject {

	private ITypedObject typedObject;
	private String introText;

	public MixedSQLEditorInput(ITypedObject typedObj, ISQLEditorInput<?> sqlInput) {
		this(typedObj, sqlInput, SQLEditor.EDITOR_ID, SQLMessages
				.getString("editor.mixedSql.sqlIntroText")); //$NON-NLS-1$
	}

	public MixedSQLEditorInput(ITypedObject typedObj, ISQLEditorInput<?> sqlInput,
			String sqlEditorId) {
		this(typedObj, sqlInput, sqlEditorId, SQLMessages.getString("editor.mixedSql.sqlIntroText")); //$NON-NLS-1$
	}

	public MixedSQLEditorInput(ITypedObject typedObj, ISQLEditorInput<?> sqlInput,
			String sqlEditorId, String introText) {
		super(new String[] { RCPTypedEditor.EDITOR_ID, sqlEditorId }, new IEditorInput[] {
				new TypedEditorInput(typedObj), sqlInput });
		this.typedObject = typedObj;
		this.introText = introText;
	}

	@Override
	public String getName() {
		if (typedObject instanceof INamedObject) {
			return ((INamedObject) typedObject).getName();
		} else {
			return super.getName();
		}
	}

	public String getSqlIntroText() {
		return introText;
	}

	@Override
	public IElementType getType() {
		return typedObject.getType();
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == ISQLEditorInput.class) {
			return getInput()[1];
		}
		return super.getAdapter(adapter);
	}
}
