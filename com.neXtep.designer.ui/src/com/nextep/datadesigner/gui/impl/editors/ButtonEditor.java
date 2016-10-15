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
import org.eclipse.swt.widgets.Button;
import com.nextep.datadesigner.gui.model.CancellableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;

/**
 * This class handle button selection and propagates a predefined event when button is pressed.
 * 
 * @author Christophe
 */
public class ButtonEditor extends CancellableEditor {

	private Button button;
	private ChangeEvent firedEvent;
	private IEventListener target;

	private ButtonEditor(Button button, ChangeEvent firedEvent, IEventListener target) {
		this.button = button;
		this.firedEvent = firedEvent;
		this.target = target;
		// button.addListener(SWT.FocusIn,this);
		button.addListener(SWT.Selection, this);
	}

	public static ButtonEditor handle(Button button, ChangeEvent firedEvent, IEventListener target) {
		return new ButtonEditor(button, firedEvent, target);
	}

	@Override
	public void captureContent() {
		// Nothing to capture
	}

	@Override
	public void publish() {
		target.handleEvent(firedEvent, null, button);
	}

	@Override
	public void restore() {
		// Nothing to restore
	}

}
