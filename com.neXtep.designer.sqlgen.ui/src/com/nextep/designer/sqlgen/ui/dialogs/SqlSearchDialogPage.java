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
package com.nextep.designer.sqlgen.ui.dialogs;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.sqlgen.ui.impl.SQLSearchQuery;

public class SqlSearchDialogPage extends DialogPage implements ISearchPage {

	private Text searchText;
	private Button wholeWordCheck;
	private Button regExpCheck;

	public SqlSearchDialogPage() {
	}

	public SqlSearchDialogPage(String title) {
		super(title);
	}

	public SqlSearchDialogPage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public boolean performAction() {
		NewSearchUI.runQueryInBackground(new SQLSearchQuery(searchText.getText(), regExpCheck
				.getSelection(), wholeWordCheck.getSelection()));
		return true;
	}

	@Override
	public void setContainer(ISearchPageContainer container) {

	}

	@Override
	public void createControl(Composite parent) {
		Composite editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(2, false));

		Label searchTextLabel = new Label(editor, SWT.NONE);
		searchTextLabel.setText(SQLMessages.getString("search.dialog.textSearchLabel")); //$NON-NLS-1$
		searchTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		searchText = new Text(editor, SWT.BORDER);
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Space filler
		new Label(editor, SWT.NONE);

		wholeWordCheck = new Button(editor, SWT.CHECK);
		wholeWordCheck.setText(SQLMessages.getString("search.dialog.wholeWord")); //$NON-NLS-1$
		wholeWordCheck.setSelection(true);

		// Space filler
		new Label(editor, SWT.NONE);

		regExpCheck = new Button(editor, SWT.CHECK);
		regExpCheck.setText(SQLMessages.getString("search.dialog.regExp")); //$NON-NLS-1$

		setControl(editor);
	}

}
