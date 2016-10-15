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
package com.nextep.installer.services;

import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.model.IInstallerMonitor;

/**
 * This service provides convenience logging features for the installer. Any output message of the
 * installer is routed to this class which formats the string to output and delegates the output
 * "action" to the {@link IInstallerMonitor}.
 * 
 * @author Christophe Fondacci
 */
public interface ILoggingService {

	/**
	 * Outputs the string to the installer monitor, without any carriage return or line feed
	 * 
	 * @param str string to output
	 */
	void out(String str);

	/**
	 * Outputs the specified string padded to the specified width. This allows proper alignment on
	 * proportional font display. The string will be padded with spaces.
	 * 
	 * @param str string to output
	 * @param pad the pad width of the output to generate.
	 */
	void out(String str, int pad);

	/**
	 * Outputs an error message to the monitor.
	 * 
	 * @param err error message to display
	 */
	void error(String err);

	/**
	 * Outputs an error message to the monitor with stack trace depending on log level activation.
	 * 
	 * @param err error message to display
	 * @param t the {@link Throwable} which caused the error
	 */
	void error(String err, Throwable t);

	/**
	 * Pads all future logs to the right. Any further call to any "output" log method will generate
	 * a padded output.
	 */
	void pad();

	/**
	 * Unpads all future logs to the left.
	 */
	void unpad();

	/**
	 * Outputs the specified string to the monitor with a new line.
	 * 
	 * @param str text to log
	 */
	void log(String str);

	/**
	 * The monitor to output information to
	 * 
	 * @param monitor the {@link IInstallerMonitor} to send our logged output
	 */
	void setMonitor(IInstallerMonitor monitor);

	/**
	 * Gives access to the installer monitor for direct communication
	 * 
	 * @return the {@link IInstallerMonitor}
	 */
	IInstallerMonitor getMonitor();

	/**
	 * Repeats the specified character to build a string of the specified length.
	 * 
	 * @param car character to repeat
	 * @param length length of the string to build
	 * @return the separator string
	 */
	String getSeparator(char car, int length);

	/**
	 * Configures this logging service based on the installer configuration
	 * 
	 * @param configuration the {@link IInstallConfiguration}
	 */
	void configure(IInstallConfiguration configuration);

}
