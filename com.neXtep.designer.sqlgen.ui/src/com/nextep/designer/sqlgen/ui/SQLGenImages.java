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
package com.nextep.designer.sqlgen.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;

/**
 * @author Christophe Fondacci
 */
public class SQLGenImages {

	private static final Log log = LogFactory.getLog(SQLGenImages.class);
	public static final Image ICON_SQL = Activator.getImageDescriptor(
			"/resource/SQLScriptIconSmall.ico").createImage();
	public static final Image ICON_SQL_TINY = Activator.getImageDescriptor(
			"/resource/SQLScriptIconTiny.ico").createImage();
	public static final Image ICON_HINT = Activator.getImageDescriptor("/resource/hintTiny.ico")
			.createImage();
	public static final Image ICON_BUILD_TINY = Activator.getImageDescriptor(
			"/resource/BuildTiny.ico").createImage();
	public static final Image ICON_BUILD_SMALL = Activator.getImageDescriptor(
			"/resource/BuildSmall.ico").createImage();

	public static final Image ICON_FILTER = Activator.getImageDescriptor("/resource/filter.gif")
			.createImage();
	public static final Image ICON_PUBLIC = Activator.getImageDescriptor("/resource/public_co.gif")
			.createImage();
	public static final Image ICON_PUBLIC_FIELD = Activator.getImageDescriptor(
			"/resource/field_protected_obj.gif").createImage();
	public static final Image ICON_FILTER_ADD = Activator.getImageDescriptor(
			"/resource/AddFilterTiny.ico").createImage();
	public static final Image ICON_FILTER_DEL = Activator.getImageDescriptor(
			"/resource/DelFilterTiny.ico").createImage();

	public static final Image ICON_CONSOLE = Activator.getImageDescriptor(
			"/resource/GenerationConsoleTiny.ico").createImage();
	public static final Image WIZARD_SCRIPT = Activator.getImageDescriptor(
			"/resource/wizScript.gif").createImage();

	public static void dispose() {
		log.info("Disposing SQL generator images...");
		ICON_SQL.dispose();
		ICON_SQL_TINY.dispose();
		ICON_HINT.dispose();
		ICON_PUBLIC.dispose();
		ICON_PUBLIC_FIELD.dispose();
		ICON_BUILD_TINY.dispose();
		ICON_FILTER.dispose();
		ICON_FILTER_ADD.dispose();
		ICON_FILTER_DEL.dispose();
		ICON_CONSOLE.dispose();
		ICON_BUILD_SMALL.dispose();
		WIZARD_SCRIPT.dispose();
	}
}
