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
package com.nextep.designer.beng.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;

/**
 * @author Christophe Fondacci
 */
public class BENGImages {

	private static final Log log = LogFactory.getLog(BENGImages.class);
	public static final Image ICON_DEPLOY_UNIT = BengUIPlugin.getImageDescriptor(
			"/resource/ModuleIconTiny.ico").createImage(); //$NON-NLS-1$
	public static final Image ICON_DELIVERY = BengUIPlugin.getImageDescriptor(
			"/resource/DeliveryIconTiny.ico").createImage(); //$NON-NLS-1$
	public static Image ICON_CONTAINER = BengUIPlugin.getImageDescriptor(
			"/resource/ViewIconSmall.ico").createImage(); //$NON-NLS-1$
	public static Image ICON_EXPORT = BengUIPlugin
			.getImageDescriptor("/resource/export_wiz.gif").createImage(); //$NON-NLS-1$
	public static Image ICON_NEW_DEPENDENCY = BengUIPlugin.getImageDescriptor(
			"/resource/NewDependencyTiny.ico").createImage(); //$NON-NLS-1$
	public static Image ICON_DEPENDENCY = BengUIPlugin.getImageDescriptor(
			"/resource/dependencyTiny.ico").createImage(); //$NON-NLS-1$
	public static Image ICON_DEL_DEPENDENCY = BengUIPlugin.getImageDescriptor(
			"/resource/DelDependencyTiny.ico").createImage(); //$NON-NLS-1$
	public static Image ICON_REFRESH = BengUIPlugin
			.getImageDescriptor("/resource/SyncTiny.ico").createImage(); //$NON-NLS-1$
	public static Image ICON_COMPUTEBUILD = BengUIPlugin.getImageDescriptor(
			"/resource/ComputeBuildSetTiny.ico").createImage(); //$NON-NLS-1$
	public static Image ICON_NEW_BUILD_ITEM = BengUIPlugin.getImageDescriptor(
			"/resource/NewBuildItemTiny.ico").createImage(); //$NON-NLS-1$
	public static Image ICON_DEL_BUILD_ITEM = BengUIPlugin.getImageDescriptor(
			"/resource/DelBuildItemTiny.ico").createImage(); //$NON-NLS-1$
	public static Image ICON_BUILD = BengUIPlugin
			.getImageDescriptor("/resource/BuildTiny.ico").createImage(); //$NON-NLS-1$
	public static Image ICON_ADD_EXTERNAL = BengUIPlugin.getImageDescriptor(
			"/resource/AddExternalFile.gif").createImage(); //$NON-NLS-1$
	public static Image ICON_DEL_EXTERNAL = BengUIPlugin.getImageDescriptor(
			"/resource/DelExternalFileTiny.ico").createImage(); //$NON-NLS-1$

	public static Image WIZ_NEW_DELIVERY = BengUIPlugin.getImageDescriptor(
			"/resource/packages_folder.png").createImage(); //$NON-NLS-1$

	public static void dispose() {
		log.info("Disposing Build engine resources..."); //$NON-NLS-1$
		ICON_DEPLOY_UNIT.dispose();
		ICON_DELIVERY.dispose();
		ICON_CONTAINER.dispose();
		ICON_EXPORT.dispose();
		ICON_NEW_DEPENDENCY.dispose();
		ICON_DEPENDENCY.dispose();
		ICON_DEL_DEPENDENCY.dispose();
		ICON_REFRESH.dispose();
		ICON_COMPUTEBUILD.dispose();
		ICON_BUILD.dispose();
		ICON_NEW_BUILD_ITEM.dispose();
		ICON_DEL_BUILD_ITEM.dispose();
		ICON_ADD_EXTERNAL.dispose();
		ICON_DEL_EXTERNAL.dispose();
		WIZ_NEW_DELIVERY.dispose();
	}
}
