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
 * An event-based editor handling check box. This editor will fire change events when the attached
 * check box is modified.
 * 
 * @author Christophe Fondacci
 */
public class CheckBoxEditor extends CancellableEditor {

	private Button checkbox;
	private ChangeEvent firedEvent;
	private IEventListener target;
	private boolean oldCheckedStatus;

	private CheckBoxEditor(Button checkbox, ChangeEvent firedEvent, IEventListener target) {
		this.checkbox = checkbox;
		this.firedEvent = firedEvent;
		this.target = target;
		checkbox.addListener(SWT.FocusIn, this);
		checkbox.addListener(SWT.Selection, this);
	}

	public static CheckBoxEditor handle(Button checkbox, ChangeEvent firedEvent,
			IEventListener target) {
		return new CheckBoxEditor(checkbox, firedEvent, target);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.CancellableEditor#captureContent()
	 */
	@Override
	public void captureContent() {
		oldCheckedStatus = checkbox.getSelection();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.CancellableEditor#publish()
	 */
	@Override
	public void publish() {
		// if(checkbox.getSelection()!=oldCheckedStatus) {
		target.handleEvent(firedEvent, null, checkbox.getSelection());
		// Initializing old content if focus stay on the same control
		oldCheckedStatus = checkbox.getSelection();
		// }

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.CancellableEditor#restore()
	 */
	@Override
	public void restore() {
		checkbox.setSelection(oldCheckedStatus);
	}

}
