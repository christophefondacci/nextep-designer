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
package com.nextep.designer.core.model.impl;

import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.model.base.AbstractController;

/**
 * This no operation controller does absolutely nothing. It is used by the {@link ControllerFactory}
 * when a controller cannot be found in the extension registry.<br>
 * Note that the use of this class reveals configuration problems. Proper platform configuration
 * should never make use of this class.
 * 
 * @author Christophe Fondacci
 */
public class DefaultController extends AbstractController {

	@Override
	public void modelChanged(Object content) {

	}

	@Override
	public void modelDeleted(Object content) {
	}

}
