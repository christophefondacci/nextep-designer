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
package com.nextep.designer.beng.model.impl;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.model.ArtefactMode;
import com.nextep.designer.beng.model.IArtefact;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class Artefact extends IDNamedObservable implements IArtefact {

	private IVersionInfo initialRelease;
	private IVersionInfo targetRelease;
	private ArtefactMode type = ArtefactMode.MANUAL;
	private IReference ref;
	private IDeliveryModule module;

	public Artefact(IComparisonItem item) {
		IReference ref = (IReference) CorePlugin.getIdentifiableDao().load(Reference.class,
				item.getReference().getUID());
		setUnderlyingReference(ref);
		// Setting initial reference
		if (item.getTarget() != null && item.getTarget() instanceof IVersionable) {
			setInitialRelease(((IVersionable<?>) item.getTarget()).getVersion());
		}
		// Setting target release
		if (item.getSource() != null && item.getSource() instanceof IVersionable) {
			setTargetRelease(((IVersionable<?>) item.getSource()).getVersion());
		}
		// Checking validity
		if (getInitialRelease() == null && getTargetRelease() == null) {
			throw new ErrorException(
					"Invalid artefact: non-versionable artefact or initial and target version are null."); //$NON-NLS-1$
		}
		computeName();
	}

	/**
	 * Computes the name of this artefact from its underlying reference and initial / target
	 * versions.
	 */
	private void computeName() {
		// Setting name
		IReferenceable r;
		try {
			r = VersionHelper.getReferencedItem(getUnderlyingReference());
		} catch (ErrorException e) {
			r = null;
		}
		if (r instanceof INamedObject) {
			setName(((INamedObject) r).getName());
		} else {
			if (getInitialRelease() != null) {
				// If we have an initial release, we try to initialize the name from there
				IVersionable<?> v = (IVersionable<?>) CorePlugin.getIdentifiableDao().load(
						IVersionable.class, getInitialRelease().getUID(),
						HibernateUtil.getInstance().getSandBoxSession(), false);
				if (v != null) {
					setName(v.getName());
				} else {
					setName("Unknown"); //$NON-NLS-1$
				}
			} else {
				// Otherwise we try to get the reference last known name
				final String refName = getUnderlyingReference().getArbitraryName();
				if (refName != null) {
					setName(refName);
				} else {
					// We might fall here... Setting an "unknown" name to avoid later NPE
					setName("Unknown"); //$NON-NLS-1$
				}
			}
		}
	}

	public Artefact() {
	}

	/**
	 * @see com.nextep.designer.beng.model.IArtefact#getInitialRelease()
	 */
	@Override
	public IVersionInfo getInitialRelease() {
		return initialRelease;
	}

	/**
	 * @see com.nextep.designer.beng.model.IArtefact#getTargetRelease()
	 */
	@Override
	public IVersionInfo getTargetRelease() {
		return targetRelease;
	}

	/**
	 * @see com.nextep.designer.beng.model.IArtefact#setInitialRelease(com.nextep.designer.vcs.model.IVersionInfo)
	 */
	@Override
	public void setInitialRelease(IVersionInfo release) {
		if (release != this.initialRelease) {
			this.initialRelease = release;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IArtefact#setTargetRelease(com.nextep.designer.vcs.model.IVersionInfo)
	 */
	@Override
	public void setTargetRelease(IVersionInfo release) {
		if (release != this.targetRelease) {
			this.targetRelease = release;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}

	}

	/**
	 * @see com.nextep.datadesigner.impl.NamedObservable#toString()
	 */
	@Override
	public String toString() {
		try {
			return getReference().toString()
					+ " " + getInitialRelease() + " -> " + getTargetRelease(); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (RuntimeException e) {
			return super.toString();
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IArtefact#getType()
	 */
	@Override
	public ArtefactMode getType() {
		return type;
	}

	/**
	 * @see com.nextep.designer.beng.model.IArtefact#setType(com.nextep.designer.beng.model.ArtefactMode)
	 */
	@Override
	public void setType(ArtefactMode type) {
		this.type = type;
	}

	/**
	 * @see com.nextep.designer.beng.model.IArtefact#getUnderlyingReference()
	 */
	@Override
	public IReference getUnderlyingReference() {
		return ref;
	}

	/**
	 * @see com.nextep.designer.beng.model.IArtefact#setUnderlyingReference(com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public void setUnderlyingReference(IReference ref) {
		this.ref = ref;
	}

	/**
	 * @see com.nextep.datadesigner.impl.NamedObservable#setReference(com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public void setReference(IReference ref) {
		throw new UnsupportedOperationException("Cannot set reference on artefact."); //$NON-NLS-1$
	}

	// /**
	// * @see java.lang.Object#equals(java.lang.Object)
	// */
	// @Override
	// public boolean equals(Object obj) {
	// if(obj instanceof IArtefact) {
	// IArtefact a = (IArtefact)obj;
	// if(a.getReference().getUID().equals(getUID()) {
	// if(getInitialRelease()==)
	// }
	// }
	// return super.equals(obj);
	// }

	/**
	 * @see com.nextep.datadesigner.impl.NamedObservable#getName()
	 */
	@Override
	public String getName() {
		String name = super.getName();
		if (name == null) {
			computeName();
			return super.getName();
		}
		return name;

	}

	/**
	 * @see com.nextep.designer.beng.model.IArtefact#getDelivery()
	 */
	@Override
	public IDeliveryModule getDelivery() {
		return module;
	}

	/**
	 * @see com.nextep.designer.beng.model.IArtefact#setDelivery(com.nextep.designer.beng.model.IDeliveryModule)
	 */
	@Override
	public void setDelivery(IDeliveryModule delivery) {
		this.module = delivery;
	}
}
