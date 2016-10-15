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
package com.nextep.datadesigner.impl;

import org.eclipse.core.runtime.IAdaptable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IPropertyProvider;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ReferenceContext;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;

/**
 * Default implementation of a reference. This implementation is linked with the ReferenceManager to
 * ensure that all created instances are registered in the manager.<br>
 * The equality of a reference is handled for both persisted and not persisted references. For
 * persisted references, they are equal if and only if their IDs are equal. For unpersisted
 * entities, they are equal if and only if they refer the same Object instance (through the ==
 * equality).
 * 
 * @author Christophe Fondacci
 */
public class Reference extends Observable implements IReference, IAdaptable {

	// private static final Log log = LogFactory.getLog(Reference.class);
	/** The global unique ID of this reference, exists only for persisted references */
	private UID referenceId;
	/**
	 * The instance which has created this reference, exists only for newly created references or
	 * unpersisted references.
	 */
	private Object instance;
	/** Reference type */
	private IElementType type;
	/** An arbitrary name, should we need to display a single reference */
	private String arbitraryName;
	/** The volatile flag of this reference */
	private boolean isVolatile = true;
	/** Context of this reference, for volatile references loaded from db */
	private ReferenceContext context;

	/**
	 * Constructs a new version reference
	 * 
	 * @param refId unique identifier of this reference
	 * @param arbitraryName an arbitrary name of this reference used for user selection only.
	 * @param the IReferenceable object being referenced
	 */
	public Reference(IElementType type, String arbitraryName, Object instance) {
		this.type = type;
		this.instance = instance;
		// Setting the arbitrary name
		setArbitraryName(arbitraryName);
		// Referencing to the manager
		if (instance instanceof IReferenceable) {
			CorePlugin.getService(IReferenceManager.class).reference(this,
					(IReferenceable) instance);
		}
	}

	/**
	 * Hibernate empty constructor
	 */
	protected Reference() {
	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#getReferenceId()
	 */
	public UID getReferenceId() {
		return referenceId;
	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#getType()
	 */
	public IElementType getType() {
		return type;
	}

	/**
	 * Hibernate Identifier getter
	 * 
	 * @return the raw identifier
	 */
	protected long getId() {
		if (referenceId == null) {
			return 0;
		}
		return referenceId.rawId();
	}

	/**
	 * Hibernate identifier setter
	 * 
	 * @param id the raw identifier
	 */
	protected void setId(long id) {
		setUID(new UID(id));
	}

	/**
	 * Hibernate type setter
	 * 
	 * @param type new type to set
	 */
	protected void setType(IElementType type) {
		this.type = type;
	}

	/**
	 * Visual representation of this reference
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + (isVolatile() ? "volatile" : "") + "Ref:"
				+ (type != null ? type.getId() : null) + ":" + referenceId + ":"
				+ getArbitraryName() + ")";
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Reference) {
			if (this == obj)
				return true;
			Reference ref = (Reference) obj;
			// ///// TO COMMENT AND TEST
			// if(isVolatile() || ref.isVolatile()) {
			// return (this.instance == ref.instance); // || (this.instance!= null &&
			// this.instance.equals(ref.instance));
			// }
			// ///// END TO COMMENT
			if (referenceId != null) {
				return this.referenceId.equals(ref.getReferenceId())
						&& this.getType() == ref.getType();
			} else if (ref.getReferenceId() != null) {
				// False because we compare a null referenceId (not persisted) with a not null
				// referenceId (persisted)
				return false;
			} else {
				// Redundant
				return this.instance == ref.instance;
				// return this.getArbitraryName().equals(ref.getArbitraryName()) && this.getType()
				// == ref.getType();
			}
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// /////////// TO COMMENT
		// if(instance != null && isVolatile()) { //isVolatile() || referenceId == null) {
		// return instance.hashCode();
		// }
		// return ((type!=null ? type.getId() : "Unknown") +
		// (referenceId!=null?referenceId.toString():"NULL")).hashCode();
		// /////////// END TO COMMENT

		// TO UNCOMMENT AND TEST
		return (type != null ? type.getId() : "Unknown").hashCode();

	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#getArbitraryName()
	 */
	public String getArbitraryName() {
		return arbitraryName;
	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#setArbitraryName(java.lang.String)
	 */
	public void setArbitraryName(String arbitraryName) {
		if (arbitraryName == null || arbitraryName.length() < 200) {
			this.arbitraryName = arbitraryName;
		} else {
			this.arbitraryName = arbitraryName.substring(0, 200);
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#getReference()
	 */
	@Override
	public IReference getReference() {
		return this;
	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#setReference(com.nextep.datadesigner.impl.Reference)
	 */
	@Override
	public void setReference(IReference ref) {
		// Nonsense
	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#getUID()
	 */
	@Override
	public UID getUID() {
		return referenceId;
	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#setUID(com.nextep.datadesigner.model.UID)
	 */
	@Override
	public void setUID(UID id) {
		// UID oldId = this.referenceId;
		this.referenceId = id;
		// if(oldId != referenceId && referenceId != null && instance!=null) {
		// this.setVolatile(false);
		// if(instance instanceof IReferenceable) {
		// CorePlugin.getService(IReferenceManager.class).reference(this, (IReferenceable)instance);
		// }
		// }

	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#setInstance(com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public void setInstance(IReferenceable referenceable) {
		this.instance = referenceable;
	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#getInstance()
	 */
	public IReferenceable getInstance() {
		return (IReferenceable) this.instance;
	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#isVolatile()
	 */
	@Override
	public boolean isVolatile() {
		// Commented the "referenceId==null" because it causes Merge to fail
		// During merge objects are retrieved from sand box and have an ID.
		// They must be considered as volatiles.
		//
		// The " && referenceId==null" clause was added for ??? Somethind related
		// to deliveries. It might be related to a delivery module load which
		// was done in sandbox rather than in current session. Since it has been
		// fixed, it may work now... Be vigilant when modifying this!!!
		return (isVolatile /* && (referenceId == null) */) || (referenceId == null);
	}

	/**
	 * @see com.nextep.datadesigner.model.IReference#setVolatile()
	 */
	@Override
	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertyProvider.class) {
			return new ReferencePropertyProvider(this);
		}
		return null;
	}

	@Override
	public ReferenceContext getReferenceContext() {
		return context;
	}

	@Override
	public void setReferenceContext(ReferenceContext context) {
		if (this.context == null) {
			this.context = context;
		}
	}
}
