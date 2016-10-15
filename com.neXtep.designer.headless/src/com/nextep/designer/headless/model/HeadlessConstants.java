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
package com.nextep.designer.headless.model;

/**
 * Global constants for the headless runtime (mainly command-line arguments names)
 * 
 * @author Christophe Fondacci
 */
public interface HeadlessConstants {

	String DB_USER_ARG = "user"; //$NON-NLS-1$
	String DB_PASSWORD_ARG = "password"; //$NON-NLS-1$
	String DB_DATABASE_ARG = "database"; //$NON-NLS-1$
	String DB_VENDOR_ARG = "vendor"; //$NON-NLS-1$
	String DB_PORT_ARG = "port"; //$NON-NLS-1$
	String DB_SERVER_ARG = "server"; //$NON-NLS-1$

	String TARGET_CONTEXT = "target"; //$NON-NLS-1$
	String REPOSITORY_CONTEXT = "repository"; //$NON-NLS-1$

	String CONFIG_FILE_ARG = "config.file"; //$NON-NLS-1$
	String EXPORT_FILENAME = "outfile"; //$NON-NLS-1$

	String TARGET_TASKS = "tasks"; //$NON-NLS-1$

	String VERBOSE = "verbose"; //$NON-NLS-1$
}
