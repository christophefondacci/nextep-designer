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
package com.nextep.datadesigner.dbgm.gui.navigators;

import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.dbgm.model.IVariable;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.ui.DBGMImages;

/**
 * @author Christophe Fondacci
 *
 */
public class VariableNavigator extends UntypedNavigator {

	private IVariable var;
	public VariableNavigator(IVariable var) {
		super(null,null);
		this.var = var;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return DBGMImages.ICON_PUBLIC_FIELD;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getTitle()
	 */
	@Override
	public String getTitle() {
		return var.getName() + (var.getDatatypeName() != null ? " : " + var.getDatatypeName() : "");
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getType()
	 */
	@Override
	public IElementType getType() {
		return null;
	}
	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#getModel()
	 */
	@Override
	public Object getModel() {
		return var;
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();
	}

}
