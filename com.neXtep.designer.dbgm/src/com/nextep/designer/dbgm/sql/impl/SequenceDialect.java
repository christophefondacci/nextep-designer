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
package com.nextep.designer.dbgm.sql.impl;

import com.nextep.designer.dbgm.sql.ISequenceDialect;

/**
 * @author Bruno Gautier
 */
public class SequenceDialect implements ISequenceDialect {

	private static final String START_WITH = "START WITH"; //$NON-NLS-1$
	private static final String RESTART_WITH = "RESTART WITH"; //$NON-NLS-1$
	private static final String INCREMENT_BY = "INCREMENT BY"; //$NON-NLS-1$
	private static final String MINVALUE = "MINVALUE"; //$NON-NLS-1$
	private static final String NO_MINVALUE = "NO MINVALUE"; //$NON-NLS-1$
	private static final String MAXVALUE = "MAXVALUE"; //$NON-NLS-1$
	private static final String NO_MAXVALUE = "NO MAXVALUE"; //$NON-NLS-1$
	private static final String CYCLE = "CYCLE"; //$NON-NLS-1$
	private static final String NO_CYCLE = "NO CYCLE"; //$NON-NLS-1$
	private static final String CACHE = "CACHE"; //$NON-NLS-1$
	private static final String NO_CACHE = "NO CACHE"; //$NON-NLS-1$
	private static final String ORDER = "ORDER"; //$NON-NLS-1$
	private static final String NO_ORDER = "NO ORDER"; //$NON-NLS-1$

	@Override
	public String getStartWithClause() {
		return START_WITH;
	}

	@Override
	public String getRestartWithClause() {
		return RESTART_WITH;
	}

	@Override
	public String getIncrementByClause() {
		return INCREMENT_BY;
	}

	@Override
	public String getMinValueClause() {
		return MINVALUE;
	}

	@Override
	public String getNoMinValueClause() {
		return NO_MINVALUE;
	}

	@Override
	public String getMaxValueClause() {
		return MAXVALUE;
	}

	@Override
	public String getNoMaxValueClause() {
		return NO_MAXVALUE;
	}

	@Override
	public String getCycleClause() {
		return CYCLE;
	}

	@Override
	public String getNoCycleClause() {
		return NO_CYCLE;
	}

	@Override
	public String getCacheClause() {
		return CACHE;
	}

	@Override
	public String getNoCacheClause() {
		return NO_CACHE;
	}

	@Override
	public String getOrderClause() {
		return ORDER;
	}

	@Override
	public String getNoOrderClause() {
		return NO_ORDER;
	}

}
