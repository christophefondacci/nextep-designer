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
package com.nextep.designer.vcs.ui.compare;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.vcs.gui.rcp.ComparisonItemEditorInput;
import com.nextep.datadesigner.vcs.impl.ComparisonPropertyWrapper;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.vcs.model.IComparisonItem;

public class PropertyEditor extends EditorPart {

	public final static String EDITOR_ID = "com.nextep.designer.vcs.ui.compare.PropertyEditor";
	private Tree tree;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		tree = new Tree(parent, SWT.NONE);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		TreeColumn propName = new TreeColumn(tree, SWT.NONE);
		propName.setText(UIMessages.getString("properties.view.propertyCol"));
		propName.setWidth(200);

		TreeColumn propSrcVal = new TreeColumn(tree, SWT.NONE);
		propSrcVal.setText(UIMessages.getString("properties.view.sourceValueCol"));
		propSrcVal.setWidth(150);

		TreeColumn propTgtVal = new TreeColumn(tree, SWT.NONE);
		propTgtVal.setText(UIMessages.getString("properties.view.targetValueCol"));
		propTgtVal.setWidth(150);
		
		final ComparisonItemEditorInput input = (ComparisonItemEditorInput)getEditorInput();
		final IComparisonItem compItem = input.getComparisonItem();
		ComparisonPropertyWrapper prop = new ComparisonPropertyWrapper("", compItem);
		ComparisonPropertyNavigator nav = new ComparisonPropertyNavigator(prop, tree);
		nav.create(null, -1);
		nav.initialize();
		nav.refreshConnector();
		autoExpand(nav.getSWTConnector());
	}

	@Override
	public void setFocus() {
		tree.setFocus();
	}

	/**
	 * Automatically expand any item which has the DIFFER color in the tree item hierarchy
	 * @param item root tree item to start from 
	 */
	private void autoExpand(TreeItem item) {
		if(item.getBackground() == FontFactory.COMPARISON_DIFFER) {
			item.setExpanded(true);
			for(TreeItem child : item.getItems()) {
				autoExpand(child);
			}
		}
	}
}
