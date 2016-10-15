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
/**
 *
 */
package com.nextep.datadesigner.dbgm.impl;


/**
 * UNUSED DUE TO HIBERNATE SESSIONS
 * @author Christophe Fondacci
 *
 */
//public class ReferencedColumn extends Reference implements IBasicColumn {

//	private Reference ref;
//	public ReferencedColumn(Reference colRef) {
//		super(colRef.getType(),colRef.getArbitraryName(),this);
//		this.setUID(colRef.getReferenceId());
//	}
//
//	private IBasicColumn col() {
//		return (IBasicColumn)CorePlugin.getService(IReferenceManager.class).getReferencedItem(ref);
//	}
//	/**
//	 * @see com.nextep.datadesigner.dbgm.model.IBasicColumn#getDatatype()
//	 */
//	@Override
//	public IDatatype getDatatype() {
//		return col().getDatatype();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.dbgm.model.IBasicColumn#getDefaultExpr()
//	 */
//	@Override
//	public String getDefaultExpr() {
//		return col().getDefaultExpr();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.dbgm.model.IBasicColumn#getParent()
//	 */
//	@Override
//	public IBasicTable getParent() {
//		return col().getParent();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.dbgm.model.IBasicColumn#getRank()
//	 */
//	@Override
//	public int getRank() {
//		return col().getRank();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.dbgm.model.IBasicColumn#isNotNull()
//	 */
//	@Override
//	public boolean isNotNull() {
//		return col().isNotNull();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.dbgm.model.IBasicColumn#setDatatype(com.nextep.datadesigner.dbgm.model.IDatatype)
//	 */
//	@Override
//	public void setDatatype(IDatatype datatype) {
//		col().setDatatype(datatype);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.dbgm.model.IBasicColumn#setDefaultExpr(java.lang.String)
//	 */
//	@Override
//	public void setDefaultExpr(String expr) {
//		col().setDefaultExpr(expr);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.dbgm.model.IBasicColumn#setNotNull(boolean)
//	 */
//	@Override
//	public void setNotNull(boolean notNull) {
//		col().setNotNull(notNull);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.dbgm.model.IBasicColumn#setParent(com.nextep.datadesigner.dbgm.model.IBasicTable)
//	 */
//	@Override
//	public void setParent(IBasicTable table) {
//		col().setParent(table);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.dbgm.model.IBasicColumn#setRank(int)
//	 */
//	@Override
//	public void setRank(int rank) {
//		col().setRank(rank);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
//	 */
//	@Override
//	public IElementType getType() {
//		return col().getType();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.IdentifiedObject#getUID()
//	 */
//	@Override
//	public UID getUID() {
//		return col().getUID();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.IdentifiedObject#setUID(com.nextep.datadesigner.model.UID)
//	 */
//	@Override
//	public void setUID(UID id) {
//		col().setUID(id);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.INamedObject#getDescription()
//	 */
//	@Override
//	public String getDescription() {
//		return col().getDescription();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.INamedObject#getName()
//	 */
//	@Override
//	public String getName() {
//		return col().getName();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.INamedObject#setDescription(java.lang.String)
//	 */
//	@Override
//	public void setDescription(String description) {
//		col().setDescription(description);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.INamedObject#setName(java.lang.String)
//	 */
//	@Override
//	public void setName(String name) {
//		col().setName(name);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.IObservable#addListener(com.nextep.datadesigner.model.IEventListener)
//	 */
//	@Override
//	public void addListener(IEventListener listener) {
//		col().addListener(listener);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.IObservable#addListener(com.nextep.datadesigner.model.IEventListener, boolean)
//	 */
//	@Override
//	public void addListener(IEventListener listener, boolean delayed) {
//		col().addListener(listener,delayed);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.IObservable#getListeners()
//	 */
//	@Override
//	public Collection<IEventListener> getListeners() {
//		return col().getListeners();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.IObservable#notifyListeners(com.nextep.datadesigner.model.ChangeEvent, java.lang.Object)
//	 */
//	@Override
//	public void notifyListeners(ChangeEvent event, Object o) {
//		col().notifyListeners(event, o);
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.IObservable#removeListener(com.nextep.datadesigner.model.IEventListener)
//	 */
//	@Override
//	public void removeListener(IEventListener listener) {
//		col().removeListener(listener);
//	}
//
//	/**
//	 * @see com.nextep.designer.vcs.model.IVersionControlled#getModel()
//	 */
//	@Override
//	public IBasicColumn getModel() {
//		return col().getModel();
//	}
//
//	/**
//	 * @see com.nextep.designer.vcs.model.IVersionControlled#lockUpdates()
//	 */
//	@Override
//	public void lockUpdates() {
//		col().lockUpdates();
//	}
//
//	/**
//	 * @see com.nextep.designer.vcs.model.IVersionControlled#unlockUpdates()
//	 */
//	@Override
//	public void unlockUpdates() {
//		col().unlockUpdates();
//	}
//
//	/**
//	 * @see com.nextep.designer.vcs.model.IVersionControlled#updatesLocked()
//	 */
//	@Override
//	public boolean updatesLocked() {
//		return col().updatesLocked();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.IReferenceable#getReference()
//	 */
//	@Override
//	public IReference getReference() {
//		return col().getReference();
//	}
//
//	/**
//	 * @see com.nextep.datadesigner.model.IReferenceable#setReference(com.nextep.datadesigner.impl.Reference)
//	 */
//	@Override
//	public void setReference(IReference ref) {
//		col().setReference(ref);
//	}
//
//	/**
//	 * @see java.lang.Comparable#compareTo(java.lang.Object)
//	 */
//	@Override
//	public int compareTo(IBasicColumn o) {
//		return col().compareTo(o);
//	}
//
//}
