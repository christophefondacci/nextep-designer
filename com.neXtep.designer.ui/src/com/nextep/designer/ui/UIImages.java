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
package com.nextep.designer.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class UIImages {

	private final static Log LOGGER = LogFactory.getLog(UIImages.class);

	public static final Image ICON_MARKER_ERROR = CoreUiPlugin.getImageDescriptor(
			"/resource/error_tsk.gif").createImage(); //$NON-NLS-1$
	public static final Image ICON_MARKER_WARNING = CoreUiPlugin.getImageDescriptor(
			"/resource/warn_tsk.gif").createImage(); //$NON-NLS-1$
	public static final Image WIZARD_NEW_CONNECTION = CoreUiPlugin.getImageDescriptor(
			"/resource/wizConnect.png").createImage(); //$NON-NLS-1$
	public static final Image ICON_SECURITY = CoreUiPlugin.getImageDescriptor(
			"/resource/userSecurity.gif").createImage(); //$NON-NLS-1$
	public static final Image WIZARD_GENERIC = CoreUiPlugin.getImageDescriptor(
			"/resource/dbObjects.png").createImage(); //$NON-NLS-1$
	public static final String RESOURCE_DROP_LARGE = "/resource/Drop.png"; //$NON-NLS-1$
	public static final Image DROP_LARGE = CoreUiPlugin.getImageDescriptor(RESOURCE_DROP_LARGE)
			.createImage();
	public static final Image ORACLE_ICON = CoreUiPlugin
			.getImageDescriptor("/resource/oracle.png").createImage(); //$NON-NLS-1$
	public static final Image MYSQL_ICON = CoreUiPlugin
			.getImageDescriptor("/resource/mysql.png").createImage(); //$NON-NLS-1$
	public static final Image POSTGRE_ICON = CoreUiPlugin.getImageDescriptor(
			"/resource/postgre.png").createImage(); //$NON-NLS-1$
	public static final Image DB2_ICON = CoreUiPlugin
			.getImageDescriptor("/resource/db2.png").createImage(); //$NON-NLS-1$
	public static final Image MSSQL_ICON = CoreUiPlugin.getImageDescriptor(
			"/resource/sqlserver.png").createImage(); //$NON-NLS-1$
	public static final Image JDBC_ICON = CoreUiPlugin
			.getImageDescriptor("/resource/jdbc.png").createImage(); //$NON-NLS-1$

	public static void dispose() {
		LOGGER.info("Disposing core UI resources...");

		ICON_MARKER_ERROR.dispose();
		ICON_MARKER_WARNING.dispose();
		WIZARD_NEW_CONNECTION.dispose();
		ICON_SECURITY.dispose();
		WIZARD_GENERIC.dispose();
		DROP_LARGE.dispose();

		ORACLE_ICON.dispose();
		MYSQL_ICON.dispose();
		POSTGRE_ICON.dispose();
		DB2_ICON.dispose();
		MSSQL_ICON.dispose();
		JDBC_ICON.dispose();
	}

}
