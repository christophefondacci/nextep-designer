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
package com.nextep.designer.core.helpers;

import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.exception.CancelException;

/**
 * A custom progress monitor which controls when the elements are passed to the "real" monitor. It
 * simply acts as a filter between the caller and the parent monitor to avoid calling the parent
 * monitor too often (since it slows down the entire process).
 * 
 * @author Christophe Fondacci
 */
public class CustomProgressMonitor implements IProgressMonitor {

	private IProgressMonitor monitor;
	private long counter;
	private int range;
	private boolean filterSubTask;

	/**
	 * Creates a new custom progress monitor which will pass ticks to its parent monitor every
	 * <code>range</code> ticks.
	 * 
	 * @param parent parent {@link IProgressMonitor} to which calls are dispatched
	 * @param range the range of this monitor.
	 * @param filterSubtask whether or not the subtask should only be propagated once per range
	 */
	public CustomProgressMonitor(IProgressMonitor parent, int range, boolean filterSubtask) {
		this.monitor = parent;
		this.range = range;
		this.filterSubTask = filterSubtask;
	}

	/**
	 * Creates a new custom progress monitor which will pass ticks to its parent monitor every
	 * <code>range</code> ticks.
	 * 
	 * @param parent parent {@link IProgressMonitor} to which calls are dispatched
	 * @param range the range of this monitor.
	 */

	public CustomProgressMonitor(IProgressMonitor parent, int range) {
		this(parent, range, false);
	}

	@Override
	public void beginTask(String name, int totalWork) {
		monitor.beginTask(name, totalWork);
	}

	@Override
	public void done() {
		monitor.done();
	}

	@Override
	public void internalWorked(double work) {
		monitor.internalWorked(work);
	}

	@Override
	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	@Override
	public void setCanceled(boolean value) {
		monitor.setCanceled(value);
	}

	@Override
	public void setTaskName(String name) {
		monitor.setTaskName(name);
	}

	@Override
	public void subTask(String name) {
		if (isRangeDone() || !filterSubTask) {
			monitor.subTask(name);
		}
	}

	@Override
	public void worked(int work) {
		if (monitor.isCanceled()) {
			throw new CancelException("Capture has been cancelled by user.");
		}
		if (isRangeDone()) {
			monitor.worked(range);
		}
	}

	private boolean isRangeDone() {
		return counter++ % range == 0;
	}
}
