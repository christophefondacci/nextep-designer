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
package com.nextep.datadesigner.dbgm.impl;

import java.math.BigDecimal;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.IActivity;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class Sequence extends SynchedVersionable<ISequence> implements ISequence {

	private boolean cycle = false;
	private boolean ordered = false;
	private BigDecimal start = BigDecimal.ONE;
	private BigDecimal min;
	private BigDecimal max;
	private Long increment = 1L;
	private Boolean cached = Boolean.FALSE;
	private int cacheSize;

	public Sequence(String name, IActivity activity) {
		this();
		// Setting name
		setName(name);
		// Setting unversionned version
		setVersion(VersionFactory.getUnversionedInfo(new Reference(getType(), getName(), this),
				activity));
	}

	public Sequence() {
		nameHelper.setFormatter(DBGMHelper.getCurrentVendor().getNameFormatter());
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public boolean isCycle() {
		return cycle;
	}

	@Override
	public Boolean isCached() {
		return cached;
	}

	@Override
	public Long getIncrement() {
		return increment;
	}

	@Override
	public BigDecimal getMaxValue() {
		return max;
	}

	@Override
	public BigDecimal getMinValue() {
		return min;
	}

	@Override
	public BigDecimal getStart() {
		return start;
	}

	@Override
	public boolean isOrdered() {
		return ordered;
	}

	@Override
	public void setCached(Boolean cached) {
		final Boolean old = this.cached;
		this.cached = cached;
		notifyIfChanged(old, cached, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setCycle(boolean cycle) {
		final boolean old = this.cycle;
		this.cycle = cycle;
		notifyIfChanged(old, cycle, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setIncrement(Long increment) {
		Long old = this.increment;
		this.increment = increment;
		notifyIfChanged(old, increment, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setMaxValue(BigDecimal maxValue) {
		final BigDecimal old = this.max;
		this.max = maxValue;
		notifyIfChanged(old, max, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setMinValue(BigDecimal minValue) {
		final BigDecimal old = this.min;
		this.min = minValue;
		notifyIfChanged(old, this.min, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setOrdered(boolean ordered) {
		final boolean old = this.ordered;
		this.ordered = ordered;
		notifyIfChanged(old, ordered, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setStart(BigDecimal start) {
		final BigDecimal old = start;
		this.start = start;
		notifyIfChanged(old, start, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public int getCacheSize() {
		return cacheSize;
	}

	@Override
	public void setCacheSize(int size) {
		final int old = cacheSize;
		this.cacheSize = size;
		notifyIfChanged(old, cacheSize, ChangeEvent.MODEL_CHANGED);
	}

}
