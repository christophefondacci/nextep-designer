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
package com.nextep.designer.beng.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.services.IDeliveryService;
import com.nextep.designer.beng.ui.BengUIMessages;
import com.nextep.designer.beng.ui.model.DeliveryTypeItem;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;

/**
 * This handler creates a new delivery script on the currently selected delivery folder
 * (DeliveryType)
 * 
 * @author Christophe Fondacci
 */
public class NewDeliveryItemHandler extends AbstractHandler {

	// private static final Log log = LogFactory.getLog(NewDeliveryItemHandler.class);
	/**
	 * @see com.nextep.designer.vcs.ui.handlers.NewTypedInstanceHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		DeliveryTypeItem typeItem = null;
		// Retrieving delivery type
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection) sel).getFirstElement();
			if (o instanceof DeliveryTypeItem) {
				typeItem = (DeliveryTypeItem) o;
			} else {
				// Exiting with error message
				throw new ErrorException(BengUIMessages.getString("invalidDeliveryFolder")); //$NON-NLS-1$
				// return null;
			}
		}

		// Creating a new custom SQL script
		ISQLScript s = new SQLScript(ScriptType.CUSTOM);
		// Editing script*
		ITypedObjectUIController controller = UIControllerFactory.getController(IElementType
				.getInstance(ISQLScript.TYPE_ID));
		IDisplayConnector c = controller.initializeEditor(s);
		AbstractUIController.newWizardEdition("New delivery script wizard...", c);
		// Saving script
		ControllerFactory.getController(s).save(s);
		// Adding to delivery module
		IDeliveryModule m = typeItem.getModule();
		DBVendor scriptVendor = m.getDBVendor();
		// For JDBC deliveries, we instantiate untagged scripts which make them available for all
		// vendor deployment
		if (scriptVendor == DBVendor.JDBC) {
			scriptVendor = null;
		}
		final IDeliveryService deliveryService = BengPlugin.getService(IDeliveryService.class);
		IDeliveryItem<?> item = deliveryService.createDeliveryScript(typeItem.getType(), s);
		item.setDBVendor(scriptVendor);
		m.addDeliveryItem(item);

		return null;

	}
}
