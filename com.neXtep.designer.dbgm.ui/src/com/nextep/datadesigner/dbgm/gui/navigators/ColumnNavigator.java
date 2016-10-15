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
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.helper.DatatypeHelper;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.navigators.UnversionedNavigator;

/**
 * A column navigator is a navigator connector
 * that allows a column to be displayed in the version
 * view tree hierarchy.
 *
 * @author Christophe Fondacci
 *
 */
public class ColumnNavigator extends UnversionedNavigator implements INavigatorConnector {

	/**
	 * @param column
	 */
	public ColumnNavigator(IBasicColumn column,ITypedObjectUIController controller) {
		super(VersionHelper.getVersionable(column.getParent()),column,controller);
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return DatatypeHelper.getDatatypeIcon(((IBasicColumn)getModel()).getDatatype());
	}


	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance("COLUMN");
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		super.refreshConnector();
		PlatformUI.getWorkbench().getDisplay().syncExec( new Runnable() {
			@Override
			public void run() {
				if(getSWTConnector()!=null && !getSWTConnector().isDisposed()) {
					getSWTConnector().setImage(getConnectorIcon());
				}
			}
		});

	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object o) {
		refreshConnector();
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#defaultAction()
	 */
	@Override
	public void defaultAction() {
		if(getParent()!=null && getParent().getParent()!=null) {
			getParent().getParent().defaultAction();
		}

	}
	public int compareTo(INavigatorConnector c) {
		IBasicColumn column = (IBasicColumn)getModel();
		if(c instanceof ColumnNavigator) {
			IBasicColumn col = (IBasicColumn)c.getModel();
			return column.compareTo(col);
		} else {
			return super.compareTo(c);
		}
	}

}
