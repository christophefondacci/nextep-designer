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
package com.nextep.designer.dbgm.gef;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Christophe Fondacci
 *
 */
public class UnwrappingSelectionProvider implements ISelectionProvider
{
	protected ISelection selection;
	protected Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();
	protected ISelectionChangedListener selectionChangedListener =
		new ISelectionChangedListener()
	{
		public void selectionChanged(SelectionChangedEvent event)
		{
			setSelection(event.getSelection());
		}
	};

	public UnwrappingSelectionProvider(ISelectionProvider selectionProvider)
	{
		selectionProvider.addSelectionChangedListener(selectionChangedListener);
		setSelection(selectionProvider.getSelection());
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		listeners.add(listener);
	}

	public ISelection getSelection()
	{
		return selection;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		listeners.remove(listener);
	}

	public void setSelection(ISelection selection)
	{
		if (selection instanceof IStructuredSelection)
		{
			Object [] objects = ((IStructuredSelection)selection).toArray();
			for (int i = 0; i < objects.length; ++i)
			{
				objects[i] = unwrap(objects[i]);
			}
			this.selection = new StructuredSelection(objects);
		}
		else
		{
			this.selection = selection;
		}
		fireSelectionChanged();
	}

	protected Object unwrap(Object object)
	{
//		if(object instanceof EditPart) {
//			Object model = ((EditPart)object).getModel();
//			if(model instanceof IDiagramItem) {
//				IDiagramItem item = (IDiagramItem)model;
//				Object sel = VersionHelper.getReferencedItem(item.getItemReference());
//				return sel;
//			}
//		}
		return object;
	}

	protected void fireSelectionChanged()
	{
		for (ISelectionChangedListener selectionChangedListener : listeners)
		{
			selectionChangedListener.selectionChanged(new SelectionChangedEvent(this, selection));
		}
	}
}