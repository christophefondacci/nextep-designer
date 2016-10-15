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
package com.nextep.designer.sqlgen.ui.impl;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.model.IModelOriented;

public class SQLEditorMatchAdapter implements IEditorMatchAdapter {

	@Override
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
		if (editor.getEditorInput() instanceof IModelOriented<?>) {
			return result.getMatches(((IModelOriented<?>) editor.getEditorInput()).getModel());
		} else {
			return null;
		}
	}

	@Override
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		Object elt = match.getElement();
		IEditorInput input = editor.getEditorInput();
		if (input instanceof ISQLEditorInput<?>) {
			if (((ISQLEditorInput<?>) input).getModel() == elt) {
				return true;
			}
		}
		return false;
	}

}
