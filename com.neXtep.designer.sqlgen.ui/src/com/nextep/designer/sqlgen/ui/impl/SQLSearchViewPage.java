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
package com.nextep.designer.sqlgen.ui.impl;

import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.sqlgen.ui.editors.SQLMultiEditor;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class SQLSearchViewPage extends AbstractTextSearchViewPage {

	private SQLSearchTreeNewContentProvider provider;
	
	@Override
	protected void clear() {
		if(provider != null ) {
			getViewer().refresh();
		}
	}

	@Override
	protected void configureTableViewer(TableViewer viewer) {
		provider = new SQLSearchTreeNewContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setLabelProvider(new DecoratingStyledCellLabelProvider(new SQLSearchLabelProvider(this), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(), null));

	}

	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		provider = new SQLSearchTreeNewContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setLabelProvider(new DecoratingStyledCellLabelProvider(new SQLSearchLabelProvider(this), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(), null));
	}

	@Override
	protected void elementsChanged(Object[] objects) {
		if(provider != null) {
			provider.elementsChanged(objects,true);
		}
	}

	@Override
	protected void showMatch(Match match, int currentOffset, int currentLength,
			boolean activate) throws PartInitException {
		Object elt = match.getElement();
		if(elt instanceof ITypedObject) {
			final ITypedObject typedObj = (ITypedObject)elt;
			final ITypedObjectUIController controller = UIControllerFactory.getController(typedObj.getType());
			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					controller.getEditorInput(typedObj), 
					controller.getEditorId());
			if(editor instanceof SQLMultiEditor) {
				editor = ((SQLMultiEditor)editor).getCurrentEditor();
			}
			if(editor instanceof AbstractTextEditor) {
				((AbstractTextEditor) editor).selectAndReveal(match.getOffset(), match.getLength());
			}
		}
	}
}
