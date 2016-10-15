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
package com.nextep.designer.sqlgen.ui.editors;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;

public class TypedObjectHyperlink implements IHyperlink {

	private IRegion region;
	private String text;
	private ITypedObject object;
	public TypedObjectHyperlink(IRegion region, String text, ITypedObject object) {
		this.region = region;
		this.text = text;
		this.object = object;
	}
	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public String getHyperlinkText() {
		return text;
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public void open() {
		SQLEditorUIServices.getInstance().getTypedObjectTextProvider().open(text);
//		ControllerFactory.getController(object.getType()).initializeNavigator(object).defaultAction();
	}

}
