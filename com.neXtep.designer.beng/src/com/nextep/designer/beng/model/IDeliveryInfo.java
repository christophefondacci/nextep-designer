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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.beng.model;

import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * This interface defines information about a given delivery. It is a lightweight descriptor of a
 * delivery that allows processes to analyze a delivery without (or before) loading it.
 * 
 * @author Christophe Fondacci
 */
public interface IDeliveryInfo extends IdentifiedObject, INamedObject, Comparable<Object> {

	/**
	 * The target release of this delivery
	 * 
	 * @return the target {@link IVersionInfo} that this delivery can upgrade to
	 */
	IVersionInfo getTargetRelease();

	/**
	 * The source release of this delivery
	 * 
	 * @return the {@link IVersionInfo} that this delivery can upgrade
	 */
	IVersionInfo getSourceRelease();

}
