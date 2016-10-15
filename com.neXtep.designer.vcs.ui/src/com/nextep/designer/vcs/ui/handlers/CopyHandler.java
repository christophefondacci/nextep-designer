/*******************************************************************************
 * Copyright (c) 2012 neXtep Software and contributors.
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
package com.nextep.designer.vcs.ui.handlers;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.gui.impl.GUIWrapper;
import com.nextep.datadesigner.gui.impl.RenameConnector;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;
import com.nextep.designer.vcs.model.impl.Activity;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * @author Christophe Fondacci
 */
public class CopyHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection s = HandlerUtil.getCurrentSelection(event);
		if (!s.isEmpty() && s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) HandlerUtil
					.getCurrentSelection(event);
			if (sel.size() == 1 && sel.getFirstElement() instanceof IVersionable<?>) {
				final IVersionable<?> o = (IVersionable<?>) sel.getFirstElement();
				// Prompting for new name
				final RenameConnector connector = new RenameConnector((IObservable) o);
				GUIWrapper wrapper = new GUIWrapper(connector,
						VCSUIMessages.getString("handler.copy.title"), //$NON-NLS-1$
						300, 120);
				wrapper.invoke();
				if (!wrapper.isCancelled()) {
					String newName = connector.getNewName();
					if (newName == null || "".equals(newName.trim())) { //$NON-NLS-1$
						MessageDialog.openWarning(wrapper.getShell(),
								VCSUIMessages.getString("handler.rename.invalidTitle"), //$NON-NLS-1$
								VCSUIMessages.getString("handler.rename.invalidMsg")); //$NON-NLS-1$
					} else if (newName.equals(o.getName())) {
						MessageDialog.openWarning(wrapper.getShell(),
								VCSUIMessages.getString("handler.copy.invalidTitle"), //$NON-NLS-1$
								VCSUIMessages.getString("handler.copy.invalidMsg")); //$NON-NLS-1$
					} else {
						final IVersionable<?> copy = VersionableFactory.copy(o);
						copy.setName(newName);
						copy.setVersion(VersionFactory.getUnversionedInfo(
								new Reference(copy.getType(), copy.getName(), copy),
								Activity.getDefaultActivity()));
						// Assigning fresh new references to contained elements
						final Map<IReference, IReferenceable> refMap = copy.getReferenceMap();
						for (IReference r : refMap.keySet()) {
							final IReferenceable referenceable = refMap.get(r);
							referenceable.setReference(new Reference(r.getType(),
									((INamedObject) referenceable).getName(), referenceable));
						}
						// Adding to container
						o.getContainer().addVersionable(copy, new ImportPolicyAddOnly());
						return newName;
					}
				}
			}
		}
		return null;
	}

}
