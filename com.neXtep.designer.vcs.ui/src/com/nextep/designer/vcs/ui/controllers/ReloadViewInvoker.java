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
package com.nextep.designer.vcs.ui.controllers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.model.InvokableController;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * @author Christophe Fondacci
 * @deprecated this class introduces an architectural problem: dependency from non-UI to UI layer
 */
@Deprecated
public class ReloadViewInvoker extends InvokableController {

	@Override
	public Object invoke(Object... arg) {
		boolean reload = true;
		if (arg.length == 0) {
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
				reload = MessageDialog.openQuestion(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						VCSUIMessages.getString("reloadViewPromptTitle"),
						VCSUIMessages.getString("reloadViewPrompt"));
			} else {
				reload = true;
			}
		}
		if (reload) {
			final IWorkspaceService viewService = getViewService();
			// Saving preivous view instance reference (because some listeners point to it)
			final IWorkspace v = viewService.getCurrentWorkspace();
			// Reloading view (creates a new view instance)
			viewService
					.changeWorkspace(viewService.getCurrentWorkspace().getUID(), new NullProgressMonitor());

			// Switching listeners to listen to the new view instance
			Designer.getListenerService().switchListeners(v, viewService.getCurrentWorkspace());
			for (IEventListener l : new ArrayList<IEventListener>(v.getListeners())) {
				Designer.getListenerService().unregisterListener(v, l);
			}

			// Changing opened editors' model: since the view has been reloaded, edited
			// objects are deprecated
			// and new instances have been loaded to represent the new database status.
			// We will now browse all opened editors to map the previously edited item
			// instance with the new one
			final IEditorReference[] editorRefs = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().getEditorReferences();
			final List<IEditorReference> editorsToClose = new ArrayList<IEditorReference>();
			for (IEditorReference r : editorRefs) {
				try {
					// We consider editors whose input contains a model, that is to say they
					// implement IModelOriented
					if (r.getEditorInput() instanceof IModelOriented) {
						final IReferenceable element = (IReferenceable) r.getEditorInput()
								.getAdapter(IReferenceable.class);
						// To have a chance to convert their input, we need the model to be
						// referenceable
						if (element != null) {
							final IReference ref = element.getReference();
							// Trying to locate new referenced model through the reference
							// manager
							IReferenceable newElement = VersionHelper.getReferencedItem(ref);
							if (newElement != null) {
								// Switching listeners
								Designer.getListenerService().switchListeners(
										(IObservable) element, (IObservable) newElement);
								// Here we are, changing model (may already be done through
								// the listeners switch)
								((IModelOriented) r.getEditorInput()).setModel(newElement);

							} else {
								// No reference found, closing...
								editorsToClose.add(r);
							}
						} else {
							// Not a referenceable element, closing editor
							editorsToClose.add(r);
						}
					}
				} catch (PartInitException e) {
					// Problems to retrieve the input, closing
					editorsToClose.add(r);
				}
			}
			// Closing all remaining editors (it is much likely editors on object which do
			// no more
			// exist in the new reloaded view)
			PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage()
					.closeEditors(
							editorsToClose.toArray(new IEditorReference[editorsToClose.size()]),
							false);
		}
		return null;
	}

	private IWorkspaceService getViewService() {
		return VCSPlugin.getService(IWorkspaceService.class);
	}
}
