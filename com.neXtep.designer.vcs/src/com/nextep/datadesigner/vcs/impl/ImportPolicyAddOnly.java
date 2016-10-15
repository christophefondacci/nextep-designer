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
/**
 *
 */
package com.nextep.datadesigner.vcs.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IImportPolicy;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.base.AbstractImportPolicy;

/**
 * An import policy which only adds non-existing objects to a version container.
 * 
 * @see com.nextep.designer.vcs.model.base.AbstractImportPolicy
 * @author Christophe Fondacci
 */
public class ImportPolicyAddOnly extends AbstractImportPolicy {

	private static final Log log = LogFactory.getLog(ImportPolicyAddOnly.class);
	private static IImportPolicy instance = null;

	public static IImportPolicy getInstance() {
		if (instance == null) {
			instance = new ImportPolicyAddOnly();
		}
		return instance;
	}

	/**
	 * @see com.nextep.designer.vcs.model.base.AbstractImportPolicy#existingObject(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionable, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected boolean existingObject(IVersionable<?> importing, IVersionable<?> existing,
			IActivity activity) {
		log.debug("AddOnlyPolicy: Existing collision in target container, skipping "
				+ importing.getType().getName().toLowerCase() + " <" + importing.getName() + ">");
		return false;
	}

	/**
	 * @see com.nextep.designer.vcs.model.base.AbstractImportPolicy#unexistingObject(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionContainer)
	 */
	@Override
	protected boolean unexistingObject(IVersionable<?> importing, IVersionContainer targetContainer) {
		// targetContainer.addVersionable(importing);
		try {
			log.debug("Adding " + importing.getType().getName().toLowerCase() + " "
					+ importing.getName() + " to container '" + targetContainer.getName() + "'");
			// Observable.deactivateListeners();
			CorePlugin.getIdentifiableDao().save(importing, true);
			targetContainer.getContents().add(importing);
			importing.setContainer(targetContainer);
			// Observable.activateListeners();
			targetContainer.notifyListeners(ChangeEvent.VERSIONABLE_ADDED, importing);
			VersionHelper.relink(importing);
			return true;
		} catch (Exception e) {
			log.error("Exception occurred during container import: " + e.getMessage(), e);
			return false;
		} finally {
			Observable.activateListeners();
		}
	}

	/**
	 * @see com.nextep.designer.vcs.model.base.AbstractImportPolicy#beforeImport(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionContainer,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected void beforeImport(IVersionable<?> v, IVersionContainer c, IActivity activity) {
		if (c.updatesLocked()) {
			throw new ErrorException("Import failed: target container <" + c.getName()
					+ "> is checked in. Please check out the container and try again.");
		}
	}

	@Override
	protected IVersionContainer getContainerForExistenceCheck(IVersionContainer targetContainer) {
		return VCSPlugin.getViewService().getCurrentWorkspace();
	}

	@Override
	public void finalizeImport() {
	}

}
