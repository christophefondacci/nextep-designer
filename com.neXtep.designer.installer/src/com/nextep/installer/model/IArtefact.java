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
package com.nextep.installer.model;

import com.neXtep.shared.model.ArtefactType;
import com.nextep.installer.parsers.DescriptorParser;

/**
 * This interface represents a delivery artefact.<br>
 * An artefact is a file whose contents could be interpreted and deployed to a target database.<br>
 * i.e. : a SQL script could be an artefact, a CSV data file, another delivery, etc. <br>
 * <br>
 * An artefact is an element of a delivery which can be deployed on a target database. Artefacts are
 * processed by the {@link DeployEngine} which delegates the deployment to a {@link IDeployHandler}.<br>
 * Artefacts are file-based and defines a {@link ArtefactType} so that the deploy engine can know
 * which handler to use for a given artefact.<br>
 * Artefacts are generated from the XML-based delivery file by the {@link DescriptorParser}.
 * 
 * @author Christophe Fondacci
 */
public interface IArtefact {

	/**
	 * Relative path of the artefact in the current delivery
	 * 
	 * @return a relative path to this artefact
	 */
	public String getRelativePath();

	/**
	 * Defines the relative path to this arteface
	 * 
	 * @param path path to the artefact, relative to the current delivery location
	 */
	public void setRelativePath(String path);

	/**
	 * @return the filename of this artefact
	 */
	public String getFilename();

	/**
	 * Defines the filename of this artefact
	 * 
	 * @param filename
	 */
	public void setFilename(String filename);

	/**
	 * @return the type of this artefact
	 */
	public ArtefactType getType();

	/**
	 * Defines the type of this artefact
	 * 
	 * @param type
	 */
	public void setType(ArtefactType type);

	/**
	 * @return the parent delivery which defines this artefact
	 */
	public IDelivery getDelivery();

	/**
	 * Sets the delivery which defines this artefact
	 * 
	 * @param delivery
	 */
	public void setDelivery(IDelivery delivery);

	/**
	 * Defines the {@link DBVendor} for which this artefact is meant to be applied.
	 * 
	 * @param vendor the DBVendor on which this IArtefact is applicable
	 */
	public void setDBVendor(DBVendor vendor);

	/**
	 * @return the {@link DBVendor} on which this IArtefact is applicable
	 */
	public DBVendor getDBVendor();
}
