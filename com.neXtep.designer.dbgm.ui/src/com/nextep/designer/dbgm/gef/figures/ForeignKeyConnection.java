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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.designer.dbgm.gef.editors.ConnectionEditPart;

/**
 * @author Christophe Fondacci
 *
 */
public class ForeignKeyConnection extends PolylineConnection {

	private Label text;
	private IKeyConstraint key;
	private ConnectionEditPart connection;
	public ForeignKeyConnection(ConnectionEditPart connection, IKeyConstraint key) {
		this.key=key;
		this.connection = connection;
		text = new Label(key.getName());
		text.setVisible(false);
		this.add(text);
	}
	public IKeyConstraint getConstraint() {
		return key;
	}
	public ConnectionEditPart getConnection() {
		return connection;
	}
	public void updateTextBounds() {

		if(key != null) {
			text.setText(key.getName());
		}
		Point start = this.getStart();
		Point end = this.getEnd();
		Dimension d = text.getPreferredSize();
		int dx=0,dy=0;
		if(start.equals(end)) {
			PointList pl = new PointList(4);
			pl.addPoint(new Point(start.x,start.y));
			pl.addPoint(new Point(start.x+30,start.y+0));
			pl.addPoint(new Point(start.x+30,start.y-30));
			pl.addPoint(new Point(start.x,start.y-30));
			setPoints(pl);
			dx = 40;
			dy =-d.height;
		} else {

			if(Math.abs(end.x-start.x) > Math.abs(end.y-start.y)) {
				// Handling S--E
				if(end.x>start.x) {
					dx = 10;
					dy = -d.height;

				} else {
					//Handling E--S
					dx = -d.width - 10;
					dy = -d.height;
				}
			} else {
				// Handling S
				//          |
				//          E
				if(end.y> start.y) {
					dx = - d.width / 2;
					dy = 10;

				} else {
				// Handling E
				//			|
				//			S
					dx = -d.width / 2;
					dy = -d.height - 10;
				}
			}
		}
		text.setBounds(new Rectangle(start.x +dx, start.y + dy,d.width,d.height));
	}
	public void setLabelVisible(boolean visible) {
		text.setVisible(visible);
	}
	@Override
	protected void outlineShape(Graphics g) {
//		PointList pl = getPoints();
//		if(pl.size()>2) {
//			g.setLineStyle(SWT.LINE_SOLID);
//			PointList startLine = pl.getCopy();
//			startLine.removePoint(startLine.size()-1);
//			g.drawPolyline(startLine);
//			g.setLineStyle(SWT.LINE_DASH);
//			startLine = new PointList();
//			startLine.addPoint(pl.getPoint(pl.size()-2));
//			startLine.addPoint(pl.getLastPoint());
//			g.drawPolyline(startLine);
//			g.setLineStyle(SWT.LINE_SOLID);
//		} else {
			super.outlineShape(g);
//		}
	}

	@Override
	public void setLineWidth(int w) {
		super.setLineWidth(w);
		((KeyDecoration)getSourceDecoration()).setLineWidth(w);
	}
}
