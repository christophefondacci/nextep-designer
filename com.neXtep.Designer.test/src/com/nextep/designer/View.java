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
package com.nextep.designer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class View extends ViewPart {

	public static final String ID = "com.neXtep.Designer.view";
	
	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		top.setLayout(layout);
		// top banner
		Composite banner = new Composite(top, SWT.NONE);
		banner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false));
		layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.numColumns = 2;
		banner.setLayout(layout);
		
		// setup bold font
		Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);    
		
		Label l = new Label(banner, SWT.WRAP);
		l.setText("Subject:");
		l.setFont(boldFont);
		l = new Label(banner, SWT.WRAP);
		l.setText("This is a message about the cool Eclipse RCP!");
		
		l = new Label(banner, SWT.WRAP);
		l.setText("From:");
		l.setFont(boldFont);
    
		final Link link = new Link(banner, SWT.NONE);
		link.setText("<a>nicole@mail.org</a>");
		link.addSelectionListener(new SelectionAdapter() {    
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(getSite().getShell(), "Not Implemented", "Imagine the address book or a new message being created now.");
			}    
		});
    
		l = new Label(banner, SWT.WRAP);
		l.setText("Date:");
		l.setFont(boldFont);
		l = new Label(banner, SWT.WRAP);
		l.setText("10:34 am");
		// message contents
		Text text = new Text(top, SWT.MULTI | SWT.WRAP);
		text.setText("This RCP Application was generated from the PDE Plug-in Project wizard. This sample shows how to:\n"+
						"- add a top-level menu and toolbar with actions\n"+
						"- add keybindings to actions\n" +
						"- create views that can't be closed and\n"+
						"  multiple instances of the same view\n"+
						"- perspectives with placeholders for new views\n"+
						"- use the default about dialog\n"+
						"- create a product definition\n");
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	public void setFocus() {
	}
}
