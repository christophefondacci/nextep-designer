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
package com.nextep.designer.dbgm.gef.figures;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.gui.impl.FontFactory;

/**
 * Figure used to hold the column labels. Implements the shadow effects
 * 
 * @author Christophe
 */
public class ColumnsFigure extends Figure {

	public ColumnsFigure() {
		FlowLayout layout = new FlowLayout();
		layout.setMinorAlignment(FlowLayout.ALIGN_LEFTTOP);
		layout.setStretchMinorAxis(false);
		layout.setHorizontal(false);
		setLayoutManager(layout);
		setBorder(new ColumnFigureBorder());
		setBackgroundColor(FontFactory.LIGHT_YELLOW);
		setForegroundColor(FontFactory.BLACK);
		setFont(Display.getCurrent().getSystemFont());
		setOpaque(true);
	}

	/**
	 * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
	 */
	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle bounds = getBounds().getCopy().resize(-6, 0); // .translate(2,2);
		Rectangle shadBounds = bounds.getCopy().translate(6, 0); // .resize(-1,-1);

		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH5_COLOR);
		graphics.fillRectangle(shadBounds);
		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH4_COLOR);
		graphics.fillRectangle(shadBounds.resize(-2, 0).translate(1, 0));
		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH3_COLOR);
		graphics.fillRectangle(shadBounds.resize(-2, 0).translate(1, 0));
		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH2_COLOR);
		graphics.fillRectangle(shadBounds.resize(-2, 0).translate(1, 0));
		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH1_COLOR);
		graphics.fillRectangle(shadBounds.resize(-2, 0).translate(1, 0));
		graphics.setBackgroundColor(FontFactory.SHADOW_GRAPH0_COLOR);
		graphics.fillRectangle(shadBounds.resize(-2, 0).translate(1, 0));

		graphics.setBackgroundColor(getLocalBackgroundColor());
		graphics.fillRectangle(bounds);
		graphics.setForegroundColor(getLocalForegroundColor());
		graphics.drawRectangle(bounds.resize(-1, 0));
		graphics.restoreState();
		// super.paintFigure(graphics);

	}

	class ColumnFigureBorder extends AbstractBorder {

		public Insets getInsets(IFigure figure) {
			return new Insets(5, 3, 3, 1);
		}

		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), tempRect
					.getTopRight().translate(-7, 0));
		}
	}
}
