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
package com.nextep.designer.dbgm.gef.figures;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Transform;
import com.nextep.datadesigner.gui.impl.FontFactory;

/**
 * @author Christophe Fondacci
 */
public class EllipseDecoration extends Ellipse implements RotatableDecoration {

	private static final int DIAMETER = 10;
	private Point center = null;
	private Point refPoint = null;
	private Transform transform = new Transform();

	public EllipseDecoration() {
		this.setSize(DIAMETER, DIAMETER);
		this.setBackgroundColor(FontFactory.BLACK);
		this.setForegroundColor(FontFactory.BLACK);
		this.setOpaque(true);
	}

	/**
	 * @see org.eclipse.draw2d.Figure#setLocation(org.eclipse.draw2d.geometry.Point)
	 */
	@Override
	public void setLocation(Point p) {
		super.setLocation(p);
		center = new Point(p.x, p.y - DIAMETER / 2);
	}

	/**
	 * @see org.eclipse.draw2d.RotatableDecoration#setReferencePoint(org.eclipse.draw2d.geometry.Point)
	 */
	@Override
	public void setReferencePoint(Point p) {
		refPoint = p.getCopy();
		Point c = getLocation();
		int dx = 0, dy = 0;
		if (Math.abs(refPoint.x - c.x) > Math.abs(refPoint.y - c.y)) {
			if (refPoint.x > c.x) {
				dx = 0;
				dy = -DIAMETER / 2;
			} else if (c.x > refPoint.x) {
				dx = -DIAMETER;
				dy = -DIAMETER / 2;
			}
		} else {
			if (refPoint.y > c.y) {
				dx = -DIAMETER / 2;
				dy = 0;
			} else if (c.y > refPoint.y) {
				dx = -DIAMETER / 2;
				dy = -DIAMETER;
			}
		}
		setLocation(c.translate(dx, dy));

		// getLo
		// this.setLocation(new Point(p.x-1,p.y));
	}

	// /**
	// * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
	// */
	// @Override
	// public void paint(Graphics graphics) {
	// Point p = this.getLocation();
	// graphics.setBackgroundColor(ColorConstants.black);
	// graphics.setForegroundColor(ColorConstants.black);
	// graphics.fillOval(p.x, p.y, 300, 300);
	// }

}
