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
package com.nextep.designer.dbgm.ui.actions;

import java.text.MessageFormat;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.ICheckConstraintContainer;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * @author Christophe Fondacci
 */
public class CheckConstraintsActionProvider extends AbstractFormActionProvider {

	@Override
	public Object add(ITypedObject parent) {
		final ITypedObjectUIController controller = UIControllerFactory.getController(IElementType
				.getInstance(ICheckConstraint.TYPE_ID));
		final ICheckConstraintContainer table = (ICheckConstraintContainer) parent;
		int constraintsCount = table.getCheckConstraints().size();
		return controller.emptyInstance("CHECK_" + constraintsCount, parent);
	}

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
		final ICheckConstraint checkConstraint = (ICheckConstraint) toRemove;
		// Asking user confirmation
		final boolean confirmed = MessageDialog
				.openQuestion(Display.getDefault().getActiveShell(), MessageFormat.format(
						DBGMUIMessages.getString("editor.table.confirmDelCheckTitle"), //$NON-NLS-1$
						checkConstraint.getName()), MessageFormat.format(
						DBGMUIMessages.getString("editor.table.confirmDelCheck"), //$NON-NLS-1$
						checkConstraint.getName(), checkConstraint.getConstrainedTable().getName()));
		if (confirmed) {
			// If confirmed, we remove it through our workspace service
			final IWorkspaceUIService workspaceService = VCSUIPlugin
					.getService(IWorkspaceUIService.class);
			workspaceService.remove((IReferenceable) toRemove);
		}
	}

}
