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
package com.nextep.designer.dbgm.ui.jface;

import java.util.ArrayList;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.ui.jface.TypedContentProvider;

public class TablesNotInDiagramContentProvider extends TypedContentProvider {

	private IDiagram diagram;
	public TablesNotInDiagramContentProvider(IDiagram diagram) {
		super(IElementType.getInstance(IBasicTable.TYPE_ID));
		this.diagram = diagram;
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		List<IReference> itemsRef = new ArrayList<IReference>();
		for(IDiagramItem i : diagram.getItems()) {
			itemsRef.add(i.getItemReference());
		}
		List<Object> elts = new ArrayList<Object>();
		for(Object o : super.getElements(inputElement)) {
			if(!itemsRef.contains(((IReferenceable)o).getReference())) {
				elts.add(o);
			}
		}
		return elts.toArray();
	}
	public boolean isEmpty() {
		return getElements(VersionHelper.getCurrentView()).length == 0;
	}
}
