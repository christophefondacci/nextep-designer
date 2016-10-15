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
package com.nextep.datadesigner.gui.impl.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import com.nextep.datadesigner.gui.model.CancellableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;

/**
 * @author Christophe Fondacci
 *
 */
public class ComboEditor extends CancellableEditor {

	private ChangeEvent 	triggeredEvent=null;
	private IEventListener 	target = null;
	private Combo 			combo=null;
	private int 			oldSelection;
	private String 			oldText;

	public ComboEditor(Combo source, ChangeEvent triggeredEvent, IEventListener target) {
		this.triggeredEvent=triggeredEvent;
		this.target=target;
		this.combo=source;
		combo.addListener(SWT.FocusOut, this);
		combo.addListener(SWT.FocusIn, this);
		combo.addListener(SWT.Traverse, this);
		combo.addListener(SWT.Selection,this);
	}
	public static ComboEditor handle(Combo source, ChangeEvent triggeredEvent, IEventListener target) {
		return new ComboEditor(source,triggeredEvent,target);
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.CancellableEditor#captureContent()
	 */
	@Override
	public void captureContent() {
		oldSelection = combo.getSelectionIndex();
		oldText = combo.getText();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.CancellableEditor#publish()
	 */
	@Override
	public void publish() {
		if(!combo.getText().equals(oldText)) {
			target.handleEvent(triggeredEvent, null, combo.getText());
			captureContent();
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.CancellableEditor#restore()
	 */
	@Override
	public void restore() {
		if(oldSelection!=-1) {
			combo.select(oldSelection);
		} else {
			combo.setText(oldText);
		}
	}

}
