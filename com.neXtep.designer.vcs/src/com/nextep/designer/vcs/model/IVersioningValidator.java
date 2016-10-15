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
package com.nextep.designer.vcs.model;

import org.eclipse.core.runtime.IStatus;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.services.impl.VersioningService;

/**
 * A versioning validator can validate versioning actions like checkout, commit or undo checkouts.
 * It offers dedicated methods for each action it has to validate. Depdending on the configuration
 * defined on the running instance, validators may or may not be called by the
 * {@link IVersioningService}. <br>
 * These validators need to be injected in the {@link VersioningService} implementation as it may
 * not be useful for other implementations.
 * 
 * @author Christophe Fondacci
 */
public interface IVersioningValidator {

	/**
	 * Defines whether this validator is active for the specified versionable list. A validator
	 * needs to return <code>true</code> as soon as at least one element of the given list needs
	 * validation.
	 * 
	 * @param event the current {@link VersioningOperation}
	 * @param context context of the current versioning operation as a
	 *        {@link IVersioningOperationContext}
	 * @return <code>true</code> if this validator has anything to validate for this context /
	 *         event, else <code>false</code> in which case no other validation methods will be
	 *         called.
	 */
	boolean isActiveFor(IVersioningOperationContext context);

	/**
	 * Called for validation of a versioning operation. The returned status allows implementors to
	 * abort the process while providing details about the validation problem.<br>
	 * Validators may contribute to the current context.
	 * 
	 * @param context context of the current versioning operation as a
	 *        {@link IVersioningOperationContext}
	 * @return a validation status
	 */
	IStatus validate(IVersioningOperationContext context);

}
