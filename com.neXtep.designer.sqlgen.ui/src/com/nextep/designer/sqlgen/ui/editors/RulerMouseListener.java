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

import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.designer.sqlgen.ui.PackageEditorInput;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;

/**
 * A mouse listener of the SQL Editor's vertical
 * ruler which can instantiate new breakbopints
 * 
 * @author Christophe Fondacci
 *
 */
public class RulerMouseListener implements MouseListener {

	private IVerticalRuler ruler;
	private SQLEditor editor;
	public RulerMouseListener(IVerticalRuler ruler,SQLEditor editor) {
		this.ruler = ruler;
		this.editor = editor;
	}
	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if(editor.getEditorInput() instanceof PackageEditorInput) {
			IPackage pkg = (IPackage)((PackageEditorInput)editor.getEditorInput()).getModel();
			int line = ruler.toDocumentLineNumber(e.y);
			SQLEditorUIServices.getInstance().toggleBreakpoint(new Breakpoint(pkg,line));
		}
	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseUp(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
