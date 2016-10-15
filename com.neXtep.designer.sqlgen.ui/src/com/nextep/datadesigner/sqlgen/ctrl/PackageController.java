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
package com.nextep.datadesigner.sqlgen.ctrl;

import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.gui.ObservableEditorGUI;
import com.nextep.datadesigner.sqlgen.gui.PackageNavigator;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.sqlgen.ui.PackageEditorInput;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 */
public class PackageController extends AbstractUIController {

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new ObservableEditorGUI((IPackage) content, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeGraphical(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeNavigator(java.lang.Object)
	 */
	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new PackageNavigator((IPackage) model, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeProperty(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	@Override
	public Object newInstance(Object parent) {
		IVersionContainer c = (IVersionContainer) parent;

		IVersionable<IPackage> versionable = VersionableFactory.createVersionable(IPackage.class);
		newWizardEdition("Package creation...", initializeEditor(versionable.getVersionnedObject()
				.getModel()));
		IPackage pkg = versionable.getVersionnedObject().getModel();
		pkg.setBodySourceCode("PACKAGE BODY " + pkg.getName() + " is\r\n\r\nend;");
		pkg.setSpecSourceCode("PACKAGE " + pkg.getName() + " is\r\n\r\nend;");
		// Saving versionable
		save(versionable);
		// Adding to container
		c.addVersionable(versionable, new ImportPolicyAddOnly());
		// Saving container
		CorePlugin.getIdentifiableDao().save(c);
		// Returning versionable
		return versionable;
	}

	@Override
	public Object emptyInstance(String name, Object parent) {
		IVersionable<IPackage> versionable = VersionableFactory.createVersionable(IPackage.class);
		versionable.setName(name);
		((IVersionContainer) parent).addVersionable(versionable, new ImportPolicyAddOnly());
		return versionable;
	}

	@Override
	public String getEditorId() {
		return "com.neXtep.designer.sqlgen.ui.packageEditor";
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new PackageEditorInput((IPackage) model);
	}
}
