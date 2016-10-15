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
/**
 *
 */
package com.nextep.datadesigner.vcs.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;

/**
 * @author Christophe Fondacci
 *
 */
public class TreeNavigatorEditor extends AbstractTreeEditor {

	protected ChangeEvent event;
	protected Text txt;

	public TreeNavigatorEditor(Tree tree, ChangeEvent firedEvent) {
		super(tree);
		this.event=firedEvent;
	}

	/**
	 * This method creates and returns the control for treeitem edition.
	 * It should return a <code>null</code> Control to indicate that this
	 * selection is not editable.
	 *
	 * @param selection TreeItem being edited
	 * @return the SWT control for this item edition
	 */
	protected Control getEditControl(TreeItem selection) {
		if(!(selection.getData() instanceof INavigatorConnector)) {
			return null;
		}
		INavigatorConnector conn = (INavigatorConnector)selection.getData();
		if(!(conn.getModel() instanceof INamedObject)) {
			return null;
		}
		INamedObject named = (INamedObject)conn.getModel();
		if(!VersionHelper.ensureModifiable(named, false)) return null;
		txt = new Text(selection.getParent(),SWT.BORDER);
		txt.setText(named.getName()==null ? "" : named.getName()); //lastSelection.getText());
		txt.selectAll();
		return txt;
	}
	/**
	 * Defines the layout of this editor.
	 * This method should be overridden by extensions.
	 *
	 * @param editor editor which will be displayed.
	 */
	protected void setEditorLayout(TreeEditor editor) {
		editor.grabHorizontal=true;
		editor.grabVertical=true;
		editor.verticalAlignment=SWT.CENTER;
	}
	/**
	 * Final validation of the edited content. This method applies
	 * modifications to the model. Extensions should override this
	 * method.
	 */
	protected void publish(TreeItem selection) {
		if(!selection.isDisposed() && !txt.isDisposed()) {
			INavigatorConnector nav = (INavigatorConnector)selection.getData();
			//nav.handleEvent(event, null, txtEditor.getText());
			INamedObject named = (INamedObject)nav.getModel();
			named.setName(txt.getText());
			nav.refreshConnector();
		}
	}

}
