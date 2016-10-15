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

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.PointList;

public class ShortestManhattanRouter implements ConnectionRouter {
	private ShortestPathConnectionRouter shortRouter;
	private ManhattanConnectionRouter manRouter;
	
	public ShortestManhattanRouter(IFigure f) {
		shortRouter = new ShortestPathConnectionRouter(f);
		manRouter = new ManhattanConnectionRouter();
	}
	
	@Override
	public Object getConstraint(Connection connection) {
		return shortRouter.getConstraint(connection);
	}

	@Override
	public void invalidate(Connection connection) {
		shortRouter.invalidate(connection);
	}

	@Override
	public void remove(Connection connection) {
		shortRouter.remove(connection);
	}

	@Override
	public void route(Connection connection) {
		shortRouter.route(connection);
		
		final PointList points = connection.getPoints();
		if(points.size()>2) {
			PointList newPoints = new PointList();
			for(int i = 0 ; i < points.size()-1 ; i ++) {
				newPoints.addAll(new CustomManhattanConnectionRouter().routeFromTo(points.getPoint(i), points.getPoint(i+1), connection));
			}
			connection.setPoints(newPoints);
		} else {
			connection.setPoints(new CustomManhattanConnectionRouter().routeFromTo(points.getFirstPoint(), points.getLastPoint(), connection));
		}

	}

	@Override
	public void setConstraint(Connection connection, Object constraint) {
		shortRouter.setConstraint(connection, constraint);
	}

}
