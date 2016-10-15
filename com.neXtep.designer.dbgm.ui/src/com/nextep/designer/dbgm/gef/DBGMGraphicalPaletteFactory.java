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
package com.nextep.designer.dbgm.gef;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.SimpleFactory;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.designer.dbgm.ui.DbgmUIPlugin;

/**
 * Utility class that can create a GEF Palette.
 * @see #createPalette()
 * @author Elias Volanakis
 */
final class DBGMGraphicalPaletteFactory {

/** Preference ID used to persist the palette location. */
private static final String PALETTE_DOCK_LOCATION = "ShapesEditorPaletteFactory.Location";
/** Preference ID used to persist the palette size. */
private static final String PALETTE_SIZE = "ShapesEditorPaletteFactory.Size";
/** Preference ID used to persist the flyout palette's state. */
private static final String PALETTE_STATE = "ShapesEditorPaletteFactory.State";

/** Create the "Shapes" drawer. */
private static PaletteContainer createShapesDrawer() {
	PaletteDrawer componentsDrawer = new PaletteDrawer("Database",DbgmUIPlugin.getImageDescriptor("resource/DatabaseIconTiny.ico"));

	CombinedTemplateCreationEntry component = new CombinedTemplateCreationEntry(
			"Table",
			"Create a database table",
			IBasicTable.class,
			new SimpleFactory(IBasicTable.class),
			DbgmUIPlugin.getImageDescriptor("resource/TableIconTiny.ico"),
			DbgmUIPlugin.getImageDescriptor("resource/TableIconSmall.ico"));
	componentsDrawer.add(component);
	// Add (solid-line) connection tool
	ToolEntry createFKTool = new ConnectionCreationToolEntry(
			"Foreign Key",
			"Creates a foreign key connection",
			new SimpleFactory(ForeignKeyConstraint.class),
			DbgmUIPlugin.getImageDescriptor("resource/ForeignKeyTiny.ico"),
			DbgmUIPlugin.getImageDescriptor("resource/ForeignKeySmall.ico"));
	componentsDrawer.add(createFKTool);
	
	ToolEntry createUKTool = new CombinedTemplateCreationEntry(
			"Unique Key",
			"Creates a unique/primary key",
			UniqueKeyConstraint.class,
			new SimpleFactory(UniqueKeyConstraint.class),
			DbgmUIPlugin.getImageDescriptor("resource/PrimaryKeyTiny.ico"),
			DbgmUIPlugin.getImageDescriptor("resource/PrimaryKeySmall.ico"));
	componentsDrawer.add(createUKTool);
//	component = new CombinedTemplateCreationEntry(
//			"Rectangle",
//			"Create a rectangular shape",
//			RectangularShape.class,
//			new SimpleFactory(RectangularShape.class),
//			ImageDescriptor.createFromFile(ShapesPlugin.class, "icons/rectangle16.gif"),
//			ImageDescriptor.createFromFile(ShapesPlugin.class, "icons/rectangle24.gif"));
//	componentsDrawer.add(component);

	return componentsDrawer;
}

/**
 * Creates the PaletteRoot and adds all palette elements.
 * Use this factory method to create a new palette for your graphical editor.
 * @return a new PaletteRoot
 */
static PaletteRoot createPalette() {
	PaletteRoot palette = new PaletteRoot();
	palette.add(createToolsGroup(palette));
	palette.add(createShapesDrawer());
	return palette;
}

/** Create the "Tools" group. */
private static PaletteContainer createToolsGroup(PaletteRoot palette) {
	PaletteGroup toolGroup = new PaletteGroup("Tools");

	// Add a selection tool to the group
	ToolEntry tool = new PanningSelectionToolEntry();
	toolGroup.add(tool);
	palette.setDefaultEntry(tool);

	// Add a marquee tool to the group
	toolGroup.add(new MarqueeToolEntry());

	// Add a (unnamed) separator to the group
//	toolGroup.add(new PaletteSeparator());


//	toolGroup.add(tool);

//	// Add (dashed-line) connection tool
//	tool = new ConnectionCreationToolEntry(
//			"Dashed connection",
//			"Create a dashed-line connection",
//			new CreationFactory() {
//				public Object getNewObject() { return null; }
//				// see ShapeEditPart#createEditPolicies()
//				// this is abused to transmit the desired line style
//				public Object getObjectType() { return Connection.DASHED_CONNECTION; }
//			},
//			ImageDescriptor.createFromFile(ShapesPlugin.class, "icons/connection_d16.gif"),
//			ImageDescriptor.createFromFile(ShapesPlugin.class, "icons/connection_d24.gif"));
//	toolGroup.add(tool);

	return toolGroup;
}

/** Utility class. */
private DBGMGraphicalPaletteFactory() {
	// Utility class
}

}