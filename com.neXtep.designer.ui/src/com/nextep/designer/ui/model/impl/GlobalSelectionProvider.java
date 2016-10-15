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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.ui.model.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.model.IGlobalSelectionProvider;

/**
 * @author Christophe Fondacci
 */
public class GlobalSelectionProvider implements IGlobalSelectionProvider, ISelectionChangedListener {

	private static final Log LOGGER = LogFactory.getLog(GlobalSelectionProvider.class);
	private Collection<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();
	private Map<IWorkbenchPart, ISelectionProvider> providersPageMap = new HashMap<IWorkbenchPart, ISelectionProvider>();
	private ISelection globalSelection = StructuredSelection.EMPTY;

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		final IEditorPart editor = CoreUiPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor != null) {
			if (editor instanceof FormEditor) {
				final IFormPage activePage = ((FormEditor) editor).getActivePageInstance();
				final ISelectionProvider activeProvider = providersPageMap.get(activePage);
				if (activeProvider != null) {
					return activeProvider.getSelection();
				}
			}
		}
		return globalSelection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		final IEditorPart editor = CoreUiPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor != null) {
			if (editor instanceof FormEditor) {
				final IEditorPart activePage = ((FormEditor) editor).getActiveEditor();
				final ISelectionProvider activeProvider = providersPageMap.get(activePage);
				if (activeProvider != null) {
					activeProvider.setSelection(selection);
				} else {
					this.globalSelection = selection;
				}
			}
		}
	}

	@Override
	public void registerSelectionProvider(IWorkbenchPart pageEditor, ISelectionProvider provider) {
		provider.addSelectionChangedListener(this);
		if (providersPageMap.get(pageEditor) == null) {
			providersPageMap.put(pageEditor, provider);
		} else {
			LOGGER.warn(MessageFormat
					.format("Ignoring registration of delegate selection provider ''{0}'' for part ''{1}''", //$NON-NLS-1$
					provider, pageEditor));
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		for (ISelectionChangedListener listener : listeners) {
			listener.selectionChanged(event);
		}
	}
}
