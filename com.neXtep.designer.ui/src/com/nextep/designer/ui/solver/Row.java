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
package com.nextep.designer.ui.solver;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.nebula.widgets.compositetable.ResizableGridRowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.gui.impl.FontFactory;

public class Row extends Composite {

	private Label icon;
	private Label title;
	private Label text;
	private Label suggestion;
	private Composite firstCol;

	public Row(Composite parent, int style) {
		super(parent, style);
		setLayout(new ResizableGridRowLayout()); // GridRowLayout(new int[] { 160, 100 }, false));
		firstCol = new Composite(this, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		firstCol.setLayoutData(data);
		GridLayout layout = new GridLayout(2, false);
		firstCol.setLayout(layout);

		icon = new Label(firstCol, SWT.NONE);
		icon.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 2));

		title = new Label(firstCol, SWT.BOTTOM);
		title.setFont(FontFactory.FONT_BOLD);
		title.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));

		text = new Label(firstCol, SWT.UP);
		text.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, false));

		suggestion = new Label(this, SWT.NONE);
		suggestion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Everything is white (need to enforce this for all pltaforms)
		firstCol.setBackground(FontFactory.WHITE);
		setBackground(FontFactory.WHITE);
		icon.setBackground(FontFactory.WHITE);
		title.setBackground(FontFactory.WHITE);
		text.setBackground(FontFactory.WHITE);
		suggestion.setBackground(FontFactory.WHITE);

	}

	public void setTitle(String title) {
		this.title.setText(title);
	}

	public void setText(String text) {
		this.text.setText(text);
	}

	public void setSuggestion(String suggestion) {
		this.suggestion.setText(suggestion);
	}

	public void setIcon(Image icon) {
		if (icon != null) {
			this.icon.setImage(icon);
		}
	}

	public void setSelected(boolean selected) {
		firstCol.setBackground(selected ? FontFactory.CHECKIN_COLOR : FontFactory.WHITE);
		setBackground(selected ? FontFactory.CHECKIN_COLOR : FontFactory.WHITE);
	}
}
