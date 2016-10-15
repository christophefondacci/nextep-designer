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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.gui.impl.navigators.TypeNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;

/**
 * An abstract guideline class which provides base functionality
 * of a tree editor.<br>
 * Correct implementation should extend the abstract methods:<br>
 * - <code>getEditControl</code><br>
 * - <code>setEditorLayout</code><br>
 * - <code>publish</code><br>
 *
 * @author Christophe Fondacci
 *
 */
public abstract class AbstractTreeEditor implements MouseListener, Listener {
	private Tree tree;
	private TreeItem lastSelection;
	private Control editorControl;
	private boolean dblClick = false;

	protected AbstractTreeEditor(Tree tree) {
		this.tree = tree;
		tree.addMouseListener(this);
	}
	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		this.disposeEditor();
		dblClick=true;
	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent e) {
		if(editorControl!=null && !editorControl.isDisposed()) {
			this.disposeEditor();
		}
	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public final void mouseUp(MouseEvent e) {
		if(dblClick) {
			TreeItem[] selection = tree.getSelection();
			if(selection.length>0) {
				lastSelection = selection[0];
			}
			dblClick=false;
			return;
		}
		TreeItem[] selection = tree.getSelection();
		if(selection.length>0 && e.button==1) {
			Rectangle r = selection[0].getBounds();
			if(e.x>r.x && e.x < (r.x+r.width) && e.y > r.y && e.y < (r.y+r.height)
					&& selection[0]==lastSelection && lastSelection.getData() instanceof INavigatorConnector
					&& !(lastSelection.getData() instanceof TypeNavigator)) {
				TreeEditor editor = new TreeEditor(tree);
				//Retrieveing object name
				editorControl = getEditControl(lastSelection);
				if(editorControl == null) return;
				editorControl.addListener(SWT.FocusOut, this);
				editorControl.addListener(SWT.Traverse, this);
				editor.setEditor(editorControl, selection[0]);
				setEditorLayout(editor);
				editor.layout();
				editorControl.setFocus();

			}
			lastSelection=selection[0];
		} else {
			lastSelection = null;
		}
	}

	/**
	 * This method creates and returns the control for treeitem edition.
	 * It should return a <code>null</code> Control to indicate that this
	 * selection is not editable.
	 *
	 * @param selection TreeItem being edited
	 * @return the SWT control for this item edition
	 */
	protected abstract Control getEditControl(TreeItem selection);
	/**
	 * Defines the layout of this editor.
	 * This method should be overridden by extensions.
	 *
	 * @param editor editor which will be displayed.
	 */
	protected abstract void setEditorLayout(TreeEditor editor);
	/**
	 * Final validation of the edited content. This method applies
	 * modifications to the model. Extensions should override this
	 * method.
	 */
	protected abstract void publish(TreeItem selection);
	protected final void disposeEditor() {
		if(editorControl!=null && !editorControl.isDisposed()) {
			editorControl.dispose();
			editorControl=null;
		}
	}
	/**
	 * Event listener of the editor control. This listener fires validation
	 * or cancellation actions depending on the focus / escape / return
	 * exit status.
	 *
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public final void handleEvent(final Event e) {
        switch (e.type) {
        	case SWT.FocusOut:
        		try {
        			this.publish(lastSelection);
        		} finally {
		        	this.disposeEditor();
        		}
		        break;
	        case SWT.Traverse:
	        	switch (e.detail) {
	        		case SWT.TRAVERSE_RETURN:
	        			try {
	        				this.publish(lastSelection);
	        			} finally {
	        				this.disposeEditor();
	        				e.doit=false;
	        			}
	        			break;
	        		case SWT.TRAVERSE_ESCAPE:
        				this.disposeEditor();
	        			e.doit = false;
	        			break;
	        	}
	        	break;
        }
	}

}
