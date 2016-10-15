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
package com.nextep.designer.sqlgen.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.model.base.AbstractController;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class SQLScriptController extends AbstractController {

	@Override
	public void modelChanged(Object content) {
	}

	@Override
	public void modelDeleted(Object content) {
		ISQLScript s = (ISQLScript) content;
		if (s instanceof IVersionable<?>) {
			ControllerFactory.getController(IElementType.getInstance("VERSIONABLE")).modelDeleted( //$NON-NLS-1$
					content);
		}
	}

	@Override
	public void save(IdentifiedObject content) {
		ISQLScript s = (ISQLScript) content;

		if (s.isExternal()) {
			// Retrieves the encoding configured in the preferences
			String encoding = SQLGenUtil.getPreference(PreferenceConstants.SQL_SCRIPT_ENCODING);

			Writer w = null;
			try {
				w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(s
						.getAbsolutePathname())), encoding));
				w.write(s.getSql());
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
		} else {
			super.save(content);
		}
	}
}
