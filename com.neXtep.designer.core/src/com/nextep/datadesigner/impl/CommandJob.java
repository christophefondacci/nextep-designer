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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import com.nextep.datadesigner.exception.CommandFinishException;
import com.nextep.datadesigner.model.ICommand;

/**
 * @author Christophe Fondacci
 *
 */
public class CommandJob extends Job {

	private ICommand[] commands;
	
	public CommandJob(String name, ICommand... commands) {
		super(name);
		this.commands = commands;
	}
	public CommandJob(ICommand cmd) {
		this(cmd.getName(),cmd);
	}
	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(getName(), commands.length);
		try {
			Object lastResult = null;
			for(ICommand c : commands) {
				monitor.setTaskName(c.getName());
				lastResult = c.execute(lastResult);
				monitor.worked(1);
				if(monitor.isCanceled()) return Status.CANCEL_STATUS;
			}
		} catch( CommandFinishException e ) {
			monitor.setTaskName(e.getMessage());
		} catch(RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
		monitor.done();
		return Status.OK_STATUS;
	}
	
	@Override
	protected void canceling() {
		super.canceling();
		this.getThread().interrupt();
	}

}
