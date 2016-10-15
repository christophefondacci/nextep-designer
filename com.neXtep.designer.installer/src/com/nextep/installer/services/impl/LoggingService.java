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
package com.nextep.installer.services.impl;

import java.text.MessageFormat;
import com.nextep.installer.InstallerMessages;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.model.IInstallerMonitor;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.model.impl.InstallerConsoleMonitor;
import com.nextep.installer.services.ILoggingService;

public class LoggingService implements ILoggingService {

	private IInstallerMonitor monitor = new InstallerConsoleMonitor();
	private int pad = 0;
	private boolean isVerbose = false;

	public void error(String err) {
		log(getPaddedString(MessageFormat.format(InstallerMessages
				.getString("service.logging.errorLoggerMsg"), err))); //$NON-NLS-1$
	}

	public void log(String str) {
		monitor.log(getPaddedString(str + '\n'));
	}

	public void out(String str, int pad) {
		StringBuffer buf = new StringBuffer(str);
		if (str.length() < pad) {
			for (int i = str.length(); i < pad; i++) {
				buf.append(' ');
			}
		}
		monitor.log(getPaddedString(buf.toString()));
	}

	public void out(String str) {
		monitor.log(getPaddedString(str));
	}

	public void pad() {
		pad++;
	}

	public void setMonitor(IInstallerMonitor monitor) {
		this.monitor = monitor;
	}

	public void unpad() {
		pad--;
	}

	private String getPaddedString(String s) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < pad; i++) {
			buf.append("   "); //$NON-NLS-1$
		}
		buf.append(s);
		return buf.toString();
	}

	public String getSeparator(char car, int length) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buf.append(car);
		}
		return buf.toString();
	}

	public void error(String err, Throwable t) {
		error(err);
		if (isVerbose) {
			t.printStackTrace();
		}
	}

	public void configure(IInstallConfiguration configuration) {
		isVerbose = configuration.isOptionDefined(InstallerOption.VERBOSE);
	}

	public IInstallerMonitor getMonitor() {
		return monitor;
	}

}
