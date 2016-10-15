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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.headless.model.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A progress monitor which reports its work to a logger. This progress monitor could be configured
 * to report work using dot ticks, or not.
 * 
 * @author Christophe Fondacci
 */
public class HeadlessProgressMonitor implements IProgressMonitor {

	private static final Log STDOUT_LOGGER = LogFactory.getLog("OUT"); //$NON-NLS-1$
	private static final Log LOGGER = LogFactory.getLog(HeadlessProgressMonitor.class);
	private boolean reportWork;
	private String context = "";

	public HeadlessProgressMonitor(boolean reportWork) {
		this.reportWork = reportWork;
	}

	@Override
	public void worked(int work) {
		if (reportWork) {
			for (int i = 0; i < work; i++) {
				STDOUT_LOGGER.info('.');
			}
		}
	}

	private void insertLineFeed() {
		if (reportWork) {
			STDOUT_LOGGER.info("\n"); //$NON-NLS-1$
		}
	}

	@Override
	public void subTask(String name) {
		insertLineFeed();
		LOGGER.info(context + name);
	}

	@Override
	public void setTaskName(String name) {
		insertLineFeed();
		LOGGER.info(context + name);
	}

	@Override
	public void setCanceled(boolean value) {

	}

	@Override
	public boolean isCanceled() {
		return false;
	}

	@Override
	public void internalWorked(double work) {
	}

	@Override
	public void done() {
		insertLineFeed();
	}

	@Override
	public void beginTask(String name, int totalWork) {
		LOGGER.info(context + name);
	}

	public void setContext(String context) {
		this.context = "[" + context + "] ";
	}
}
