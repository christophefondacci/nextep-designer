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
package com.nextep.datadesigner.sqlgen.model;

/**
 * Defines a SQL script type. Script types allows to categorize SQL script according to their
 * content. Script types also define a corresponding file extension.
 * 
 * @author Christophe Fondacci
 */
public enum ScriptType {
	DROP("DRP", ".drp"), //$NON-NLS-1$ //$NON-NLS-2$
	SEQ("SEQ", ".seq"), //$NON-NLS-1$ //$NON-NLS-2$
	CUSTOM("CST", ".sql"), //$NON-NLS-1$ //$NON-NLS-2$
	WRAPPER("WRP", ".sql"), //$NON-NLS-1$ //$NON-NLS-2$
	TYPE("TYP", ".typ"), //$NON-NLS-1$ //$NON-NLS-2$
	TABLE("TBL", ".tab"), //$NON-NLS-1$ //$NON-NLS-2$
	CLUSTER_INDEX("CLI", ".cli"), //$NON-NLS-1$ //$NON-NLS-2$
	INDEX("IND", ".ind"), //$NON-NLS-1$ //$NON-NLS-2$
	PKCONSTRAINT("PK", ".pk"), //$NON-NLS-1$ //$NON-NLS-2$
	MVIEW_LOG("MVL", ".mvl"), //$NON-NLS-1$ //$NON-NLS-2$
	MAT_VIEW("MVW", ".mvw"), //$NON-NLS-1$ //$NON-NLS-2$
	FKCONSTRAINT("FK", ".fk"), //$NON-NLS-1$ //$NON-NLS-2$
	CHECKCONS("CHK", ".chk"), //$NON-NLS-1$ //$NON-NLS-2$
	PACKAGE_SPEC("PKS", ".pks"), //$NON-NLS-1$ //$NON-NLS-2$
	VIEW("VW", ".vw"), //$NON-NLS-1$ //$NON-NLS-2$
	JAVA("JAV", ".jav"), //$NON-NLS-1$ //$NON-NLS-2$
	PROC("PRC", ".prc"), //$NON-NLS-1$ //$NON-NLS-2$
	PACKAGE_BODY("PKB", ".pkb"), //$NON-NLS-1$ //$NON-NLS-2$
	TYPE_BODY("TYB", ".tyb"), //$NON-NLS-1$ //$NON-NLS-2$
	TRIGGER("TRG", ".trg"), //$NON-NLS-1$ //$NON-NLS-2$
	COMMENTS("COM", ".com"), //$NON-NLS-1$ //$NON-NLS-2$
	DATA("DAT", ".sql"), //$NON-NLS-1$ //$NON-NLS-2$
	DATADEL("DEL", ".sql"), //$NON-NLS-1$ //$NON-NLS-2$
	SYNONYM("SYN", ".syn"); //$NON-NLS-1$ //$NON-NLS-2$

	private String fileExtension;
	private String code;

	ScriptType(String code, String extension) {
		this.fileExtension = extension;
		this.code = code;
	}

	/**
	 * @return the file extension of this script type
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * The code used to reference this type. Mainly used for database persistence.
	 * 
	 * @return the type code
	 */
	public String getCode() {
		return code;
	}
}
