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
package com.nextep.designer.sqlgen.ui.editors.sql;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.gui.impl.TreeSelectionProvider;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.designer.dbgm.sql.TextPosition;
import com.nextep.designer.sqlgen.ui.editors.SQLMultiEditor;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class SQLContentOutlinePage extends Page implements IContentOutlinePage {

    protected IDocumentProvider fDocumentProvider;
    protected ITextEditor editor;
    private Tree outlineTree;
    private INavigatorConnector packageConnector;
    private ISelectionProvider selProvider;
    private Object selection;

    /**
     * Creates a content outline page using the given provider
     * and the given editor.
     *
     * @param provider the document provider
     * @param editor the editor
     */
    public SQLContentOutlinePage(IDocumentProvider provider, ITextEditor editor) {
        super();
        fDocumentProvider= provider;
        this.editor= editor;
    }

    /**
     * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        outlineTree = new Tree(parent,SWT.NONE);
        selProvider = TreeSelectionProvider.handle(outlineTree, false);
        addSelectionChangedListener(new ISelectionChangedListener() {
        	@Override
        	public void selectionChanged(SelectionChangedEvent event) {
            	StructuredSelection sel = (StructuredSelection)selProvider.getSelection();
            	if(!sel.isEmpty() && packageConnector.getModel() instanceof IPackage) {
            		IParseable pkg = (IParseable)packageConnector.getModel();
            		setInternalSelection(pkg, sel.getFirstElement());
            	}
        	}
        });
        Object model = ((ISQLEditorInput)editor.getEditorInput()).getModel();
        if(model !=null) {
	        packageConnector = UIControllerFactory.getController(model).initializeNavigator(model);
	        packageConnector.setTree(outlineTree);
	        packageConnector.create(null, -1);
	//        if(packageConnector instanceof PackageNavigator) {
	//        	((PackageNavigator)packageConnector).dedicatedChildInit();
	//        }
	        packageConnector.initialize();
	        packageConnector.refreshConnector();
	        packageConnector.getSWTConnector().setExpanded(true);
        }
    }

    public void update() {
        if(packageConnector!=null) {
            packageConnector.refreshConnector();
        }
    }
    /**
     * @see org.eclipse.ui.part.IPage#getControl()
     */
    @Override
    public Control getControl() {
        return outlineTree;
    }

    /**
     * @see org.eclipse.ui.part.IPage#setFocus()
     */
    @Override
    public void setFocus() {
        outlineTree.setFocus();
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        selProvider.addSelectionChangedListener(listener);
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
     */
    @Override
    public ISelection getSelection() {
    	StructuredSelection sel = (StructuredSelection)selProvider.getSelection();
    	if(!sel.isEmpty() && packageConnector.getModel() instanceof IPackage) {
    		IParseable pkg = (IParseable)packageConnector.getModel();
    		setInternalSelection(pkg, sel.getFirstElement());
    	}
    	return sel;
    }

    private void setInternalSelection(IParseable pkg, Object sel) {
    	if(this.selection != sel) {
    		this.selection = sel;
       		TextPosition p = pkg.getParseData().getPosition(sel);
    		if(p!=null) {
    			if(editor instanceof SQLMultiEditor) {
    				((SQLMultiEditor)editor).getCurrentEditor().setHighlightRange(p.offset, p.length, true);
    			} else {
    				editor.setHighlightRange(p.offset, p.length, true);
    			}
    		}
    	}
    }
    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    @Override
    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        selProvider.removeSelectionChangedListener(listener);
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void setSelection(ISelection selection) {
        selProvider.setSelection(selection);
    }
}
