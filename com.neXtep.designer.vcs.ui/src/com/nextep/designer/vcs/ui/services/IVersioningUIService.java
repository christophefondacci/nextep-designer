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
package com.nextep.designer.vcs.ui.services;

import org.eclipse.swt.widgets.Shell;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.services.ICoreVersioningService;

/**
 * This interace provides UI-related versioning services to the platform. Any of the method may
 * interact with the user.
 * 
 * @author Christophe Fondacci
 */
public interface IVersioningUIService extends ICoreVersioningService {

	/**
	 * Prompts the user to select a previous version of the specified versionable element
	 * 
	 * @param v {@link IVersionable} element to pick a previous version for
	 * @return the {@link IVersionInfo} selected by the user
	 * @throws CancelException if the user cancel version selection
	 */
	IVersionInfo pickPreviousVersion(IVersionable<?> v, String title);

	/**
	 * Prompts the user to select a previous version of the specified versionable element. The
	 * specified Shell argument will be used as the parent shell of the new dialog.
	 * 
	 * @param parentShell parent shell for the version selection dialog
	 * @param v the {@link IVersionable} to pick a version for
	 * @param title title of the dialog to display to the user
	 * @return the selected version {@link IVersionInfo}
	 * @throws CancelException whenever the user cancelled the operation
	 */
	IVersionInfo pickPreviousVersion(Shell parentShell, IVersionable<?> v, String title);
}
