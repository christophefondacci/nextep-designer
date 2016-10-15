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
package com.nextep.designer.dbgm.gef.editors;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;

/**
 * @author Christophe Fondacci
 *
 */
public class ItemAnchor extends ChopboxAnchor {

	private Point ref;
	public ItemAnchor(IFigure owner,Point ref) {
		super(owner);
		this.ref = new Point(ref);
		if(ref.x>= owner.getBounds().x || ref.x <= owner.getBounds().x+owner.getBounds().width) {
			ref.x -= owner.getBounds().x;
			ref.y = getReferencePoint().y;
		}
		if(ref.y>= owner.getBounds().y || ref.y <= owner.getBounds().y+owner.getBounds().height) {
			ref.x = getReferencePoint().x;
			ref.y -= owner.getBounds().y;
		}
//		owner.translateToRelative(this.ref);
	}

	/**
	 * Gets a Rectangle from {@link #getBox()} and returns the Point where a line from the
	 * center of the Rectangle to the Point <i>reference</i> intersects the Rectangle.
	 *
	 * @param reference The reference point
	 * @return The anchor location
	 */
	public Point getLocation(Point reference) {
		return ref;
//		Rectangle r = Rectangle.SINGLETON;
//		r.setBounds(getBox());
//		r.translate(-1, -1);
//		r.resize(1, 1);
//
//		getOwner().translateToAbsolute(r);
//		float centerX = r.x + 0.5f * r.width;
//		float centerY = r.y + 0.5f * r.height;
//
//		if (r.isEmpty() || (ref.x == (int)centerX && ref.y == (int)centerY))
//			return new Point((int)centerX, (int)centerY);  //This avoids divide-by-zero
//
//		float dx = ref.x - centerX;
//		float dy = ref.y - centerY;
//
//		//r.width, r.height, dx, and dy are guaranteed to be non-zero.
//		float scale = 0.5f / Math.max(Math.abs(dx) / r.width, Math.abs(dy) / r.height);
//
//		dx *= scale;
//		dy *= scale;
//		centerX += dx;
//		centerY += dy;
//
//		return new Point(Math.round(centerX), Math.round(centerY));
	}
}
