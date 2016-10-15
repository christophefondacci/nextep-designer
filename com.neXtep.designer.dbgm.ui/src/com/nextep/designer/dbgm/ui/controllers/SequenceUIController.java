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

import com.nextep.datadesigner.dbgm.gui.SequenceEditorGUI;
import com.nextep.datadesigner.dbgm.gui.navigators.SequenceNavigator;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 */
public class SequenceUIController extends AbstractUIController {

	/**
	 *
	 */
	public SequenceUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new SequenceEditorGUI((ISequence) content, this);
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
		return new SequenceNavigator((ISequence) model, this);
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
		IVersionContainer container = (IVersionContainer) parent;
		int seqCount = VersionHelper.getAllVersionables(container,
				IElementType.getInstance("SEQUENCE")).size();
		// Creating versionable
		IVersionable<ISequence> s = VersionableFactory.createVersionable(ISequence.class);
		if (container.getShortName() != null && !"".equals(container.getShortName())) {
			s.setName(container.getShortName() + "_SEQ_" + (seqCount + 1));
		}
		// Editing
		newWizardEdition("Sequence creation...", initializeEditor(s.getVersionnedObject()
				.getModel()));
		save(s);
		container.addVersionable(s, new ImportPolicyAddOnly());
		return s;
	}

	@Override
	public Object emptyInstance(String name, Object parent) {
		IVersionable<ISequence> s = VersionableFactory.createVersionable(ISequence.class);
		s.setName(name);
		((IVersionContainer) parent).addVersionable(s, new ImportPolicyAddOnly());
		return s;
	}
}
