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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.impl.StringAttribute;
import com.nextep.datadesigner.vcs.gui.dialog.MergeNavigator;

/**
 * @author Christophe Fondacci
 *
 */
public class TreeAttributeEditor extends AbstractTreeEditor {

	private Text editor;

	private TreeAttributeEditor(Tree t) {
		super(t);
	}
	public static TreeAttributeEditor handle(Tree t) {
		return new TreeAttributeEditor(t);
	}
	/**
	 * @see com.nextep.datadesigner.vcs.gui.AbstractTreeEditor#getEditControl(org.eclipse.swt.widgets.TreeItem)
	 */
	@Override
	protected Control getEditControl(TreeItem selection) {
		if(selection.getData() instanceof MergeNavigator) {
			MergeNavigator nav = (MergeNavigator)selection.getData();
			StringAttribute str = (StringAttribute)nav.getMergeProposal();
			editor = new Text(selection.getParent(),SWT.BORDER);
			editor.setText((String)str.getValue());
			editor.selectAll();
			Rectangle itemBounds = selection.getBounds();
			// Simulating text to compute attribute intro bounds
			String backupText = selection.getText();
			selection.setText(str.getName() + " = ");
			Rectangle intro = selection.getBounds();
			selection.setText(backupText);
			// Now sizing our Text control
			editor.setBounds(intro.x+intro.width, itemBounds.y, itemBounds.width-intro.width, itemBounds.height);
			editor.setVisible(true);
			editor.setFocus();
			return editor;
		}
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.gui.AbstractTreeEditor#publish(org.eclipse.swt.widgets.TreeItem)
	 */
	@Override
	protected void publish(TreeItem selection) {
		if(!selection.isDisposed()) {
			MergeNavigator nav = (MergeNavigator)selection.getData();
			StringAttribute str = (StringAttribute)nav.getMergeProposal();
			str.setValue(editor.getText());
			nav.refreshConnector();
		}

	}

	/**
	 * @see com.nextep.datadesigner.vcs.gui.AbstractTreeEditor#setEditorLayout(org.eclipse.swt.custom.TreeEditor)
	 */
	@Override
	protected void setEditorLayout(TreeEditor editor) {
		editor.minimumHeight = editor.getEditor().getBounds().height;
		editor.minimumWidth = editor.getEditor().getBounds().width;
//		editor.verticalAlignment=SWT.CENTER;
//		editor.grabVertical=true;
//		editor.grabHorizontal=true;
	}

}
