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
package com.nextep.designer.vcs.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;

/**
 * @author Christophe Fondacci
 */
public class VCSImages {

	private static final Log log = LogFactory.getLog(VCSImages.class);
	public static Image ICON_VIEW = VCSUIPlugin.getImageDescriptor("/resource/ViewIconSmall.ico")
			.createImage();
	public static final Image ICON_NEW_CONTAINER_TINY = VCSUIPlugin.getImageDescriptor(
			"/resource/NewContainerTiny.ico").createImage();
	public static final Image ICON_CONTAINER_TINY = VCSUIPlugin.getImageDescriptor(
			"/resource/ViewIconTiny.ico").createImage();
	public static final Image ICON_NEW_USER_TINY = VCSUIPlugin.getImageDescriptor(
			"/resource/NewUserTiny.ico").createImage();
	public static final Image ICON_USERS = VCSUIPlugin.getImageDescriptor(
			"/resource/UsersSmall.ico").createImage();
	public static final Image ICON_USERS_TINY = VCSUIPlugin.getImageDescriptor(
			"/resource/UsersTiny.gif").createImage();
	public static final Image ICON_USER_TINY = VCSUIPlugin.getImageDescriptor(
			"/resource/UserTiny.gif").createImage();
	public static final Image ICON_USER_DIS_TINY = VCSUIPlugin.getImageDescriptor(
			"/resource/UserDisabledTiny.gif").createImage();
	public static final Image ICON_USER = VCSUIPlugin.getImageDescriptor("/resource/UserSmall.gif")
			.createImage();
	public static final Image ICON_CHANGE_VIEW = VCSUIPlugin.getImageDescriptor(
			"/resource/ChangeViewSmall.ico").createImage();
	// Version compare
	public static final Image ICON_EQUALS = VCSUIPlugin.getImageDescriptor(
			"/resource/differ_filter.png").createImage();
	public static final Image ICON_DIFF = VCSUIPlugin.getImageDescriptor("/resource/DiffTiny.ico")
			.createImage();
	public static final Image ICON_DIFF_ADDED = VCSUIPlugin.getImageDescriptor(
			"/resource/DiffAddTiny.png").createImage();
	public static final Image ICON_DIFF_REMOVED = VCSUIPlugin.getImageDescriptor(
			"/resource/DiffRemoveTiny.png").createImage();
	public static final Image ICON_DIFF_REMOVED_SMALL = VCSUIPlugin.getImageDescriptor(
			"/resource/DiffRemoveSmall.ico").createImage();
	public static final Image ICON_DIFF_CHANGED = VCSUIPlugin.getImageDescriptor(
			"/resource/DiffDiffTiny.png").createImage();
	public static final Image ICON_MERGE = VCSUIPlugin
			.getImageDescriptor("/resource/MergeTiny.ico").createImage();
	public static final Image ICON_VERSIONTREE = VCSUIPlugin.getImageDescriptor(
			"/resource/VersionTreeSmall.ico").createImage();

	public static final Image ICON_REFERENCE = VCSUIPlugin.getImageDescriptor(
			"/resource/linkSmall.ico").createImage();
	public static final Image ICON_REFERENCE_TINY = VCSUIPlugin.getImageDescriptor(
			"/resource/linkTiny.ico").createImage();
	public static final Image ICON_COLLAPSE_ALL = VCSUIPlugin.getImageDescriptor(
			"/resource/collapseall.gif").createImage();

	public static final Image ICON_ZOOMOUT = VCSUIPlugin.getImageDescriptor(
			"/resource/ZoomOutTiny.ico").createImage();
	public static final Image ICON_ZOOMIN = VCSUIPlugin.getImageDescriptor(
			"/resource/ZoomInTiny.ico").createImage();
	public static final Image ICON_HISTORY = VCSUIPlugin
			.getImageDescriptor("/resource/history.gif").createImage();
	public static final Image ICON_DEPENDENCIES = VCSUIPlugin.getImageDescriptor(
			"/resource/call_hierarchy.gif").createImage();
	public static final Image ICON_RESOLVE = VCSUIPlugin.getImageDescriptor(
			"/resource/ResolveImportCheck.ico").createImage();
	public static final Image ICON_VERSION_NAVIGATOR = VCSUIPlugin.getImageDescriptor(
			"/resource/NavigatorViewIcon.ico").createImage();
	public static final Image ICON_VALIDATION = VCSUIPlugin.getImageDescriptor(
			"/resource/validation.png").createImage();
	public static final Image ICON_VERSION_SETTINGS = VCSUIPlugin.getImageDescriptor(
			"/resource/versionSettings.png").createImage();

	public static final Image ICON_COMMIT = VCSUIPlugin.getImageDescriptor(
			"/resource/CheckInSmall.ico").createImage();
	public static final Image ICON_CHECKOUT = VCSUIPlugin.getImageDescriptor(
			"/resource/CheckOutSmall.ico").createImage();
	public static final Image ICON_UNDO_CHECKOUT = VCSUIPlugin.getImageDescriptor(
			"/resource/UndoCheckOutSmall.ico").createImage();

	public static final Image WIZ_USERS = VCSUIPlugin.getImageDescriptor("/resource/wizUsers.png")
			.createImage();
	public static final Image WIZ_UNDO = VCSUIPlugin.getImageDescriptor("/resource/wiz_undo.png")
			.createImage();
	public static final Image WIZ_INSTALL = VCSUIPlugin.getImageDescriptor(
			"/resource/driver_installation.png").createImage();

	public static void setTiny(boolean tiny) {
		if (tiny) {
			ICON_VIEW = VCSUIPlugin.getImageDescriptor("/resource/ViewIconTiny.ico").createImage();
		} else {
			ICON_VIEW = VCSUIPlugin.getImageDescriptor("/resource/ViewIconSmall.ico").createImage();
		}
	}

	public static void dispose() {
		log.debug("Disposing VCS image resources...");
		ICON_VIEW.dispose();
		ICON_NEW_CONTAINER_TINY.dispose();
		ICON_CONTAINER_TINY.dispose();
		ICON_NEW_USER_TINY.dispose();
		ICON_USERS.dispose();
		ICON_USERS_TINY.dispose();
		ICON_USER_TINY.dispose();
		ICON_USER_DIS_TINY.dispose();
		ICON_USER.dispose();
		ICON_CHANGE_VIEW.dispose();

		ICON_EQUALS.dispose();
		ICON_DIFF.dispose();
		ICON_DIFF_ADDED.dispose();
		ICON_DIFF_REMOVED.dispose();
		ICON_DIFF_REMOVED_SMALL.dispose();
		ICON_DIFF_CHANGED.dispose();
		ICON_MERGE.dispose();
		ICON_VERSIONTREE.dispose();
		ICON_REFERENCE.dispose();
		ICON_REFERENCE_TINY.dispose();
		ICON_COLLAPSE_ALL.dispose();
		ICON_ZOOMOUT.dispose();
		ICON_ZOOMIN.dispose();
		ICON_HISTORY.dispose();
		ICON_DEPENDENCIES.dispose();
		ICON_RESOLVE.dispose();
		ICON_VERSION_NAVIGATOR.dispose();
		ICON_VALIDATION.dispose();
		ICON_VERSION_SETTINGS.dispose();

		ICON_COMMIT.dispose();
		ICON_CHECKOUT.dispose();
		ICON_UNDO_CHECKOUT.dispose();

		WIZ_USERS.dispose();
		WIZ_UNDO.dispose();
		WIZ_INSTALL.dispose();
	}
}
