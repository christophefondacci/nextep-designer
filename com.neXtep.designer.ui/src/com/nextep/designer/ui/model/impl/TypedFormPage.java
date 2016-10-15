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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.ui.model.impl;

import org.eclipse.ui.forms.editor.IFormPage;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.ui.model.ITypedFormPage;

/**
 * Default {@link ITypedFormPage} implementation
 * 
 * @author Christophe Fondacci
 */
public class TypedFormPage implements ITypedFormPage {

	private IElementType type;
	private IFormPage page;

	/**
	 * Creates this {@link ITypedFormPage} instance
	 * 
	 * @param type the {@link IElementType} associated with the form page
	 * @param page the {@link IFormPage}
	 */
	public TypedFormPage(IElementType type, IFormPage page) {
		this.type = type;
		this.page = page;
	}

	@Override
	public IElementType getType() {
		return type;
	}

	@Override
	public IFormPage getFormPage() {
		return page;
	}

}
