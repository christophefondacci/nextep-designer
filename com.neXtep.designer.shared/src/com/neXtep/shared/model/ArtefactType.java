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
package com.neXtep.shared.model;

/**
 * This interface is the bridge between the IDE build engine
 * and the installer.
 * The artefact types are the ones which the installer is able
 * to deploy.
 * This class must be 1.4 compliant but has been designed for
 * smooth enum switch. 
 * @author Christophe Fondacci
 *
 */
public class ArtefactType {
	public static final ArtefactType SQL 		= new ArtefactType("SQL");
	public static final ArtefactType DELIVERY 	= new ArtefactType("DELIVERY");
	public static final ArtefactType SYSDBA 	= new ArtefactType("SYSDBA");
	public static final ArtefactType SQLLOAD	= new ArtefactType("SQLLOAD");
	public static final ArtefactType MYSQLLOAD	= new ArtefactType("MYSQLLOAD");
	public static final ArtefactType RESOURCE	= new ArtefactType("RESOURCE");
	
	public static ArtefactType[] values() {
		return new ArtefactType[] {SQL,DELIVERY,SYSDBA,SQLLOAD,MYSQLLOAD,RESOURCE};
	}
	private String name;
	public String name() {
		return name;
	}
	private ArtefactType(String name) {
		this.name = name;
	}
	public static ArtefactType valueOf(String name) {
		ArtefactType[] types = ArtefactType.values();
		for(int i = 0 ; i<types.length ; i++) {
			if(types[i].name().equals(name)) {
				return types[i];
			}
		}
		throw new IllegalArgumentException("Unknown artefact type");
	}
	
}
