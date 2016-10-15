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
package com.nextep.designer.sqlgen.ui.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.sqlgen.model.IGenerationConsole;
import com.nextep.datadesigner.sqlgen.model.IGenerationSubmitter;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.services.BuildResult;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.model.IGenerationResult;

public class GenerationResultSubmitter implements IGenerationSubmitter  {

	private IGenerationSubmitter submitter;
	
	public GenerationResultSubmitter(IGenerationSubmitter submitter, IGenerationResult result) {
		this.submitter = submitter;
		setGenerationResult(result);
	}
	
	@Override
	public IGenerationConsole getConsole() {
		return submitter.getConsole();
	}

	@Override
	public IGenerationResult getGenerationResult() {
		return submitter.getGenerationResult();
	}

	@Override
	public void setConsole(IGenerationConsole console) {
		submitter.setConsole(console);
	}

	@Override
	public void setGenerationResult(IGenerationResult result) {
		submitter.setGenerationResult(result);
	}

	@Override
	public BuildResult submit(IProgressMonitor monitor, ISQLScript script,
			IConnection conn) {
		return submitter.submit(monitor, script, conn);
	}
	public void submit(IProgressMonitor monitor, IConnection conn) {
		for(ISQLScript s : getGenerationResult().buildScript()) {
			submit(monitor, s,conn);
		}
	}

	@Override
	public DBVendor getVendor() {
		return submitter.getVendor();
	}
	
}
