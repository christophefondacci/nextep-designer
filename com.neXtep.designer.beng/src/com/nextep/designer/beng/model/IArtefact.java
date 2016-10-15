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
package com.nextep.designer.beng.model;

import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * An artefact is an element which should be generated.
 * It is part of the build engine model and they compose
 * a {@link IDeliveryModule} to indicate what should be
 * generated.<br>
 * Since they indicate what to generate, they define an
 * initial and a target release of the underlying elements.
 * 
 * @author Christophe Fondacci
 *
 */
public interface IArtefact extends INamedObject, IObservable, IdentifiedObject {

	/**
	 * @return the reference of the undelrying element
	 * 		   to generate.
	 */
	public abstract IReference getUnderlyingReference();
	/**
	 * Defines the reference of an underlying element to
	 * generate.
	 * 
	 * @param ref reference of the element to generate
	 */
	public abstract void setUnderlyingReference(IReference ref);
	/**
	 * @return the initial release to start generation from. This
	 * 		   release may be <code>null</code> to indicate a
	 * 		   generation from scratch.
	 */
	public abstract IVersionInfo getInitialRelease();
	/**
	 * Defines the initial release from which the generation
	 * should start. If this initial release is <code>null</code>
	 * it will indicate the generator to completely create this
	 * element.<br>
	 * <b>NOTE:</b> if initial release is <code>null</code>, target
	 * release must <b>not</b> be <code>null</code> 
	 * 
	 * @param release the intial release to start generation from 
	 */
	public abstract void setInitialRelease(IVersionInfo release);
	/**
	 * @return the target release which should be generated. This
	 * 		   release may be <code>null</code> to indicate that 
	 * 		   the generator should drop this object.
	 */
	public abstract IVersionInfo getTargetRelease();
	/**
	 * Defines the target release to generate. If this target
	 * release is <code>null</code> it will tell the generator
	 * to drop this object (using the drop strategies).<br>
	 * <b>NOTE:</b> if the target release is <code>null</code>, the
	 * initial release must <b>not</b> be <code>null</code>
	 * 
	 * @param release target release to generate
	 */
	public abstract void setTargetRelease(IVersionInfo release);
	/**
	 * @return the artefact creation type (default is MANUAL)
	 */
	public abstract ArtefactMode getType();
	/**
	 * @param type artefact creation type
	 */
	public abstract void setType(ArtefactMode type);
	/**
	 * @return the delivery module in which this artefact is defined
	 */
	public abstract IDeliveryModule getDelivery();
	/**
	 * Defines the delivery module of this artefact
	 * @param delivery delivery module
	 */
	public abstract void setDelivery(IDeliveryModule delivery);
}
