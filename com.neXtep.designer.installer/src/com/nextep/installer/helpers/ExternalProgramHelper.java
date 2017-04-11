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
package com.nextep.installer.helpers;

import com.nextep.installer.model.IInstallConfiguration;

/**
 * This helper provides common method to retrieve external programs locations.
 * 
 * @author Christophe Fondacci
 */
public final class ExternalProgramHelper {

	/** The property indicating the location of the binary program to use */
	static String PROP_BINARY_LOCATION = "admin.nextep.bin.location"; //$NON-NLS-1$

	private ExternalProgramHelper() {
	}

	public static String getProgramLocation(IInstallConfiguration configuration, String programName) {
		// Locating specified program binary
		String binaryLocation = configuration.getProperty(PROP_BINARY_LOCATION + "." //$NON-NLS-1$
				+ programName.toLowerCase());
		if (binaryLocation == null) {
			// Compatibility with older installer releases
			binaryLocation = configuration.getProperty(PROP_BINARY_LOCATION);

			// If nothing defined, we count on the global 'programName' aliased binary
			if (binaryLocation == null) {
				binaryLocation = programName;
			}
		}
		return binaryLocation;
	}
}
