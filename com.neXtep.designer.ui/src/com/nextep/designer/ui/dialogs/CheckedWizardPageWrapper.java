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
package com.nextep.designer.ui.dialogs;

import org.eclipse.swt.widgets.Composite;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.model.ICheckedObject;

/**
 * This class adapts a IDisplayConnector into a wizard page by extending the default wrapper in
 * order to add a checked object listener.
 * 
 * @author Christophe Fondacci
 */
public class CheckedWizardPageWrapper extends WizardPageWrapper implements IEventListener {

	private IDisplayConnector connector;

	public CheckedWizardPageWrapper(IDisplayConnector conn) {
		super(conn);
		this.connector = conn;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		connector.refreshConnector();
		if (connector.getModel() instanceof ICheckedObject
				&& connector.getModel() instanceof IObservable) {
			Designer.getListenerService().registerListener(this.getControl(),
					(IObservable) connector.getModel(), this);
			checkModel();
		}
	}

	/**
	 * Checks the underlying model
	 */
	private void checkModel() {
		try {
			if (connector.getModel() != null) {
				((ICheckedObject) connector.getModel()).checkConsistency();
			}
			setErrorMessage(null);
			setPageComplete(true);
		} catch (InconsistentObjectException e) {
			setErrorMessage(e.getReason());
			setPageComplete(false);
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		checkModel();
	}

}
