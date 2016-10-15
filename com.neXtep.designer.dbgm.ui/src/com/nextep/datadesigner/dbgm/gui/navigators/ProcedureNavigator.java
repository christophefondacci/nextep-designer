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

import java.util.ArrayList;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IProcedureParameter;
import com.nextep.datadesigner.dbgm.model.IVariable;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;

/**
 * @author Christophe Fondacci
 *
 */
public class ProcedureNavigator extends UntypedNavigator {

	public ProcedureNavigator(IProcedure proc) {
		super(proc,null);
	}

	@Override
	public void initializeChildConnectors() {
		for(IVariable v : ((IProcedure)getModel()).getVariables()) {
			addConnector(new VariableNavigator(v));
		}
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(IProcedure.TYPE_ID);
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getTitle()
	 */
	@Override
	public String getTitle() {
		IProcedure p = (IProcedure)getModel();
		StringBuffer buf = new StringBuffer();
		buf.append(p.getName());
		boolean isFirst = true;
		for(IProcedureParameter param : p.getParameters()) {
			if(!isFirst) {
				buf.append(',');
			} else {
				buf.append('(');
				isFirst = false;
			}
			if(param!=null && param.getDatatype()!=null && param.getDatatype().getName()!=null) {
				buf.append(param.getDatatype().getName().toLowerCase());
			}
		}
		if(!isFirst) {
			buf.append(')');
		}
		if(p.getReturnType() != null) {
			buf.append(" : ");
			if(p.getReturnType()!=null && p.getReturnType().getName()!=null) {
				buf.append(p.getReturnType().getName().toLowerCase());
			}
		}
		return buf.toString();
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		// Removing connectors
		for(INavigatorConnector c : new ArrayList<INavigatorConnector>(getConnectors())) {
			removeConnector(c);
		}
		initializeChildConnectors();
		super.refreshConnector();
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#autoExpand()
	 */
	@Override
	protected boolean autoExpand() {
		return false;
	}
	
//	@Override
//	public void defaultAction() {
////		final IProcedure p = (IProcedure)getModel();
//		// Supported, we open standard editor
//		super.defaultAction();
//	}
}
