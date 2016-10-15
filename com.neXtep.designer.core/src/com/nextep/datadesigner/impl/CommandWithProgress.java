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
package com.nextep.datadesigner.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.model.ICommandWithProgress;

/**
 * Base implementation of the progress monitor
 * @author Christophe
 *
 */
public abstract class CommandWithProgress implements ICommandWithProgress {
	private IProgressMonitor monitor;
	@Override
	public IProgressMonitor getProgressMonitor() {
		if(monitor!=null) { 
			return monitor;
		} else {
			// We always return a non null progress monitor
			return new IProgressMonitor() {
				@Override
				public void beginTask(String name, int totalWork) {
				}
				@Override
				public void done() {
				}

				@Override
				public void internalWorked(double work) {
				}

				@Override
				public boolean isCanceled() {
					return false;
				}

				@Override
				public void setCanceled(boolean value) {
				}

				@Override
				public void setTaskName(String name) {
				}

				@Override
				public void subTask(String name) {
				}

				@Override
				public void worked(int work) {
				}
				
			};
		}
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

}
