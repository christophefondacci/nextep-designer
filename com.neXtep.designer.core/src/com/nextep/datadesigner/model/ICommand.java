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
/**
 *
 */
package com.nextep.datadesigner.model;

/**
 * Command pattern interface.
 *
 * @author Christophe Fondacci
 */
public interface ICommand {
	/** Extension ID of the command extension point */
	public final String COMMAND_EXTENSION_ID = "com.neXtep.designer.core.command";
	/**
	 * Executes this command. This method will accept a variable
	 * number of arguments depending on the implementor.
	 *
	 * @param parameters parameters to pass to the command
	 * @return the result of the command
	 */
	public abstract Object execute(Object... parameters);
	/**
	 * The duration will be used when executing a batch of commands
	 * to estimate the duration of each phase. This allows to
	 * display a reliable progress bar.
	 *
	 * @return the estimated duration of this command
	 */
//	public abstract int getEstimatedDuration();
	/**
	 * @return the command name
	 */
	public abstract String getName();
}
