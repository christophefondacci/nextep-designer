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
package com.nextep.designer.beng.ui.services;

import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.designer.beng.model.IDeliveryModule;

public interface IDeliveryExportUIService {

	/**
	 * Generates the delivery to the specified filesystem directory.
	 * 
	 * @param targetDirectory directory where the artefact should be generated.
	 * @param module the {@link IDeliveryModule} to export
	 * @param the {@link IProgressMonitor} to report progress to
	 */
	void exportDelivery(final IDeliveryModule module);
}
