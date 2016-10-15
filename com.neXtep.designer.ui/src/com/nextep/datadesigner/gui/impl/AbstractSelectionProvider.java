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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * An abstract selection provider for the RCP
 * platform. Extensions only have to implement
 * the {@link AbstractSelectionProvider#setSelection(ISelection)}
 * method to implement a selection provider.<br>
 * This abstract class implements the SWT {@link SelectionListener}
 * interface so implementors should add this class as a
 * selection listener of the SWT control.
 *
 * @see TableSelectionProvider
 *
 * @author Christophe Fondacci
 *
 */
public abstract class AbstractSelectionProvider implements ISelectionProvider,
		SelectionListener {
	private List<ISelectionChangedListener>	selListeners = new ArrayList<ISelectionChangedListener>();
	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public final void addSelectionChangedListener(ISelectionChangedListener listener) {
		selListeners.add(listener);
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	@Override
	public abstract ISelection getSelection();

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public final void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selListeners.remove(listener);
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public final void setSelection(ISelection selection) {
		for(ISelectionChangedListener l : selListeners) {
			l.selectionChanged(new SelectionChangedEvent(this,selection));
		}
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public final void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public final void widgetSelected(SelectionEvent e) {
		setSelection(getSelection());
	}

}
