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
package com.nextep.datadesigner.gui.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;

/**
 * @author Christophe Fondacci
 */
public class FontFactory {

	private static final Log log = LogFactory.getLog(FontFactory.class);
	public static final Color DATATYPE_COLOR = new Color(Display.getDefault(), 0, 0, 128);
	public static final Color DDL_COLOR = new Color(Display.getDefault(), 128, 0, 64);
	public static final Color DML_COLOR = new Color(Display.getDefault(), 0, 0, 0);
	public static final Color FUNC_COLOR = new Color(Display.getDefault(), 0, 0, 0);
	public static final Color PLVAR_COLOR = new Color(Display.getDefault(), 0, 0, 160);
	public static final Color PROMPT_COLOR = new Color(Display.getDefault(), 63, 127, 95);
	public static final Color QUOTE_COLOR = new Color(Display.getDefault(), 0, 0, 200);
	public static final Color CHECKIN_COLOR = new Color(Display.getDefault(), 0, 0, 160);
	public static final Color CHECKOUT_COLOR = new Color(Display.getDefault(), 0, 0, 0);
	public static final Color COMMENT_COLOR = new Color(Display.getDefault(), 63, 95, 191);

	public static final Color SQL_SHADOW_COLOR0 = new Color(Display.getDefault(), 113, 185, 149);
	public static final Color SQL_SHADOW_COLOR1 = new Color(Display.getDefault(), 63, 127, 95);
	public static final Color SQL_SHADOW_COLOR2 = new Color(Display.getDefault(), 49, 100, 74);

	public static final Color VERSIONTREE_CHECKIN_COLOR = new Color(Display.getDefault(), 185, 200,
			246);
	public static final Color VERSIONTREE_CHECKOUT_COLOR = new Color(Display.getDefault(), 205,
			246, 185);
	public static final Color VERSIONTREE_LIGHT_COLOR = new Color(Display.getDefault(), 226, 255,
			241);

	private static final FontData fontData = Display.getDefault().getSystemFont().getFontData()[0];
	public static final Font FONT_TINIEST = new Font(Display.getDefault(), fontData.getName(),
			fontData.getHeight() - 4, SWT.BOLD);
	public static final Font FONT_TINY = new Font(Display.getDefault(), fontData.getName(),
			fontData.getHeight() - 2, SWT.BOLD);
	public static final Font FONT_BOLD = new Font(Display.getDefault(), fontData.getName(),
			fontData.getHeight(), SWT.BOLD);
	public static final Font FONT_ITALIC = new Font(Display.getDefault(), fontData.getName(),
			fontData.getHeight(), SWT.ITALIC);
	public static final Font FONT_SCRIPT = new Font(Display.getDefault(), "Courier New", fontData
			.getHeight(), SWT.NONE);
	public static final Color ERROR_COLOR = new Color(Display.getDefault(), 230, 0, 0);
	public static final Color WARN_COLOR = new Color(Display.getDefault(), 0, 0, 230);

	public static final Color SHADOW_TEXT_COLOR = new Color(Display.getDefault(), 202, 189, 172);
	public static final Color SHADOW_GRAPH0_COLOR = new Color(Display.getDefault(), 170, 170, 170);
	public static final Color SHADOW_GRAPH1_COLOR = new Color(Display.getDefault(), 200, 200, 200);
	public static final Color SHADOW_GRAPH2_COLOR = new Color(Display.getDefault(), 220, 220, 220);
	public static final Color SHADOW_GRAPH3_COLOR = new Color(Display.getDefault(), 230, 230, 230);
	public static final Color SHADOW_GRAPH4_COLOR = new Color(Display.getDefault(), 240, 240, 240);
	public static final Color SHADOW_GRAPH5_COLOR = new Color(Display.getDefault(), 250, 250, 250);
	public static final Color BLACK = new Color(Display.getDefault(), 0, 0, 0);
	public static final Color WHITE = new Color(Display.getDefault(), 255, 255, 255);
	public static final Color LIGHT_RED = new Color(Display.getDefault(), 255, 7, 25);
	public static final Color LIGHT_RED_ERROR = new Color(Display.getDefault(), 247, 185, 186);
	public static final Color LIGHT_YELLOW = new Color(Display.getDefault(), 255, 255, 221);
	public static final Color DIAGRAM_CHILD_TABLE_COLOR = new Color(Display.getDefault(), 207, 227,
			250);
	public static final Color DIAGRAM_PARENT_TABLE_COLOR = new Color(Display.getDefault(), 207,
			250, 227);

	public static final Color VERSIONABLE_DECORATOR_COLOR = new Color(Display.getDefault(), 171,
			142, 120);

	// Comparison colors
	public static final Color COMPARISON_ADDED = new Color(Display.getDefault(), 192, 255, 192);
	public static final Color COMPARISON_REMOVED = new Color(Display.getDefault(), 255, 160, 160);
	public static final Color COMPARISON_DIFFER = new Color(Display.getDefault(), 255, 255, 128);

	public static TextLayout versionableLayout = new TextLayout(Display.getCurrent());

	public static void dispose() {
		log.debug("Disposing font resources...");
		DATATYPE_COLOR.dispose();
		DDL_COLOR.dispose();
		DML_COLOR.dispose();
		FUNC_COLOR.dispose();
		PLVAR_COLOR.dispose();
		PROMPT_COLOR.dispose();
		QUOTE_COLOR.dispose();
		FONT_TINIEST.dispose();
		FONT_TINY.dispose();
		FONT_BOLD.dispose();
		FONT_ITALIC.dispose();
		FONT_SCRIPT.dispose();
		CHECKIN_COLOR.dispose();
		CHECKOUT_COLOR.dispose();
		COMMENT_COLOR.dispose();
		SQL_SHADOW_COLOR0.dispose();
		SQL_SHADOW_COLOR1.dispose();
		VERSIONTREE_CHECKIN_COLOR.dispose();
		VERSIONTREE_CHECKOUT_COLOR.dispose();
		VERSIONTREE_LIGHT_COLOR.dispose();
		SHADOW_TEXT_COLOR.dispose();
		SHADOW_GRAPH0_COLOR.dispose();
		SHADOW_GRAPH1_COLOR.dispose();
		SHADOW_GRAPH2_COLOR.dispose();
		SHADOW_GRAPH3_COLOR.dispose();
		SHADOW_GRAPH4_COLOR.dispose();
		SHADOW_GRAPH5_COLOR.dispose();
		BLACK.dispose();
		ERROR_COLOR.dispose();
		WARN_COLOR.dispose();
		WHITE.dispose();
		LIGHT_RED.dispose();
		LIGHT_RED_ERROR.dispose();
		LIGHT_YELLOW.dispose();
		DIAGRAM_CHILD_TABLE_COLOR.dispose();
		DIAGRAM_PARENT_TABLE_COLOR.dispose();
		VERSIONABLE_DECORATOR_COLOR.dispose();

		COMPARISON_ADDED.dispose();
		COMPARISON_DIFFER.dispose();
		COMPARISON_REMOVED.dispose();

		versionableLayout.dispose();
	}
}
