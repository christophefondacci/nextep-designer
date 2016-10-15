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
package com.nextep.designer.headless.batch.model.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.sqlgen.model.IGenerationConsole;
import com.nextep.designer.headless.batch.BatchMessages;
import com.nextep.designer.util.Assert;

/**
 * A headless generation console which writes the generation logs to a common logger.
 * 
 * @author Christophe Fondacci
 */
public class GenerationHeadlessConsole implements IGenerationConsole {

	private IProgressMonitor monitor;

	public GenerationHeadlessConsole(IProgressMonitor monitor) {
		Assert.notNull(monitor, "Cannot use a null monitor");
		this.monitor = monitor;
	}

	@Override
	public void start() {
		monitor.subTask(BatchMessages.getString("console.startSqlGeneration")); //$NON-NLS-1$
	}

	@Override
	public void end() {
		monitor.subTask(BatchMessages.getString("console.endSqlGeneration")); //$NON-NLS-1$
	}

	@Override
	public void log(String text) {
		monitor.subTask("  " + text); //$NON-NLS-1$
	}

}
