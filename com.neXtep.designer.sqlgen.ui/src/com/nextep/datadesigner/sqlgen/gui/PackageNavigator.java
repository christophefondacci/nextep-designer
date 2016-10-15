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
package com.nextep.datadesigner.sqlgen.gui;

import java.util.ArrayList;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.dbgm.gui.navigators.VariableNavigator;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IVariable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.designer.sqlgen.ui.PackageEditorInput;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 *
 */
public class PackageNavigator extends UntypedNavigator {

	public PackageNavigator(IPackage pkg, ITypedObjectUIController controller) {
		super(pkg,controller);
	}
	@Override
	public void initializeChildConnectors() {
		IPackage pkg = (IPackage)getModel();
//		pkg.parse();
		for(IVariable v : pkg.getVariables()) {
			addConnector(new VariableNavigator(v));
		}
		for(IProcedure p : pkg.getProcedures()) {
			addConnector(new LightProcedureNavigator(p,pkg));
		}	
	}
//	public void dedicatedChildInit() {
//		IPackage pkg = (IPackage)getModel();
//		for(IVariable v : pkg.getVariables()) {
//			addConnector(new VariableNavigator(v));
//		}
//		for(IProcedure p : pkg.getProcedures()) {
//			addConnector(new ProcedureNavigator(p));
//		}	
//	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#autoExpand()
	 */
	@Override
	protected boolean autoExpand() {
		return false;
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();
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
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#defaultAction()
	 */
	@Override
	public void defaultAction() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new PackageEditorInput((IPackage)getModel()), "com.neXtep.designer.sqlgen.ui.packageEditor"); // "com.neXtep.designer.sqlgen.ui.SQLEditor");
		} catch( PartInitException e) {
			throw new ErrorException(e);
		}
	}
}
