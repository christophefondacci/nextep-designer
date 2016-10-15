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
package com.nextep.designer.vcs.ui.dialogs;

import java.util.List;
import org.eclipse.swt.graphics.Image;
import com.nextep.designer.ui.model.IValidatableUI;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * The commit confirmation dialog. This implementation defines the appropriate text, descriptions
 * and icons and tags all versionable with the activity on validation when the activity is prompted.
 * 
 * @author Christophe Fondacci
 */
public class ConfirmCommitDialog extends ConfirmVersionablesDialog implements IValidatableUI {

	public ConfirmCommitDialog(IVersioningOperationContext context,
			List<IVersionable<?>> commitsToConfirm, boolean displayActivity) {
		super(context, commitsToConfirm, displayActivity);
	}

	@Override
	public String getAreaTitle() {
		return VCSUIMessages.getString("dialog.confirmCommit.title"); //$NON-NLS-1$
	}

	@Override
	public String getDescription() {
		return VCSUIMessages.getString("dialog.confirmCommit.message"); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return VCSImages.ICON_VALIDATION;
	}

	@Override
	public boolean validate() {
		// If activity is displayed, we need to tag everything with this activity
		if (isActivityDisplayed()) {
			IVersioningOperationContext context = getContext();
			if (context.getActivity() == null
					|| !context.getActivity().getName().equals(getActivityUserText())) {
				// We set a new activity when selected text differs from context activity
				context.setActivity(getVersioningService().createActivity(getActivityUserText()));
			} else {
				getVersioningService().setCurrentActivity(context.getActivity());
			}
			final IActivity activity = context.getActivity();
			for (IVersionable<?> v : getVersionablesToConfirm()) {
				v.getVersion().setActivity(activity);
			}
		}
		return true;
	}

	@Override
	public void cancel() {
	}
}
