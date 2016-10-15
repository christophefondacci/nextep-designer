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
package com.nextep.datadesigner.vcs.gui.external;

import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 */
public abstract class VersionableController extends AbstractUIController {

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	@Override
	public Object newInstance(Object parent) {
		IVersionContainer container = emptyInstanceGetContainer(parent);
		// Checking if parent is modifiable
		container = VCSUIPlugin.getVersioningUIService().ensureModifiable(container);
		// Creating versionable from factory
		IVersionable<?> v = VersionableFactory.createVersionable(getType().getInterface());
		beforeEdition(v, container, parent);
		// Editing
		newWizardEdition("Creating new " + getType().getName() + "...", initializeEditor(v
				.getVersionnedObject().getModel()));
		beforeCreation(v, parent);
		// Registering container & saving
		v.setContainer(container);
		save(v);
		// Adding to container
		container.addVersionable(v, new ImportPolicyAddOnly());
		// Returning built object
		return v;
	}

	/**
	 * Performs custom initialization before displaying the "new creation" wizard. Objects should
	 * typically be saved before edition when they contain sub-elements which may be created by the
	 * user during edition.<br>
	 * Subclasses may override to change the default empty implementation.
	 * 
	 * @return <code>true</code> to save the object before creation wizard else <code>false</code>
	 */
	protected void beforeEdition(IVersionable<?> v, IVersionContainer container, Object parent) {
	}

	/**
	 * @see com.nextep.designer.ui.model.base.AbstractUIController#emptyInstance(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public Object emptyInstance(String name, Object parent) {
		IVersionContainer container = emptyInstanceGetContainer(parent);
		// Checking if parent is modifiable
		container = VCSUIPlugin.getVersioningUIService().ensureModifiable(container);
		// Creating versionable from factory
		IVersionable<?> v = VersionableFactory.createVersionable(getType().getInterface());
		v.setName(getAvailableName(getType(), name));
		beforeCreation(v, parent);
		v.setContainer(container);
		save(v);
		// Adding to container
		container.addVersionable(v, new ImportPolicyAddOnly());
		// Returning built object
		return v;
	}

	protected IVersionContainer emptyInstanceGetContainer(Object parent) {
		return (IVersionContainer) parent;
	}

	/**
	 * This method is called just before a new object will be inserted in database to perform some
	 * initialization of the new object. The default implementation does nothing and could be
	 * extended by implementors which need to initialize the model before it is created.
	 * 
	 * @param o the new object which is about to be inserted.
	 */
	protected void beforeCreation(IVersionable<?> v, Object parent) {
	}

}
