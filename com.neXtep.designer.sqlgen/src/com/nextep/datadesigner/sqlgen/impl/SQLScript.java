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
package com.nextep.datadesigner.sqlgen.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;

/**
 * Default implementation for SQL scripts. This implementation could be either a repository
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class SQLScript extends IDNamedObservable implements ISQLScript {

	private StringBuffer sql;
	private ScriptType scriptType;
	private String directory;
	private boolean external = false;

	/**
	 * This constructor is provided for SQLScript creation from SQL generators which do not depend
	 * on the version control system (VCS). Therefore they cannot initialize an activity. Since this
	 * class is a <code>IVersionable</code> object it MUST initialize a <code>VersionInfo</code>
	 * structure. So this constructor uses the default activity provided by <code>Activity</code>
	 * implementation.
	 * 
	 * @param name
	 * @param description
	 * @param sql
	 */
	public SQLScript(String name, String description, String sql, ScriptType scriptType) {
		nameHelper.setFormatter(IFormatter.LOWERCASE);
		setName(name);
		setDescription(description == null ? "" : description); //$NON-NLS-1$
		setSql(sql);
		this.scriptType = scriptType;
	}

	protected SQLScript() {
		nameHelper.setFormatter(IFormatter.LOWERCASE);
		setName(""); //$NON-NLS-1$
		setSql(""); //$NON-NLS-1$
		this.scriptType = ScriptType.CUSTOM;
	}

	public SQLScript(ScriptType type) {
		this();
		setScriptType(type);
	}

	/**
	 * Constructor for file system based scripts.
	 * 
	 * @param directory SQL file directory
	 * @param filename SQL filename
	 */
	public SQLScript(String directory, String filename) {
		this();
		initializeExternal(directory, filename);
	}

	protected void initializeExternal(String directory, String filename) {
		// Setting external script settings
		setExternal(true);
		setDirectory(directory);

		// Defining script name and type from file extension if available
		int pointPos = filename.lastIndexOf("."); //$NON-NLS-1$
		if (pointPos != -1) {
			setName(filename.substring(0, pointPos));
			String ext = filename.substring(pointPos).toLowerCase();
			setScriptType(guessScriptType(ext));
		} else {
			setName(filename);
			setScriptType(ScriptType.CUSTOM);
		}

		// Loading SQL contents
		setSql(""); //$NON-NLS-1$
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(new File(getAbsolutePathname())));
			byte[] buffer = new byte[1024 * 4];

			int bytesRead = 0;
			while ((bytesRead = bis.read(buffer)) != -1) {
				appendSQL(new String(buffer, 0, bytesRead));
			}
		} catch (FileNotFoundException e) {
			throw new ErrorException(e);
		} catch (IOException e) {
			throw new ErrorException(e);
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					throw new ErrorException(e);
				}
			}
		}
	}

	public SQLScript(String path) {
		this();
		int fileSep = path.lastIndexOf(File.separatorChar);
		if (fileSep != -1) {
			initializeExternal(path.substring(0, fileSep), path.substring(fileSep + 1));
		}
	}

	private ScriptType guessScriptType(String extension) {
		for (ScriptType t : ScriptType.values()) {
			if (t.getFileExtension().equals(extension)) {
				return t;
			}
		}
		return ScriptType.CUSTOM;
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public String getSql() {
		return sql.toString();
	}

	@Override
	public void setSql(String content) {
		this.sql = new StringBuffer(1024 * 4);
		appendSQL(content == null ? "" : content); //$NON-NLS-1$
	}

	@Override
	public ISQLScript appendSQL(String content) {
		if (null == sql) {
			setSql(content);
		}
		sql.append(content);
		return this;
	}

	@Override
	public ISQLScript appendSQL(char c) {
		sql.append(c);
		return this;
	}

	@Override
	public ISQLScript appendSQL(BigDecimal d) {
		sql.append(d);
		return this;
	}

	@Override
	public ISQLScript appendSQL(Long l) {
		sql.append(l);
		return this;
	}

	@Override
	public ISQLScript appendSQL(int i) {
		sql.append(i);
		return this;
	}

	@Override
	public ScriptType getScriptType() {
		return scriptType;
	}

	@Override
	public void setScriptType(ScriptType scriptType) {
		this.scriptType = scriptType;
	}

	@Override
	public final String getFilename() {
		String filename = getName();

		//		if (filename != null && !filename.contains(".")) { //$NON-NLS-1$
		filename += scriptType.getFileExtension();
		// }

		return filename;
	}

	@Override
	public final String getAbsolutePathname() {
		String absPathname = getDirectory();

		if (absPathname != null && !absPathname.endsWith(File.separator)
				&& !absPathname.endsWith("/") && !absPathname.endsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			absPathname += File.separator;
		}
		// Fixing the NPE problem
		if (absPathname == null) {
			absPathname = getFilename();
		} else {
			absPathname += getFilename();
		}

		return absPathname;
	}

	@Override
	public void appendScript(ISQLScript script) {
		if (script instanceof SQLWrapperScript) {
			// Unwrapping script
			for (ScriptType t : ScriptType.values()) {
				for (ISQLScript s : ((SQLWrapperScript) script).getChildren()) {
					if (s.getScriptType() == t) {
						appendScript(s);
					}
				}
			}
		} else {
			if (script != null) {
				appendSQL(script.getSql());
			}
		}
	}

	@Override
	public String getDirectory() {
		return directory;
	}

	@Override
	public boolean isExternal() {
		return external;
	}

	@Override
	public void setExternal(boolean external) {
		this.external = external;
	}

	@Override
	public void setDirectory(String directory) {
		this.directory = directory;
	}
}
