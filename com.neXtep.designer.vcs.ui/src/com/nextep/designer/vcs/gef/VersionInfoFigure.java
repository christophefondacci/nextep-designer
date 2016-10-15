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
package com.nextep.designer.vcs.gef;

import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;

/**
 * @author Christophe Fondacci
 *
 */
public class VersionInfoFigure extends Ellipse implements MouseListener  {
	private IVersionInfo version;
	private boolean selected = false;
	private ISelectionProvider provider;
	private Label versionLabel = null;
	private ToolbarLayout layout;
	public VersionInfoFigure(IVersionInfo version, ISelectionProvider provider, boolean isCurrent) {
		this.version = version;
		this.selected=isCurrent;
		this.provider=provider;
		Label filler = new Label("");
		versionLabel = new Label(version.getLabel());
		versionLabel.setFont(FontFactory.FONT_TINY);
		versionLabel.setLabelAlignment(PositionConstants.CENTER);

		layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);

		layout.setVertical(true);

		setLayoutManager(layout);
		if(version.getStatus()==IVersionStatus.CHECKED_IN) {
			setBackgroundColor(FontFactory.VERSIONTREE_CHECKIN_COLOR); 
		} else if(version.getStatus()==IVersionStatus.CHECKED_OUT) {
			setBackgroundColor(FontFactory.VERSIONTREE_CHECKOUT_COLOR); 
		} else {
			setBackgroundColor(ColorConstants.white); 
		}
		setForegroundColor(ColorConstants.black);
		setOpaque(true);
		add(filler);
		add(versionLabel);

	}

	public void paintFigure(Graphics graphics)
	{
		Rectangle bounds = getBounds().getCopy().resize(-2, -2).translate(1, 1);

		graphics.setBackgroundColor(ColorConstants.darkGray);
		graphics.fillOval(bounds.getTranslated(1,1));
		graphics.setBackgroundColor(getLocalBackgroundColor());
		graphics.fillOval(bounds);
		if(selected) {
			graphics.setForegroundColor(ColorConstants.red);
			Rectangle redBox = getBounds().getCopy().resize(-1,-1);
			graphics.drawRectangle(redBox);
		}
		//Handling selection
		ISelection sel = provider.getSelection();
		if(sel instanceof IStructuredSelection) {
			IStructuredSelection s = (IStructuredSelection)sel;
			if(!s.isEmpty() && s.getFirstElement() == version) {
				graphics.setForegroundColor(ColorConstants.blue);
				graphics.setLineWidth(2);
				graphics.drawRectangle(getBounds());
			}
		}
		graphics.restoreState();
	}

	/**
	 * @see org.eclipse.draw2d.MouseListener#mouseDoubleClicked(org.eclipse.draw2d.MouseEvent)
	 */
	@Override
	public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent me) {
	}

	/**
	 * @see org.eclipse.draw2d.MouseListener#mousePressed(org.eclipse.draw2d.MouseEvent)
	 */
	@Override
	public void mousePressed(org.eclipse.draw2d.MouseEvent me) {
		LogFactory.getLog(VersionInfoFigure.class).debug("Mouse up");
		provider.setSelection(new StructuredSelection(version));
		repaint();
	}

	/**
	 * @see org.eclipse.draw2d.MouseListener#mouseReleased(org.eclipse.draw2d.MouseEvent)
	 */
	@Override
	public void mouseReleased(org.eclipse.draw2d.MouseEvent me) {


	}

	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
		
	}

	@Override
	public void paint(Graphics graphics) {
		if(versionLabel.isTextTruncated()) {
			versionLabel.setFont(FontFactory.FONT_TINIEST);
			versionLabel.invalidate();
			layout.invalidate();
		}
		super.paint(graphics);
	}

}

