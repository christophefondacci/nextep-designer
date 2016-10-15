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
package com.nextep.designer.vcs.ui.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.jface.TypedContentProvider;
import com.nextep.designer.vcs.ui.model.ITypedNode;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * This handler handles the move of elements into a target module.
 * 
 * @author Christophe Fondacci
 */
public class MoveToModuleHandler extends AbstractHandler {

	// private static final Log log = LogFactory.getLog(MoveToModuleHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// We have an unlocked container here, so we perform the move
		final ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			// Checking that every object to move is in a modifiable container
			Iterator<?> selIt = sel.iterator();
			Collection<IVersionable<?>> versionablesToMove = new ArrayList<IVersionable<?>>();
			while (selIt.hasNext()) {
				Object o = selIt.next();
				fillVersionablesFromObject(versionablesToMove, o);
			}
			// We can proceed
			Object elt = Designer.getInstance().invokeSelection(
					"find.element", //$NON-NLS-1$
					new TypedContentProvider(IElementType.getInstance(IVersionContainer.TYPE_ID)),
					VCSPlugin.getViewService().getCurrentWorkspace(),
					VCSUIMessages.getString("moveToContainerSelection")); //$NON-NLS-1$
			if (elt != null) {
				IVersionContainer c = (IVersionContainer) elt;
				getViewUIService().move(versionablesToMove, c);
			}
		}
		return null;
	}

	/**
	 * Fills the specified collection with versionables extracted from the given object
	 * 
	 * @param versionablesToMove collection of {@link IVersionable} to fill
	 * @param o object to extract {@link IVersionable} from
	 */
	private void fillVersionablesFromObject(Collection<IVersionable<?>> versionablesToMove, Object o) {
		if (o instanceof IVersionable<?>) {
			versionablesToMove.add((IVersionable<?>) o);
		} else if (o instanceof ITypedNode) {
			final ITypedNode typedNode = (ITypedNode) o;
			for (ITypedObject typedChild : typedNode.getChildren()) {
				fillVersionablesFromObject(versionablesToMove, typedChild);
			}
		}
	}

	public IWorkspaceUIService getViewUIService() {
		return VCSUIPlugin.getService(IWorkspaceUIService.class);
	}
}
