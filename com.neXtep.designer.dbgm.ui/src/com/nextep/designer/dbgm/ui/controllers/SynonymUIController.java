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
package com.nextep.designer.dbgm.ui.controllers;

import com.nextep.datadesigner.dbgm.gui.SynonymEditorGUI;
import com.nextep.datadesigner.dbgm.gui.navigators.SynonymNavigator;
import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.gui.external.VersionableController;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.datadesigner.vcs.services.NamingService;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Bruno Gautier
 */
public class SynonymUIController extends VersionableController {

	public SynonymUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new SynonymEditorGUI((ISynonym) content, this);
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new SynonymNavigator((ISynonym) model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		return null;
	}

	/*
	 * FIXME [BGA]: This override should not be necessary since the main differences from the
	 * default implementation can be handled in the delegated method beforeCreation but it is left
	 * as a reminder for a future refactoring of the default implementation(s) in the abstraction
	 * layer (maybe by adding a specific hook for the wizard launching).
	 */
	@Override
	public Object newInstance(Object parent) {
		IVersionContainer container = (IVersionContainer) parent;

		/*
		 * FIXME [BGA]: Is it necessary to check if the container is modifiable since this check is
		 * already done by the NewTypedInstanceHandler#newInstance method in the following call?
		 */
		// VersionHelper.ensureModifiable(container, true);

		// Delegating the Synonym instantiation to the factory.
		IVersionable<?> synonym = VersionableFactory.createVersionable(getType().getInterface());

		/*
		 * FIXME [BGA]: The container should be registered before the delegated beforeEdition method
		 * is called so that the Naming Service can initialize the module's name variable if needed.
		 * But pre-registering the container can be problematic if the user choose to cancel the
		 * creation in the wizard. One solution could be to provide the container instance to the
		 * NamingService so that he can compute the module's name variable. For the moment, the
		 * module's name will be replaced by an empty string in the eventual naming pattern
		 * associated with the Synonym type.
		 */
		beforeEdition(synonym, container, parent);

		// Editing the newly created synonym.
		newWizardEdition(DBGMUIMessages.getString("wizard.title.create")
				+ DBGMUIMessages.getString("synonym.wizard.titlename") + "...",
				initializeEditor(synonym.getVersionnedObject().getModel()));

		// The synonym has been created, we can safely register the container.
		synonym.setContainer(container);

		beforeCreation(synonym, parent);

		// Saving the new synonym into repository database
		save(synonym);

		container.addVersionable(synonym, new ImportPolicyAddOnly());

		return synonym;
	}

	@Override
	protected void beforeEdition(IVersionable<?> v, IVersionContainer container, Object parent) {
		/*
		 * TODO [BGA]: The handling of the naming patterns should be done in the abstraction layer
		 * (AbstractController for example).
		 */
		NamingService.getInstance().adjustName(v);

		/*
		 * Count the number of synonyms already created in the container to compute a counter suffix
		 * to append to the synonym name. TODO [BGA]: This could be designed as a
		 * INamingVariableProvider.
		 */
		String adjustedSynName = v.getName();
		if (adjustedSynName != null && !adjustedSynName.trim().equals("")) {
			int synCount = VersionHelper.getAllVersionables(container,
					IElementType.getInstance(ISynonym.TYPE_ID)).size();
			v.setName(adjustedSynName + "_" + (synCount + 1));
		}

		super.beforeEdition(v, container, parent);
	}

}
