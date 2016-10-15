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
package com.nextep.datadesigner.dbgm.model;

import java.math.BigDecimal;
/**
 * This interface represents a database sequence
 * @author Christophe Fondacci
 *
 */
public interface ISequence extends IDatabaseObject<ISequence> {

	public static final String TYPE_ID = "SEQUENCE";
	/**
	 * @return the sequence start number
	 */
	public BigDecimal getStart();
	/**
	 * Defines the sequence start number
	 * @param start
	 */
	public void setStart(BigDecimal start);
	/**
	 * @return the sequence increment
	 */
	public Long getIncrement();
	/**
	 * Defines this sequence incremnt
	 * @param increment
	 */
	public void setIncrement(Long increment);
	/**
	 * @return the cache mode of this sequence
	 */
	public Boolean isCached();
	/**
	 * Defines the cache mode of this sequence
	 * @param cacheMode
	 */
	public void setCached(Boolean cached);
	/**
	 * @return this sequence minimum value
	 */
	public BigDecimal getMinValue();
	/**
	 * Defines the minimum value for this sequence
	 * @param minValue
	 */
	public void setMinValue(BigDecimal minValue);
	/**
	 * @return the maximum value of this sequence
	 */
	public BigDecimal getMaxValue();
	/**
	 * Defines the maximum value of this sequence
	 * @param maxValue
	 */
	public void setMaxValue(BigDecimal maxValue);
	/**
	 * @return the cycle mode of this sequence
	 */
	public boolean isCycle();
	/**
	 * Defines if this sequence can cycle
	 * @param cycle
	 */
	public void setCycle(boolean cycle);
	/**
	 * @return whether this sequence is ordered or not
	 */
	public boolean isOrdered();
	/**
	 * Defines whether this sequence is ordered or not
	 * @param ordered
	 */
	public void setOrdered(boolean ordered);
	/**
	 * Defines the cache size (only for cached
	 * sequences).
	 *
	 * @param size the new cache size
	 */
	public void setCacheSize(int size);
	/**
	 * @return the number of values cached by this sequence
	 */
	public int getCacheSize();
}
