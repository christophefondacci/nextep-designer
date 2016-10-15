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
package com.nextep.datadesigner.gui.impl.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import com.nextep.datadesigner.gui.model.CancellableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;

/**
 * @author Christophe Fondacci
 *
 */
public class StyledTextEditor extends CancellableEditor {


	private ChangeEvent 	triggeredEvent=null;
	private IEventListener 	target = null;
	private StyledText 		text=null;
	private String 			oldContent = null;

	public StyledTextEditor(StyledText source, ChangeEvent triggeredEvent, IEventListener target) {
		this.triggeredEvent=triggeredEvent;
		this.target=target;
		this.text=source;
		text.addListener(SWT.FocusOut, this);
		text.addListener(SWT.FocusIn, this);
		text.addListener(SWT.Traverse, this);
	}
	public static StyledTextEditor handle(StyledText source, ChangeEvent triggeredEvent, IEventListener target) {
		// Initializing new editor
		StyledTextEditor e = new StyledTextEditor(source,triggeredEvent,target);
		return e;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.CancellableEditor#captureContent()
	 */
	@Override
	public void captureContent() {
		oldContent = text.getText();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.CancellableEditor#publish()
	 */
	@Override
	public void publish() {
		target.handleEvent(triggeredEvent,null,text.getText());
		// Initializing old content if focus stay on the same control
		oldContent=text.getText();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.CancellableEditor#restore()
	 */
	@Override
	public void restore() {
		// We don't implicate our target listener since
		// modification has been rolled back
		text.setText(oldContent);
	}


}
