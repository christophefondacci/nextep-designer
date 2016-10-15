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
package com.nextep.designer.dbgm.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author Christophe Fondacci
 */
public class DBGMImages {

	private static final Log log = LogFactory.getLog(DBGMImages.class);
	public static final Image ICON_TABLE = DbgmUIPlugin.getImageDescriptor(
			"/resource/TableIconSmall.ico").createImage();
	public static final Image ICON_TABLE_TYPE = DbgmUIPlugin.getImageDescriptor(
			"/resource/TableTypeIconSmall.ico").createImage();
	public static final Image ICON_FUNC = DbgmUIPlugin.getImageDescriptor("/resource/funcTiny.ico")
			.createImage();
	public static final Image ICON_NEWCOLUMN = DbgmUIPlugin.getImageDescriptor(
			"/resource/NewColumnIconTiny.ico").createImage();
	public static final Image ICON_COLUMN = DbgmUIPlugin.getImageDescriptor(
			"/resource/ColumnIconSmall.ico").createImage();
	public static final Image ICON_COLUMN_TYPE = DbgmUIPlugin.getImageDescriptor(
			"/resource/ColumnTypeIconSmall.ico").createImage();
	public static final Image ICON_GRAPH = DbgmUIPlugin.getImageDescriptor(
			"/resource/GraphIconSmall.ico").createImage();
	public static final Image ICON_ALPHATYPE = DbgmUIPlugin.getImageDescriptor(
			"/resource/AlphaTypeSmall.ico").createImage();
	public static final Image ICON_NUMTYPE = DbgmUIPlugin.getImageDescriptor(
			"/resource/ColumnIconSmall.ico").createImage();
	public static final Image ICON_DATETYPE = DbgmUIPlugin.getImageDescriptor(
			"/resource/DateTypeSmall.ico").createImage();
	public static final Image ICON_KEYS = DbgmUIPlugin
			.getImageDescriptor("/resource/KeysSmall.ico").createImage();
	public static final Image ICON_PK = DbgmUIPlugin.getImageDescriptor(
			"/resource/PrimaryKeySmall.ico").createImage();
	public static final Image ICON_FK = DbgmUIPlugin.getImageDescriptor(
			"/resource/ForeignKeySmall.ico").createImage();
	public static final Image ICON_DATASET = DbgmUIPlugin.getImageDescriptor(
			"/resource/DataSetIconTiny.png").createImage();
	public static final Image ICON_DATALINE = DbgmUIPlugin.getImageDescriptor(
			"/resource/DataLineSmall.ico").createImage();
	public static final Image ICON_NEWDATALINE = DbgmUIPlugin.getImageDescriptor(
			"/resource/NewDataLineTiny.ico").createImage();

	public static final Image ICON_DATABASE_TINY = DbgmUIPlugin.getImageDescriptor(
			"/resource/DatabaseIconTiny.ico").createImage();

	public static final Image ICON_PUBLIC_FIELD = DbgmUIPlugin.getImageDescriptor(
			"/resource/field_protected_obj.gif").createImage();
	// Tiny icons
	public static final Image ICON_TABLE_TINY = DbgmUIPlugin.getImageDescriptor(
			"/resource/TableIconTiny.ico").createImage();
	public static final Image ICON_ALPHATYPE_TINY = DbgmUIPlugin.getImageDescriptor(
			"/resource/AlphaTypeTiny.ico").createImage();
	public static final Image ICON_NUMTYPE_TINY = DbgmUIPlugin.getImageDescriptor(
			"/resource/ColumnIconTiny.ico").createImage();
	public static final Image ICON_DATETYPE_TINY = DbgmUIPlugin.getImageDescriptor(
			"/resource/DateTypeTiny.ico").createImage();
	public static final Image ICON_COLUMN_TYPE_TINY = DbgmUIPlugin.getImageDescriptor(
			"/resource/ColumnIconTiny.ico").createImage();
	public static final Image ICON_PK_TINY = DbgmUIPlugin.getImageDescriptor(
			"/resource/PrimaryKeyTiny.ico").createImage();
	public static final Image ICON_ADD_FILE = DbgmUIPlugin.getImageDescriptor(
			"/resource/addFile.ico").createImage();
	public static final Image ICON_DEL_FILE = DbgmUIPlugin.getImageDescriptor(
			"/resource/delFile.ico").createImage();
	public static final Image ICON_FILE = DbgmUIPlugin.getImageDescriptor("/resource/fileTiny.ico")
			.createImage();
	public static final Image DECORATOR_FK = DbgmUIPlugin.getImageDescriptor(
			"/resource/fk_decorator.png").createImage();
	public static final Image WIZARD_TRIGGER = DbgmUIPlugin.getImageDescriptor(
			"/resource/wizTrigger.png").createImage();
	public static final Image WIZARD_TABLE = DbgmUIPlugin.getImageDescriptor(
			"/resource/wizTables.png").createImage();
	public static final Image WIZARD_SYNONYM = DbgmUIPlugin.getImageDescriptor(
			"/resource/wizSynonyms.png").createImage();
	public static final Image WIZARD_INDEX = DbgmUIPlugin.getImageDescriptor(
			"/resource/wizIndex.png").createImage();
	public static final Image WIZARD_KEY = DbgmUIPlugin.getImageDescriptor("/resource/wizKey.png")
			.createImage();
	public static final Image WIZARD_TYPE = DbgmUIPlugin.getImageDescriptor(
			"/resource/wizUserType.png").createImage();
	public static final Color[] INDEX_COLORS = new Color[] {
			new Color(Display.getCurrent(), 0, 0, 0), new Color(Display.getCurrent(), 18, 0, 228),
			new Color(Display.getCurrent(), 0, 132, 7),
			new Color(Display.getCurrent(), 179, 0, 41),
			new Color(Display.getCurrent(), 128, 95, 0),
			new Color(Display.getCurrent(), 206, 10, 255) };
	// Gef icons
	public static final Image ICON_GRID = DbgmUIPlugin.getImageDescriptor("/resource/GridTiny.ico")
			.createImage();

	public static void dispose() {
		log.debug("Disposing DBGM image resources...");
		ICON_TABLE.dispose();
		ICON_TABLE_TYPE.dispose();
		ICON_FUNC.dispose();
		ICON_NEWCOLUMN.dispose();
		ICON_COLUMN.dispose();
		ICON_COLUMN_TYPE.dispose();
		ICON_GRAPH.dispose();
		ICON_ALPHATYPE.dispose();
		ICON_NUMTYPE.dispose();
		ICON_DATETYPE.dispose();
		ICON_KEYS.dispose();
		ICON_PK.dispose();
		ICON_FK.dispose();
		ICON_DATASET.dispose();
		ICON_DATALINE.dispose();
		ICON_NEWDATALINE.dispose();
		ICON_DATABASE_TINY.dispose();
		ICON_PUBLIC_FIELD.dispose();
		ICON_TABLE_TINY.dispose();
		ICON_ALPHATYPE_TINY.dispose();
		ICON_NUMTYPE_TINY.dispose();
		ICON_DATETYPE_TINY.dispose();
		ICON_COLUMN_TYPE_TINY.dispose();
		ICON_PK_TINY.dispose();
		ICON_ADD_FILE.dispose();
		ICON_DEL_FILE.dispose();
		ICON_FILE.dispose();
		DECORATOR_FK.dispose();
		WIZARD_TRIGGER.dispose();
		WIZARD_TABLE.dispose();
		WIZARD_SYNONYM.dispose();
		WIZARD_INDEX.dispose();
		WIZARD_KEY.dispose();
		WIZARD_TYPE.dispose();
		for (Color c : INDEX_COLORS) {
			c.dispose();
		}
	}
}
