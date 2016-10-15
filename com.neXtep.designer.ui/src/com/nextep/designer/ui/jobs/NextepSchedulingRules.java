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
package com.nextep.designer.ui.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Default neXtep Jobs scheduling rules.
 * 
 * @author Christophe Fondacci
 */
public final class NextepSchedulingRules {

	/**
	 * The refresher scheduling rules should be used for jobs in charge of refreshing the UI to
	 * reflect model changes
	 */
	public final static ISchedulingRule REFRESHER = new ISchedulingRule() {

		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};
	/**
	 * The selectioner scheduling rule should be used for jobs that maintain the selection after
	 * versioning events.
	 */
	public final static ISchedulingRule SELECTIONER = new ISchedulingRule() {

		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == REFRESHER || rule == this;
		}
	};
}
