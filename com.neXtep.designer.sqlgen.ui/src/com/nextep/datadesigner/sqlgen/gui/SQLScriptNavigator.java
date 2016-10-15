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
package com.nextep.datadesigner.sqlgen.gui;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.sqlgen.impl.VersionedSQLScript;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.sqlgen.ui.Activator;
import com.nextep.designer.sqlgen.ui.SQLScriptEditorInput;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 *
 */
public class SQLScriptNavigator extends UntypedNavigator {

	public SQLScriptNavigator(ISQLScript script, ITypedObjectUIController controller) {
		super(script,controller);
	}

	@Override
	public void initializeChildConnectors() {
		ISQLScript script = (ISQLScript)getModel();
		if(script instanceof SQLWrapperScript) {
			for(ISQLScript childScript : ((SQLWrapperScript)script).getChildren()) {
				if(childScript instanceof VersionedSQLScript) {
					addConnector(UIControllerFactory.getController(IElementType.getInstance("VERSIONABLE")).initializeNavigator(childScript));
				} else {
					addConnector(UIControllerFactory.getController(childScript.getType()).initializeNavigator(childScript));
				}
			}
		}		
	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
//	 */
//	@Override
//	public Image getConnectorIcon() {
//		return SQLGenImages.ICON_SQL;
//	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getTitle()
	 */
	public String getTitle() {
		return ((ISQLScript)getModel()).getFilename();
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#defaultAction()
	 */
	@Override
	public void defaultAction() {
		try {
			IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if(getModel() instanceof SQLWrapperScript) {
				page.openEditor(new SQLScriptEditorInput((ISQLScript)getModel()), "com.neXtep.designer.sqlgen.ui.multiSQLEditor");
			} else {
				page.openEditor(new SQLScriptEditorInput((ISQLScript)getModel()), "com.neXtep.designer.sqlgen.ui.SQLEditor");
			}
		} catch( PartInitException e) {
			throw new ErrorException(e);
		}
	}
}
