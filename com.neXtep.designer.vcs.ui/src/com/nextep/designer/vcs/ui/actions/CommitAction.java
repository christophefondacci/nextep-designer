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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.vcs.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IVersioningUIService;

/**
 * @author Christophe Fondacci
 */
public class CommitAction extends Action implements IModelOriented<IVersionable<?>>, IEventListener {

	private IVersionable<?> model;

	public CommitAction(Object instigator, IVersionable<?> model) {
		super(VCSUIMessages.getString("action.commit.tooltip"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setImageDescriptor(ImageDescriptor.createFromImage(VCSImages.ICON_COMMIT));
		setModel(model);
		Designer.getListenerService().registerListener(instigator, model, this);
	}

	@Override
	public void run() {
		final IVersioningUIService versioningService = VCSUIPlugin.getVersioningUIService();
		versioningService.commit(null, getModel());
	}

	@Override
	public void setModel(IVersionable<?> model) {
		this.model = model;
		handleEnablement();
	}

	private void handleEnablement() {
		if (model != null) {
			setEnabled(model.getVersion().getStatus() != IVersionStatus.CHECKED_IN);
		} else {
			setEnabled(false);
		}
	}

	@Override
	public IVersionable<?> getModel() {
		return model;
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case CHECKIN:
			handleEnablement();
		}
	}
}
