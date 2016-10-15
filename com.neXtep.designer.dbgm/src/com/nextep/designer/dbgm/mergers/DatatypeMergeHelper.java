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
package com.nextep.designer.dbgm.mergers;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.LengthType;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.services.IDatatypeService;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * This helper class is here to capitalize datatype merge information. Any
 * merger whih needs to compare datatype should do so by calling the static
 * methods provided here for comparing and building datatypes
 * 
 * @author Christophe Fondacci
 */
public class DatatypeMergeHelper {

	private static final Log LOGGER = LogFactory.getLog(DatatypeMergeHelper.class);
	private static final String ATTR_DATATYPE = "Datatype";
	private static final String ATTR_DATASIZE = "Datatype length";
	private static final String ATTR_DATASIZE_TYPE = "Datatype length type";
	private static final String ATTR_DATAPRECISION = "Datatype precision";
	private static final String ATTR_DATAUNSIGNED = "Datatype unsigned";

	public static void addDatatypeComparison(IComparisonItem result, IDatatype source,
			IDatatype target, ComparisonScope scope) {

		// Getting required services
		final IWorkspaceService workspaceService = CorePlugin.getService(IWorkspaceService.class);
		final IDatatypeService datatypeService = CorePlugin.getService(IDatatypeService.class);
		final IWorkspace workspace = workspaceService.getCurrentWorkspace();

		// Getting required provider
		final IDatatypeProvider p = datatypeService.getDatatypeProvider(workspace.getDBVendor());

		// Getting generation vendor (for future datatype conversion)
		final String context = Designer.getInstance().getContext();
		DBVendor generationVendor = workspace.getDBVendor();
		try {
			if (context != null) {
				generationVendor = DBVendor.valueOf(context);
			}
		} catch (IllegalArgumentException e) {
			LOGGER.error("Unknown vendor used for generation '" + context
					+ "', using workspace default : " + e.getMessage(), e);
		}

		// Converting datatype to vendor
		final IDatatype src = datatypeService.getDatatype(generationVendor, source);
		final IDatatype tgt = datatypeService.getDatatype(generationVendor, target);

		final Map<String, String> typeMap = p.getEquivalentDatatypesMap();

		if (scope == ComparisonScope.DATABASE && src != null && tgt != null
				&& src.getName().toUpperCase().startsWith("ENUM")) {
			// Special enum fix for mysql
			// TODO properly manage enums, deport mysql specific in mysql
			// plugins
			if (tgt.getName().toUpperCase().startsWith("ENUM")) {
				final String srcEnum = src.getName().substring(4);
				final String tgtEnum = tgt.getName().substring(4);
				if (srcEnum.trim().replace(" ", "").equals(tgtEnum.trim().replace(" ", ""))) {
					result.addSubItem(new ComparisonAttribute(ATTR_DATATYPE, tgt.getName(), tgt
							.getName()));
				} else {
					result.addSubItem(new ComparisonAttribute(ATTR_DATATYPE, src == null ? null
							: eqType(typeMap, src.getName()), tgt == null ? null : eqType(typeMap,
							tgt.getName())));
				}
			} else {
				result.addSubItem(new ComparisonAttribute(ATTR_DATATYPE, src == null ? null
						: eqType(typeMap, src.getName()), tgt == null ? null : eqType(typeMap,
						tgt.getName())));
			}

		} else {
			result.addSubItem(new ComparisonAttribute(ATTR_DATATYPE, src == null ? null : eqType(
					typeMap, src.getName()), tgt == null ? null : eqType(typeMap, tgt.getName())));
		}
		ComparisonScope dataLengthScope = scope;
		if (src != null && src.getName().indexOf("INT") > -1
				&& VersionHelper.getCurrentView().getDBVendor() == DBVendor.MYSQL) {
			dataLengthScope = ComparisonScope.REPOSITORY;
		}
		// Specific definition for CHAR / BYTE length handling
		Integer srcLength = src == null ? null : src.getLength();
		Integer tgtLength = null;
		if (tgt == null) {
			tgtLength = srcLength;
		} else {
			if ((src != null && src.getLengthType() == LengthType.UNDEFINED)
					|| (tgt != null && tgt.getLengthType() == LengthType.UNDEFINED)) {
				// If one of source or target is undefined, we keep the value
				tgtLength = tgt.getLength();
			} else {
				// If type of source is different than target (i.e. BYTE vs CHAR
				// or CHAR vs BYTE)
				// We swap
				if (src != null && src.getLengthType() == tgt.getLengthType()) {
					tgtLength = tgt.getLength();
				} else {
					tgtLength = tgt.getAlternateLength();
				}
			}
		}
		if ((src != null && src.getLengthType() == LengthType.CHAR)
				|| (tgt != null && tgt.getLengthType() == LengthType.CHAR)) {
			result.addSubItem(new ComparisonAttribute(ATTR_DATASIZE_TYPE, src == null ? null : src
					.getLengthType().name(), tgt == null ? null : tgt.getLengthType().name()));
		}
		if (scope == ComparisonScope.DATABASE) {
			result.addSubItem(new ComparisonAttribute(ATTR_DATASIZE, src == null ? null : String
					.valueOf(srcLength == 0 && tgt != null ? tgtLength : srcLength),
					tgt == null ? null : String.valueOf(tgtLength), dataLengthScope));
		} else if (scope == ComparisonScope.DB_TO_REPOSITORY) {
			result.addSubItem(new ComparisonAttribute(ATTR_DATASIZE, src == null ? null : String
					.valueOf(srcLength), tgt == null ? null : String.valueOf(tgtLength),
					dataLengthScope));
		} else {
			result.addSubItem(new ComparisonAttribute(ATTR_DATASIZE, src == null ? null : String
					.valueOf(src.getLength()),
					tgt == null ? null : String.valueOf(tgt.getLength()), dataLengthScope));
		}
		if (scope == ComparisonScope.DATABASE) {
			result.addSubItem(new ComparisonAttribute(ATTR_DATAPRECISION, src == null ? null
					: String.valueOf(src.getPrecision() == 0 && tgt != null ? tgt.getPrecision()
							: src.getPrecision()), tgt == null ? null : String.valueOf(tgt
					.getPrecision())));
		} else {
			result.addSubItem(new ComparisonAttribute(ATTR_DATAPRECISION, src == null ? null
					: String.valueOf(src.getPrecision()), tgt == null ? null : String.valueOf(tgt
					.getPrecision())));
		}
		result.addSubItem(new ComparisonAttribute(ATTR_DATAUNSIGNED, src == null ? null : Boolean
				.toString(src.isUnsigned()), tgt == null ? null
				: Boolean.toString(tgt.isUnsigned())));
	}

	private static String eqType(Map<String, String> eqTypeMap, String typeName) {
		String eqType = eqTypeMap.get(typeName);
		return eqType == null ? typeName : eqType;
	}

	public static IDatatype buildDataTypeFromComparison(IComparisonItem result) {
		String typeName = Merger.getStringProposalValue(ATTR_DATATYPE, result);
		int typeSize = 0;
		try {
			typeSize = Integer.valueOf(Merger.getStringProposalValue(ATTR_DATASIZE, result));
		} catch (NumberFormatException e) {
			typeSize = 0;
		}
		int typePrec = 0;
		try {
			typePrec = Integer.valueOf(Merger.getStringProposalValue(ATTR_DATAPRECISION, result));
		} catch (NumberFormatException e) {
			typePrec = 0;
		}
		LengthType lengthType = LengthType.UNDEFINED;
		final String datasizeType = Merger.getStringProposalValue(ATTR_DATASIZE_TYPE, result);
		try {
			lengthType = LengthType.valueOf(datasizeType);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Invalid length type '" + datasizeType
					+ "' returned from merge, defaulting to UNDEFINED");
		} catch (NullPointerException e) {
			LOGGER.error("Null length type returned from merge, defaulting to UNDEFINED");
		}
		IDatatype d = new Datatype(typeName, typeSize, typePrec);
		d.setUnsigned(Boolean.valueOf(Merger.getStringProposalValue(ATTR_DATAUNSIGNED, result)));
		d.setLengthType(lengthType);
		return d;
	}

}
