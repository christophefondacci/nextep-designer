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
package com.nextep.designer.ui.helpers;

import org.eclipse.core.runtime.jobs.Job;
import com.nextep.datadesigner.impl.SchedulingRuleVolatile;

/**
 * This job is a regular job that conlicts with each other. The goal of this job is to simply
 * provide a Job that needs other Blocking job to be finished before begin executed.
 * 
 * @author Christophe Fondacci
 */
public abstract class BlockingJob extends Job {

	public BlockingJob(String name) {
		super(name);
		setRule(SchedulingRuleVolatile.getInstance());
	}

}
