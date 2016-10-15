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
package com.nextep.datadesigner.vcs.gui.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.repository.RepositoryStatus;
import com.nextep.designer.vcs.ui.VCSUIMessages;

public class RepositoryUpgradeWizardPage extends WizardPage {

	private RepositoryStatus status;

	public RepositoryUpgradeWizardPage(RepositoryStatus status) {
		super("repositoryUpgrade", VCSUIMessages.getString("repositoryUpgradeTitle"), null); //$NON-NLS-1$ //$NON-NLS-2$
		setDescription(VCSUIMessages.getString("repositoryUpgradeDesc")); //$NON-NLS-1$
		this.status = status;
	}

	@Override
	public void createControl(Composite parent) {
		Composite editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout());
		Label lbl = new Label(editor, SWT.WRAP);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		lbl.setFont(FontFactory.FONT_BOLD);
		String infoMsgKey = "repositoryMsg"; //$NON-NLS-1$
		switch (status) {
		case NO_CONNECTION:
			lbl.setText(VCSUIMessages.getString("noRepositoryConnectionMsgBold")); //$NON-NLS-1$
			infoMsgKey = "repositoryConnectionMsg"; //$NON-NLS-1$
			break;
		case NO_REPOSITORY:
			lbl.setText(VCSUIMessages.getString("noRepositoryMsgBold")); //$NON-NLS-1$
			infoMsgKey = "repositoryMsg"; //$NON-NLS-1$
			break;
		case REPOSITORY_TOO_OLD:
			lbl.setText(VCSUIMessages.getString("tooOldRepositoryMsgBold")); //$NON-NLS-1$
			infoMsgKey = "repositoryMsg"; //$NON-NLS-1$
			break;
		}

		Label info = new Label(editor, SWT.WRAP);
		GridData gdInfo = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdInfo.widthHint = 300;
		info.setLayoutData(gdInfo);
		info.setText(VCSUIMessages.getString(infoMsgKey));
		setControl(editor);
	}

}
