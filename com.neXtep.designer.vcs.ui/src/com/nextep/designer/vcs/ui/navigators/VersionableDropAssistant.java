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
package com.nextep.designer.vcs.ui.navigators;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.model.ITypedNode;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

public class VersionableDropAssistant extends CommonDropAdapterAssistant {

	public VersionableDropAssistant() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent,
			Object aTarget) {
		final IVersionContainer c = (IVersionContainer) aTarget;
		// Performing drop here
		boolean confirmed = MessageDialog.openConfirm(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), VCSUIMessages
				.getString("movingVersionableConfirmTitle"), MessageFormat.format(VCSUIMessages //$NON-NLS-1$
				.getString("movingVersionableConfirm"), c.getName())); //$NON-NLS-1$
		if (confirmed) {
			ISelection s = (ISelection) aDropTargetEvent.data;
			if (s instanceof StructuredSelection) {
				List<IVersionable<?>> toMove = new ArrayList<IVersionable<?>>();
				for (Object o : ((IStructuredSelection) s).toArray()) {
					fillVersionablesFromObject(toMove, o);
				}
				getViewUIService().move(toMove, c);
				return Status.OK_STATUS;
			}
		}
		return Status.CANCEL_STATUS;
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

	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		if (operation == DND.DROP_MOVE || operation == DND.DROP_COPY) {
			if (target instanceof IVersionContainer) {
				return Status.OK_STATUS;
			}
		}
		return Status.CANCEL_STATUS;
	}

	public IWorkspaceUIService getViewUIService() {
		return VCSUIPlugin.getService(IWorkspaceUIService.class);
	}

}
