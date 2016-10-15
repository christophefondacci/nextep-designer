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
package com.nextep.designer.repository;

/**
 * An enumeration indicating the status of the repository release compared
 * to the current neXtep client which attempts to connect to it.
 * 
 * @author Christophe
 *
 */
public enum RepositoryStatus {
	NO_CONNECTION,
	NO_REPOSITORY,
	/** Indicates that the current repository release is too low for the current neXtep client release */
	REPOSITORY_TOO_OLD,
	/** Indicates that the current neXtep client release is too low to connect to the current repository release */
	CLIENT_TOO_OLD,
	/** Indicates that the current repository release is aligned with the current neXtep client release */
	OK
}
