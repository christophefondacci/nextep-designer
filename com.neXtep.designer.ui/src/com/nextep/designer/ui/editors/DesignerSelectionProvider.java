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
package com.nextep.designer.ui.editors;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;

/**
 * A selection provider object which should be used (or extended) for ISelectionProvider objects.
 * 
 * @author Christophe Fondacci
 */
public class DesignerSelectionProvider implements ISelectionProvider, IModelOriented<IObservable>,
		IEventListener {

	private Set<ISelectionChangedListener> listeners;
	private ISelection currentSelection;
	private IObservable model;
	private Control control;

	public DesignerSelectionProvider(Control c, Object model) {
		listeners = new HashSet<ISelectionChangedListener>();
		control = c;
		if (model != null && c != null && model instanceof IObservable) {
			setModel((IObservable) model);
			Designer.getListenerService().registerListener(control, (IObservable) model, this);
		} else if (model != null) {
			setSelection(new StructuredSelection(model));
		}

	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		return currentSelection;
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection selection) {
		currentSelection = selection;
		for (ISelectionChangedListener l : listeners) {
			l.selectionChanged(new SelectionChangedEvent(this, selection));
		}
	}

	@Override
	public IObservable getModel() {
		return model;
	}

	@Override
	public void setModel(IObservable model) {
		// if(this.model!=null) {
		// Designer.getListenerService().unregisterListener(this.model, this);
		// }
		this.model = model;
		if (model != null) {
			setSelection(new StructuredSelection(model));
		} else {
			setSelection(new StructuredSelection());
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// Doing nothing
		switch (event) {
		case CHECKIN:
			setModel(getModel());
		}
	}

}
