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
package com.nextep.datadesigner.gui.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A cancellable editor takes the control of content
 * modification and manages the rollback on escape.
 * It publishes the modified content to the object
 * once return is pressed or focus is lost by
 * the SWT widget.
 *
 * @author Christophe Fondacci
 *
 */
public abstract class CancellableEditor implements Listener {
	private boolean isEditing = false;
	/**
	 * Publishes the edited content to the model
	 *
	 */
	public abstract void publish();
	/**
	 * Captures the current contents (used before modifications)
	 *
	 */
	public abstract void captureContent();
	/**
	 * Restores the previous contents
	 *
	 */
	public abstract void restore();


	/**
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(final Event e) {
        switch (e.type) {
        	case SWT.FocusIn:
        		isEditing = true;
        		this.captureContent();
        		break;
        	case SWT.Dispose:
        		if(!isEditing) {
        			return;
        		}
        		// FALL THROUGH
        	case SWT.Selection:	//For controls like combo box
        	case SWT.FocusOut:
		        this.publish();
		        isEditing = false;
		        break;
		        
	        case SWT.Traverse:
	        	switch (e.detail) {
	        		case SWT.TRAVERSE_RETURN:
	        			this.publish();
	        			break;
	        		case SWT.TRAVERSE_ESCAPE:
	        			//Nothing to dispose here
	        			this.restore();
	        			e.doit = false;
	        			break;
	        	}
	        	isEditing = false;
	        	break;
        }
	}

}
