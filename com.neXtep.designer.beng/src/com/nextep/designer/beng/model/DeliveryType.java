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

import com.nextep.designer.beng.BengMessages;

/**
 * Describes a type of delivery.
 * 
 * @author Christophe Fondacci
 */
public enum DeliveryType implements Comparable<DeliveryType> {
	SYSDBA(BengMessages.getString("deliveryType.sysdba.label"), "sysdbaScripts", "SYSDBA", com.neXtep.shared.model.ArtefactType.SYSDBA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DATADEL("Data deletions", "dataDeletions", "DATADEL"),
	DDL(BengMessages.getString("deliveryType.ddl.label"), "ddlScripts", "DDL"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DML(BengMessages.getString("deliveryType.dml.label"), "dmlScripts", "DML"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DATA(BengMessages.getString("deliveryType.data.label"), "dataDefinitions", "DATA"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CUSTOM(BengMessages.getString("deliveryType.misc.label"), "miscScripts", "MISC"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public String getLabel() {
		return label;
	}

	public String getTagName() {
		return tagName;
	}

	public String getFolderName() {
		return folderName;
	}

	public com.neXtep.shared.model.ArtefactType getArtefactType() {
		return artefactType;
	}

	String label;
	String tagName;
	String folderName;
	com.neXtep.shared.model.ArtefactType artefactType;

	private DeliveryType(String label, String tagName, String folderName) {
		this(label, tagName, folderName, com.neXtep.shared.model.ArtefactType.SQL);
	}

	private DeliveryType(String label, String tagName, String folderName,
			com.neXtep.shared.model.ArtefactType artefactType) {
		this.label = label;
		this.tagName = tagName;
		this.folderName = folderName;
		this.artefactType = artefactType;
	}
}
