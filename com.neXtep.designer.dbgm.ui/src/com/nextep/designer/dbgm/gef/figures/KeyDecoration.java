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

import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

/**
 * A decoration for CASE-based foreign key relationships
 * @author Christophe Fondacci
 *
 */
public class KeyDecoration extends PolylineDecoration {

	public static final PointList FK_TIP = new PointList();
	private ForeignKeyConnection fkConn;
	static {
		FK_TIP.addPoint(0, 2);
		FK_TIP.addPoint(-2, 0);
		FK_TIP.addPoint(0, -2);
	}

	public KeyDecoration(ForeignKeyConnection fkConn) {
		super();
		setTemplate(FK_TIP);
		this.fkConn = fkConn;
	}
	/**
	 * @see org.eclipse.draw2d.PolylineDecoration#setLocation(org.eclipse.draw2d.geometry.Point)
	 */
	@Override
	public void setLocation(Point p) {
		super.setLocation(p);
		fkConn.updateTextBounds();
	}
	/**
	 * @see org.eclipse.draw2d.PolylineDecoration#setReferencePoint(org.eclipse.draw2d.geometry.Point)
	 */
	@Override
	public void setReferencePoint(Point ref) {
		super.setReferencePoint(ref);
		fkConn.updateTextBounds();
	}

}
