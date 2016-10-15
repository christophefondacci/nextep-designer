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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.nextep.datadesigner.dbgm.DBGMMessages;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.dbgm.model.TriggerTime;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;

/**
 * @author Christophe Fondacci
 */
public class Trigger extends SynchedVersionable<ITrigger> implements ITrigger {

	private Set<TriggerEvent> events = new HashSet<TriggerEvent>();
	private TriggerTime time;
	private boolean isCustom = false;
	private String sourceCode;
	private IReference tableRef;
	private IParseData parseData;
	private boolean isParsed;

	public Trigger() {
		nameHelper.setFormatter(DBGMHelper.getCurrentVendor().getNameFormatter());
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#addEvent(com.nextep.datadesigner.dbgm.model.TriggerEvent)
	 */
	@Override
	public void addEvent(TriggerEvent event) {
		if (events.add(event)) {
			notifyListeners(ChangeEvent.TRIGGER_EVENTS_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#getEvents()
	 */
	@Override
	public Set<TriggerEvent> getEvents() {
		return events;
	}

	/**
	 * Hibernate events setter
	 * 
	 * @param events loaded events
	 */
	protected void setEvents(Set<TriggerEvent> events) {
		this.events = events;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#getSourceCode()
	 */
	@Override
	public String getSourceCode() {
		return sourceCode;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#getTime()
	 */
	@Override
	public TriggerTime getTime() {
		return time;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#isCustom()
	 */
	@Override
	public boolean isCustom() {
		return isCustom;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#removeEvent(com.nextep.datadesigner.dbgm.model.TriggerEvent)
	 */
	@Override
	public void removeEvent(TriggerEvent event) {
		if (events.remove(event)) {
			notifyListeners(ChangeEvent.TRIGGER_EVENTS_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#setCustom(boolean)
	 */
	@Override
	public void setCustom(boolean isCustom) {
		if (this.isCustom != isCustom) {
			this.isCustom = isCustom;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#setSourceCode(java.lang.String)
	 */
	@Override
	public void setSourceCode(String source) {
		this.sourceCode = source;
		notifyListeners(ChangeEvent.SOURCE_CHANGED, source);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#setTime(com.nextep.datadesigner.dbgm.model.TriggerTime)
	 */
	@Override
	public void setTime(TriggerTime time) {
		this.time = time;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#getTriggableRef()
	 */
	@Override
	public IReference getTriggableRef() {
		return tableRef;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITrigger#setTriggableRef(IReference)
	 */
	@Override
	public void setTriggableRef(IReference table) {
		if (this.tableRef != table) {
			this.tableRef = table;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	// Hibernate helpers
	protected boolean isOnInsert() {
		return getEvents().contains(TriggerEvent.INSERT);
	}

	protected boolean isOnUpdate() {
		return getEvents().contains(TriggerEvent.UPDATE);
	}

	protected boolean isOnDelete() {
		return getEvents().contains(TriggerEvent.DELETE);
	}

	protected void setOnInsert(boolean onInsert) {
		if (onInsert) {
			addEvent(TriggerEvent.INSERT);
		}
	}

	protected void setOnUpdate(boolean onUpdate) {
		if (onUpdate) {
			addEvent(TriggerEvent.UPDATE);
		}
	}

	protected void setOnDelete(boolean onDelete) {
		if (onDelete) {
			addEvent(TriggerEvent.DELETE);
		}
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		List<IReference> refs = new ArrayList<IReference>(super.getReferenceDependencies());
		refs.add(getTriggableRef());
		return refs;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#updateReferenceDependencies(com.nextep.datadesigner.model.IReference,
	 *      com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		if (oldRef.equals(getTriggableRef())) {
			setTriggableRef(newRef);
			return true;
		}
		return false;
	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		super.checkConsistency();
		if (!isCustom() && getEvents().size() == 0) {
			throw new InconsistentObjectException(MessageFormat.format(DBGMMessages
					.getString("triggerEmptyEvents"), getName())); //$NON-NLS-1$
		}
		if (getTriggableRef() == null) {
			throw new InconsistentObjectException(DBGMMessages
					.getString("consistency.trigger.noParentTriggable")); //$NON-NLS-1$
		}
	}

	@Override
	public String getSql() {
		return getSourceCode();
	}

	@Override
	public void setSql(String sql) {
		setSourceCode(sql);
	}

	@Override
	public IParseData getParseData() {
		return parseData;
	}

	@Override
	public boolean isParsed() {
		return isParsed;
	}

	@Override
	public void setParseData(IParseData parseData) {
		this.parseData = parseData;
	}

	@Override
	public void setParsed(boolean parsed) {
		this.isParsed = parsed;
	}
}
