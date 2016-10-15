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
package com.nextep.designer.dbgm.gef.layout;

import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Ray;
import org.eclipse.draw2d.geometry.Rectangle;
import com.nextep.designer.vcs.model.IDiagram;

public class NonOverlappingManhattanRouter extends BendpointConnectionRouter {
	
	private static Ray 	UP		= new Ray(0, -1),
	DOWN	= new Ray(0, 1),
	LEFT	= new Ray(-1, 0),
	RIGHT	= new Ray(1, 0);
	private IDiagram diagram;
	public NonOverlappingManhattanRouter(IDiagram diagram) {
		this.diagram = diagram;
	}
	@Override
	public void route(Connection conn) {
		Point startPoint = getStartPoint(conn);
		conn.translateToRelative(startPoint);
		Point endPoint = getEndPoint(conn);
		conn.translateToRelative(endPoint);
//		Rectangle r;
//		r.i
	}
	
	/**
	 * Returns the direction the point <i>p</i> is in relation to the given rectangle.
	 * Possible values are LEFT (-1,0), RIGHT (1,0), UP (0,-1) and DOWN (0,1).
	 * 
	 * @param r the rectangle
	 * @param p the point
	 * @return the direction from <i>r</i> to <i>p</i>
	 */
	protected Ray getDirection(Rectangle r, Point p) {
		int i, distance = Math.abs(r.x - p.x);
		Ray direction;
		
		direction = LEFT;

		i = Math.abs(r.y - p.y);
		if (i <= distance) {
			distance = i;
			direction = UP;
		}

		i = Math.abs(r.bottom() - p.y);
		if (i <= distance) {
			distance = i;
			direction = DOWN;
		}

		i = Math.abs(r.right() - p.x);
		if (i < distance) {
			distance = i;
			direction = RIGHT;
		}

		return direction;
	}

	protected Ray getEndDirection(Connection conn) {
		ConnectionAnchor anchor = conn.getTargetAnchor();
		Point p = getEndPoint(conn);
		Rectangle rect;
		if (anchor.getOwner() == null)
			rect = new Rectangle(p.x - 1, p.y - 1, 2, 2);
		else {
			rect = conn.getTargetAnchor().getOwner().getBounds().getCopy();
			conn.getTargetAnchor().getOwner().translateToAbsolute(rect);
		}
		return getDirection(rect, p);
	}
	protected Ray getStartDirection(Connection conn) {
		ConnectionAnchor anchor = conn.getSourceAnchor();
		Point p = getStartPoint(conn);
		Rectangle rect;
		if (anchor.getOwner() == null)
			rect = new Rectangle(p.x - 1, p.y - 1, 2, 2);
		else {
			rect = conn.getSourceAnchor().getOwner().getBounds().getCopy();
			conn.getSourceAnchor().getOwner().translateToAbsolute(rect);
		}
		return getDirection(rect, p);
	}
}
