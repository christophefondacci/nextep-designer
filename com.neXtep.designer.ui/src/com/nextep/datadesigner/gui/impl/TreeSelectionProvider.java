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
package com.nextep.datadesigner.gui.impl;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.gui.model.INavigatorConnector;

/**
 * @author Christophe Fondacci
 *
 */
public class TreeSelectionProvider extends AbstractSelectionProvider {

    private Tree tree;
    private boolean multiple;
    private TreeSelectionProvider(Tree tree,boolean multiple) {
        tree.addSelectionListener(this);
        this.tree = tree;
        this.multiple = multiple;
    }
    public static TreeSelectionProvider handle(Tree tree, boolean multiple) {
    	return new TreeSelectionProvider(tree,multiple);
    }
    /**
     * @see com.nextep.datadesigner.gui.impl.AbstractSelectionProvider#getSelection()
     */
    @Override
    public ISelection getSelection() {
        TreeItem[] selection = tree.getSelection();
        List<Object> selectedModel = new ArrayList<Object>();
        for(TreeItem i : selection) {
        	if(i.getData() instanceof INavigatorConnector) {
        		final Object sel = ((INavigatorConnector)i.getData()).getModel();
        		if(multiple) {
        			selectedModel.add( sel );
//        			selectedModel.add(i.getData());
        		} else {
        			if(sel != null) {
        				return new StructuredSelection(sel);
        			}
        		}

        	}
        }
        return new StructuredSelection(selectedModel.toArray());
    }

}
