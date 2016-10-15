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
package com.neXtep.shared.model;

/**
 * Provides cross application services for 
 * delivery process. These methods are accessible by
 * both neXtep designer and neXtep installer.
 * 
 * @author Christophe
 *
 */
public class SharedDeliveryServices {

	/**
	 * Creates a control file name from a given repository file.
	 * 
	 * @param repositoryFilename filename of the artefact
	 * @return corresponding control file name
	 */
	public static String getControlFileName(String repositoryFilename) {
		int lastPoint = repositoryFilename.lastIndexOf(".");
		String ctrlName = null;
		if(lastPoint>0) {
			ctrlName = repositoryFilename.substring(0,lastPoint) + ".ctl";
		} else {
			ctrlName = repositoryFilename + ".ctl";
		}
		return ctrlName;
	}
}
