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
/*
 * Created on Jul 13, 2004
 */
package com.nextep.designer.dbgm.gef.figures;

import java.util.List;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.handles.HandleBounds;
import org.eclipse.swt.graphics.Color;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.dbgm.ui.DBGMImages;

/**
 * Figure used to represent a table in the schema
 * 
 * @author Phil Zoio
 */
public class TableFigure extends Figure implements HandleBounds {

	private ColumnsFigure columnsFigure = new ColumnsFigure();
	private EditableLabel nameLabel;

	public TableFigure(EditableLabel name) {
		this(name, null);
	}

	public TableFigure(EditableLabel name, List<?> colums) {
		nameLabel = name;
		ToolbarLayout layout = new ToolbarLayout();
		layout.setVertical(true);
		layout.setStretchMinorAxis(true);
		setLayoutManager(layout);
		// setBorder(new LineBorder(ColorConstants.black, 1));
		setBackgroundColor(FontFactory.LIGHT_YELLOW);
		setForegroundColor(FontFactory.BLACK);
		setFont(FontFactory.FONT_BOLD);
		setOpaque(true);

		name.setForegroundColor(FontFactory.BLACK);
		// name.setBackgroundColor(FontFactory.DIAGRAM_TABLE_COLOR);
		name.setIcon(DBGMImages.ICON_TABLE);
		// name.setOpaque(true);

		add(name);
		add(columnsFigure);

	}

	/**
	 * @see org.eclipse.draw2d.Figure#setSize(int, int)
	 */
	@Override
	public void setSize(int w, int h) {
		super.setSize(w - 5, h - 5);
		updateTextBounds();
	}

	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
		updateTextBounds();
	}

	private void updateTextBounds() {
		Rectangle r = getNameLabel().getBounds().getCopy();
		Rectangle bounds = getBounds();
		r.x = bounds.x + 1;
		r.y = bounds.y + 1;
		r.width = bounds.width - 8;
		// if(r.width!=bounds.width-8) {
		// r = r.crop(new Insets(1,1,0,7));
		getNameLabel().setBounds(r);
		// }
	}

	/**
	 * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
	 */
	@Override
	protected void paintFigure(Graphics graphics) {

		// super.paintFigure(graphics);
		Rectangle bounds = getBounds().getCopy().resize(-6, -6); // .translate(2,2);
		Rectangle shadBounds = bounds.getCopy().translate(6, 6); // .resize(-1,-1);

		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH5_COLOR);
		graphics.fillRoundRectangle(shadBounds, 3, 3);
		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH4_COLOR);
		graphics.fillRoundRectangle(shadBounds.resize(-2, -2).translate(1, 1), 3, 3);
		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH3_COLOR);
		graphics.fillRoundRectangle(shadBounds.resize(-2, -2).translate(1, 1), 3, 3);
		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH2_COLOR);
		graphics.fillRoundRectangle(shadBounds.resize(-2, -2).translate(1, 1), 3, 3);
		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH1_COLOR);
		graphics.fillRoundRectangle(shadBounds.resize(-2, -2).translate(1, 1), 3, 3);
		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH0_COLOR);
		graphics.fillRoundRectangle(shadBounds.resize(-2, -2).translate(1, 1), 3, 3);

		// graphics.fillRoundRectangle(bounds.translate(4,4),5,5);

		graphics.setBackgroundColor(getLocalBackgroundColor());
		// bounds = getBounds().getCopy().resize(-6,-6);
		graphics.setForegroundColor(FontFactory.BLACK);
		graphics.fillRectangle(bounds);
		graphics.drawRectangle(bounds.resize(-1, -1));

		graphics.restoreState();
		// SHADOW_GRAPH1_COLOR.dispose();
		// SHADOW_GRAPH2_COLOR.dispose();
		// SHADOW_GRAPH3_COLOR.dispose();
		// SHADOW_GRAPH4_COLOR.dispose();

	}

	public void setSelected(boolean isSelected) {
		LineBorder lineBorder = (LineBorder) getBorder();
		if (isSelected) {
			lineBorder.setWidth(2);
		} else {
			lineBorder.setWidth(1);
		}
	}

	/**
	 * @return returns the label used to edit the name
	 */
	public EditableLabel getNameLabel() {
		return nameLabel;
	}

	/**
	 * @return the figure containing the column lables
	 */
	public ColumnsFigure getColumnsFigure() {
		return columnsFigure;
	}

	/**
	 * @see org.eclipse.gef.handles.HandleBounds#getHandleBounds()
	 */
	@Override
	public Rectangle getHandleBounds() {
		return getBounds().getCopy().resize(-6, -6);
	}

	@Override
	public void setBackgroundColor(Color bg) {
		super.setBackgroundColor(bg);
		columnsFigure.setBackgroundColor(bg);
	}
}
