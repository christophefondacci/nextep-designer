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
package com.nextep.designer.synch.ui.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.expressions.PropertyTester;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.vcs.model.ComparisonScope;

public class SynchronizationScopePropertyTester extends PropertyTester {

	private final static Log LOGGER = LogFactory.getLog(SynchronizationScopePropertyTester.class);

	public SynchronizationScopePropertyTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		final ISynchronizationResult result = (ISynchronizationResult) receiver;
		if (result != null) {
			try {
				return result.getComparisonScope() == ComparisonScope.valueOf(property);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Internal error while checking comparison scope", e); //$NON-NLS-1$
				return false;
			}
		}
		return false;
	}

}
