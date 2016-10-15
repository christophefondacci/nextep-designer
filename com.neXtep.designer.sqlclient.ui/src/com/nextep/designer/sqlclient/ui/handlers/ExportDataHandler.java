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
package com.nextep.designer.sqlclient.ui.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.sqlclient.ui.helpers.ExportHelper;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLResult;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;

public class ExportDataHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		final ISQLQuery query = (ISQLQuery) part.getAdapter(ISQLQuery.class);
		if (query != null) {

			FileDialog d = new FileDialog(HandlerUtil.getActiveShell(event), SWT.SAVE);
			d.setFilterExtensions(new String[] { "*.csv", "*.*" }); //$NON-NLS-1$
			d.setFilterNames(new String[] { "CSV files", "All files" });
			d.setOverwrite(true);
			d.setFileName("query.csv"); //$NON-NLS-1$
			d.setText("Select the location of the file to generate");
			String fileLocation = d.open();
			if (fileLocation != null) {
				File f = new File(fileLocation);
				// Retrieves the encoding specified in the preferences
				String encoding = SQLGenUtil.getPreference(PreferenceConstants.SQL_SCRIPT_ENCODING);
				ExportHelper.initialize();
				Writer w = null;
				try {
					w = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(f), encoding));
					final ISQLResult result = query.getResult();
					for (ISQLRowResult row : result.getRows()) {
						w.write(ExportHelper.buildCSVLine(row));
					}
				} catch (FileNotFoundException e) {
					throw new ErrorException(e);
				} catch (IOException e) {
					throw new ErrorException(e);
				} finally {
					if (w != null) {
						try {
							w.close();
						} catch (IOException e) {
							throw new ErrorException(e);
						}
					}
				}
				StringBuilder buf = new StringBuilder(300);
				buf.append("Data exported to : " + f.toString());
				if (query.hasMoreRows()) {
					buf.append("\nOnly fetched rows have been exported, to export the whole query result right click on a query from the SQL editor and select 'Export data'.");
				}
				MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell(), "File exported successfully", buf.toString());
			}
			// Ensuring query is fully fetched
			// while (query.hasMoreRows()) {
			// query.fetchNextRows();
			// while (query.isRunning()) {
			// try {
			// Thread.sleep(500);
			// } catch (InterruptedException e) {
			//
			// }
			// }
			// }
		}
		return null;
	}
}
