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

import java.util.Set;
import com.nextep.datadesigner.model.IReference;

/**
 * This interface represents a generic database trigger. The trigger declaration may be handled by
 * the implementation according to the custom flag. A non custom trigger will auto-generate its
 * declaration while a custom trigger will
 * 
 * @author Christophe Fondacci
 */
public interface ITrigger extends IDatabaseObject<ITrigger>, ISqlBased, IParseable {

	public static final String TYPE_ID = "TRIGGER"; //$NON-NLS-1$

	/**
	 * Retrieves the time when this trigger fires.
	 * 
	 * @return a {@link TriggerTime} enum
	 */
	public abstract TriggerTime getTime();

	/**
	 * Defines the time when this trigger fires
	 * 
	 * @param time a {@link TriggerTime} enum
	 */
	public abstract void setTime(TriggerTime time);

	/**
	 * @return the event which causes this trigger to fire
	 */
	public abstract Set<TriggerEvent> getEvents();

	/**
	 * Adds an event to the events which causes this trigger to fire.<br>
	 * Note that some database vendors may only support one firing event.
	 * 
	 * @param event event which activates this trigger
	 */
	public abstract void addEvent(TriggerEvent event);

	/**
	 * Removes an event to the events which causes this trigger to fire
	 * 
	 * @param event event which activates this trigger
	 */
	public abstract void removeEvent(TriggerEvent event);

	/**
	 * @return the source code of this trigger definition
	 * @deprecated please use getSql() instead
	 */
	@Deprecated
	public abstract String getSourceCode();

	/**
	 * Defines the source code of this trigger definition
	 * 
	 * @param source source code of this trigger
	 */
	public abstract void setSourceCode(String source);

	/**
	 * @return whether this trigger is customly declared (designer will ignore event and time to
	 *         only consider the source code declaration).
	 */
	public abstract boolean isCustom();

	/**
	 * Defines whether this trigger is customized.
	 * 
	 * @see ITrigger#isCustom()
	 * @param isCustom is this trigger customized
	 */
	public abstract void setCustom(boolean isCustom);

	/**
	 * @return the table to which this trigger is associated
	 */
	public abstract IReference getTriggableRef();

	/**
	 * Defines the table to which this trigger is associated
	 * 
	 * @param table associated table
	 */
	public abstract void setTriggableRef(IReference table);

}
