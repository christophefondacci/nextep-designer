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

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.dbgm.ui.services.DBGMUIHelper;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;
import com.nextep.designer.sqlgen.ui.model.IConnectable;

public class ExecuteQueryHandler extends AbstractHandler {

	private static final Log log = LogFactory.getLog(ExecuteQueryHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart p = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		if (p instanceof MultiPageEditorPart) {
			p = (IEditorPart) ((MultiPageEditorPart) p).getSelectedPage();
		}
		if (p.getEditorInput() instanceof ISQLEditorInput) {
			final ISQLEditorInput<?> input = (ISQLEditorInput<?>) p.getEditorInput();
			if (input != null) {
				// Retrieving current text selection
				ITextEditor editor = (ITextEditor) p;
				ISelectionProvider selprovider = editor.getEditorSite().getSelectionProvider();
				TextSelection selectedText = (TextSelection) selprovider.getSelection();
				// Getting our document
				final IDocument doc = ((ITextEditor) p).getDocumentProvider().getDocument(input);
				try {
					int offset = selectedText.getOffset();
					int length = selectedText.getLength();
					if (length == 0) {
						// Searching last ';' character
						FindReplaceDocumentAdapter findAdapter = new FindReplaceDocumentAdapter(doc);
						IRegion r = findAdapter.find(Math.max(0, offset - 1), ";", false, true,
								false, false);
						if (r == null) {
							offset = 0;
						} else {
							offset = Math.min(r.getOffset() + 1, doc.getLength() - 1);
						}
						// Searching next ';' character
						r = findAdapter.find(offset, ";", true, true, false, false);
						if (r == null) {
							length = doc.getLength() - offset;
						} else {
							length = r.getOffset() - offset;
						}
						// Reselecting
						selprovider.setSelection(new TextSelection(offset, length));
					}

					final String sql = doc.get(offset, length);
					final Connection conn = getConnectionFromInput(input);
					SQLClientPlugin.getService(ISQLClientService.class).runQuery(input, conn, sql);

				} catch (BadLocationException e) {
					log.warn("Problems while trying to extract the SQL query", e);
				}
			}
		}
		// TODO Auto-generated method stub
		return null;
	}

	private Connection getConnectionFromInput(ISQLEditorInput<?> input) {
		Connection sqlConn = null;
		IConnection conn = null;
		if (input instanceof IConnectable) {
			final IConnectable connectable = (IConnectable) input;
			sqlConn = connectable.getSqlConnection();
			conn = connectable.getConnection();
		}
		if (sqlConn == null) {
			if (conn != null) {
				DBGMUIHelper.checkConnectionPassword(conn);
			} else {
				conn = DBGMUIHelper.getConnection(null);
			}
			// Initiliazing connection
			IDatabaseConnector dbConnector = CorePlugin.getConnectionService()
					.getDatabaseConnector(conn);
			try {
				sqlConn = dbConnector.connect(conn);
				if (input instanceof IConnectable) {
					((IConnectable) input).setSqlConnection(sqlConn);
				}
			} catch (SQLException e) {
				throw new ErrorException("Could not establish connection : " + e.getMessage(), e);
			}
		}
		return sqlConn;
	}
}
