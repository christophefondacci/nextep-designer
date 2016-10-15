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
package com.nextep.datadesigner.dbgm.gui.editors;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import com.nextep.datadesigner.dbgm.model.ISqlBased;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;

/**
 * An interface representing a SQL editor input.
 * 
 * @author Christophe Fondacci
 */
public interface ISQLEditorInput<T extends IObservable> extends IEditorInput, IModelOriented<T>,
		ISqlBased {

	/**
	 * Saves the underlying model
	 */
	void save(IDocumentProvider provider);

}
