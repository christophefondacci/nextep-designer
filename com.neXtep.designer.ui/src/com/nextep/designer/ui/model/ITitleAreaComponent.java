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
package com.nextep.designer.ui.model;

import org.eclipse.swt.graphics.Image;

/**
 * This interface represents UI component which provides title, description and icon information.
 * 
 * @author Christophe Fondacci
 */
public interface ITitleAreaComponent {

	/**
	 * Title of the connector
	 * 
	 * @return the connector's title
	 */
	String getAreaTitle();

	/**
	 * Description of the connector
	 * 
	 * @return the connector's description
	 */
	String getDescription();

	/**
	 * The image of the connector
	 * 
	 * @return the connector's image
	 */
	Image getImage();
}
