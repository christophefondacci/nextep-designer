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
package com.nextep.datadesigner.gui.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * This class handles the color on focus feature of the data designer
 * IDE. To add this behaviour to a control you simply have to call the
 * static method <code>handle</code> on this SWT control.
 *
 * @author Christophe Fondacci
 *
 */
public class ColorFocusListener implements Listener {
	private Color active = new Color(Display.getCurrent(), 220, 252, 160);
	private Color inactive = new Color(Display.getCurrent(), 255, 255, 255) ;

	private static ColorFocusListener instance;
	private ColorFocusListener() {}
	/**
	 * Instance getter for custom access
	 * @return the instance of a <code>ColorFocusListener</code>
	 */
	public static ColorFocusListener getInstance() {
		if(instance == null) {
			instance=new ColorFocusListener();
		}
		return instance;
	}
	/**
	 * Static method that handles the specified control by the
	 * ColorFocusListener. This method should be the only
	 * method used to add the behaviour to the control.
	 *
	 * @param c control for which we want to add the behaviour.
	 */
	public static void handle(Control c) {
		c.addListener(SWT.FocusIn, getInstance());
		c.addListener(SWT.FocusOut, getInstance());
	}
	/**
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event arg0) {
        switch (arg0.type) {
    	case SWT.FocusOut:
	        ((Control)arg0.widget).setBackground(inactive);
	        break;
    	case SWT.FocusIn:
    		((Control)arg0.widget).setBackground(active);
    		if(arg0.widget instanceof Text) {
    			Text t = (Text)arg0.widget;
    			t.setSelection(0, t.getText().length());
    		}
    		break;
        }
	}

}
