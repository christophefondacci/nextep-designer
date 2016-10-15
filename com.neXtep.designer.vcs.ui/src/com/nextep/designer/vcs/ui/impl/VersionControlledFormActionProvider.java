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
package com.nextep.designer.vcs.ui.impl;

import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.model.IFormActionProvider;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IVersioningUIService;

/**
 * @author Christophe Fondacci
 */
public class VersionControlledFormActionProvider implements IFormActionProvider {

	private IFormActionProvider actionProvider;

	public VersionControlledFormActionProvider(IFormActionProvider actionProvider) {
		this.actionProvider = actionProvider;
	}

	@Override
	public Object add(ITypedObject parent) {
		final IVersioningUIService versioningService = VCSUIPlugin
				.getService(IVersioningUIService.class);
		final ITypedObject unlockedParent = versioningService.ensureModifiable(parent);
		return actionProvider.add(unlockedParent);
	}

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
		final IVersioningUIService versioningService = VCSUIPlugin
				.getService(IVersioningUIService.class);
		final IModelOriented<ITypedObject> toRemoveProxy = VCSPlugin.getService(
				IVersioningService.class).createVersionAwareObject(toRemove);
		// Ensuring we got a modifiable parent element
		final ITypedObject unlockedParent = versioningService.ensureModifiable(parent);
		// Retrieving child element from proxy
		final ITypedObject unlockedToRemove = toRemoveProxy.getModel();

		// Delegating
		actionProvider.remove(unlockedParent, unlockedToRemove);
	}

	@Override
	public void up(ITypedObject parent, ITypedObject element) {
		final IVersioningUIService versioningService = VCSUIPlugin
				.getService(IVersioningUIService.class);
		final IModelOriented<ITypedObject> toRemoveProxy = VCSPlugin.getService(
				IVersioningService.class).createVersionAwareObject(element);
		// Ensuring we got a modifiable parent element
		final ITypedObject unlockedParent = versioningService.ensureModifiable(parent);
		// Retrieving child element from proxy
		final ITypedObject unlockedElement = toRemoveProxy.getModel();

		// Delegating
		actionProvider.up(unlockedParent, unlockedElement);
	}

	@Override
	public void down(ITypedObject parent, ITypedObject element) {
		final IVersioningUIService versioningService = VCSUIPlugin
				.getService(IVersioningUIService.class);
		final IModelOriented<ITypedObject> toRemoveProxy = VCSPlugin.getService(
				IVersioningService.class).createVersionAwareObject(element);
		// Ensuring we got a modifiable parent element
		final ITypedObject unlockedParent = versioningService.ensureModifiable(parent);
		// Retrieving child element from proxy
		final ITypedObject unlockedElement = toRemoveProxy.getModel();

		// Delegating
		actionProvider.down(unlockedParent, unlockedElement);
	}

	@Override
	public boolean isSortable() {
		return actionProvider.isSortable();
	}

	@Override
	public boolean isEditable() {
		return actionProvider.isEditable();
	}

	@Override
	public boolean isAddRemoveEnabled() {
		return actionProvider.isAddRemoveEnabled();
	}

	@Override
	public void edit(ITypedObject parent, ITypedObject element) {
		final IVersioningUIService versioningService = VCSUIPlugin
				.getService(IVersioningUIService.class);
		final IModelOriented<ITypedObject> toRemoveProxy = VCSPlugin.getService(
				IVersioningService.class).createVersionAwareObject(element);
		// Ensuring we got a modifiable parent element
		final ITypedObject unlockedParent = versioningService.ensureModifiable(parent);
		// Retrieving child element from proxy
		final ITypedObject unlockedElement = toRemoveProxy.getModel();

		// Delegating
		actionProvider.edit(unlockedParent, unlockedElement);
	}

}
