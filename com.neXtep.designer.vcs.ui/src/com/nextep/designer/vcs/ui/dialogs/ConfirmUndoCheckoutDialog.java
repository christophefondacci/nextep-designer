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
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.VCSUIMessages;

public class ConfirmUndoCheckoutDialog extends ConfirmVersionablesDialog {

	public ConfirmUndoCheckoutDialog(List<IVersionable<?>> commitsToConfirm) {
		super(null, commitsToConfirm, false);
	}

	@Override
	public String getAreaTitle() {
		return "Undo checkout confirmation";
	}

	@Override
	public String getDescription() {
		return VCSUIMessages.getString("dialog.confirmUndo.message");
	}

	@Override
	public Image getImage() {
		return VCSImages.WIZ_UNDO;
	}

}
