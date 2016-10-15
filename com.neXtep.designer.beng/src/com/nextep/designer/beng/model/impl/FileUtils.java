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
package com.nextep.designer.beng.model.impl;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;
import com.nextep.designer.sqlgen.services.IGenerationService;

/**
 * @author Christophe Fondacci
 */
public class FileUtils {

	private static final Log log = LogFactory.getLog(FileUtils.class);
	private static final int FILE_BUFFER = 10240;

	/**
	 * Writes the specified contents to a file at the given file location.
	 * 
	 * @param fileName name of the file to create (will be replaced if exists)
	 * @param contents contents to write to the file.
	 */
	public static void writeToFile(String fileName, String contents) {
		// Retrieves the encoding specified in the preferences
		String encoding = SQLGenUtil.getPreference(PreferenceConstants.SQL_SCRIPT_ENCODING);
		final boolean convert = SQLGenUtil
				.getPreferenceBool(PreferenceConstants.SQL_SCRIPT_NEWLINE_CONVERT);
		if (convert) {
			String newline = CorePlugin.getService(IGenerationService.class).getNewLine();
			// Converting everything to \n then \n to expected new line
			contents = contents.replace("\r\n", "\n");
			contents = contents.replace("\r", "\n");
			contents = contents.replace("\n", newline);
		}
		Writer w = null;
		try {
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)),
					encoding));
			w.write(contents);
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
	}

	public static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				throw new ErrorException(e);
			}
		}
	}

	public static void copyFile(File srcFile, File destFile) throws IOException {
		InputStream in = new FileInputStream(srcFile);
		OutputStream out = new FileOutputStream(destFile);
		long millis = System.currentTimeMillis();
		copyStreams(in, out);
		out.close();
		in.close();
		millis = System.currentTimeMillis() - millis;
		log.info("File " + srcFile + " copied in " + (millis / 1000L) + "s."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public static void copyStreams(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[FILE_BUFFER];
		int bytesRead;
		while ((bytesRead = in.read(buffer)) >= 0) {
			out.write(buffer, 0, bytesRead);
		}
	}
}
