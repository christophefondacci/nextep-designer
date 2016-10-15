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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.gui.VersionBranchEditor;
import com.nextep.datadesigner.vcs.gui.VersionableNavigator;
import com.nextep.datadesigner.vcs.impl.VersionBranch;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.ui.editors.DesignerSelectionProvider;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.model.IVersionBranch;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * Controller for versioning operations on IVersionable object. This controller is also a
 * ISelectionProvider which will fire a selectionChanged event on checkout when new objects are
 * created.
 * 
 * @author Christophe Fondacci
 */
public class VersionableController extends AbstractUIController implements ITypedObjectUIController {

	private static final Log log = LogFactory.getLog(VersionableController.class);
	private static VersionableController instance = null;
	private ISelectionProvider selectionProvider;

	public class ExtensionGetter implements IExecutableExtensionFactory {

		public Object create() throws CoreException {
			return VersionableController.getInstance();
		}
	}

	private VersionableController() {
		super();
		// addSaveEvent(ChangeEvent.MODEL_CHANGED);
		selectionProvider = new DesignerSelectionProvider(null, null);
	}

	public static VersionableController getInstance() {
		if (instance == null) {
			instance = new VersionableController();
		}
		return instance;
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		IVersionable<?> v = (IVersionable<?>) content;
		return UIControllerFactory.getController(v.getType()).initializeEditor(v);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	public void debranch(IVersionable<?> v) {
		// TODO : a impl�menter le Debranch
	}

	public Object newInstance(Object parent) {
		// No effect, cannot instantiate a versionable directly
		log.warn(VCSUIMessages.getString("controller.versionable.newVersionableError")); //$NON-NLS-1$
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeNavigator(java.lang.Object)
	 */
	public INavigatorConnector initializeNavigator(Object model) {

		return new VersionableNavigator((IVersionable<?>) model, this);
	}

	// *** REPLACED BY NO SAVE EVENT SET IN CONSTRUCTOR
	// /**
	// * This method overrides the default save behaviour of a generic
	// * controller. This implementation does nothing because a versionable
	// * object is abstract and though we assume that the save action will
	// * be performed by the related object specific controller.
	// *
	// * @see
	// com.nextep.datadesigner.ctrl.AbstractController#save(com.nextep.datadesigner.model.IdentifiedObject)
	// */
	// public void save(IdentifiedObject o) {
	// //
	// }
	// ***
	/**
	 * @see com.nextep.designer.ui.model.base.AbstractUIController#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (source instanceof IVersionInfo || source instanceof IVersionBranch) {
			CorePlugin.getIdentifiableDao().save((IdentifiedObject) source);
		}
	}

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	public IVersionBranch newBranch() {
		IVersionBranch branch = new VersionBranch("", ""); //$NON-NLS-1$ //$NON-NLS-2$
		newWizardEdition(
				VCSUIMessages.getString("controller.versionable.newBranch"), new VersionBranchEditor(branch)); //$NON-NLS-1$
		for (IVersionBranch b : VersionHelper.listBranches()) {
			if (b.getName().equalsIgnoreCase(branch.getName())) {
				branch = b;
				break;
			}
		}
		CorePlugin.getIdentifiableDao().save(branch);
		return branch;
	}
}
