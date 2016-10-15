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

import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.datadesigner.dbgm.model.LoadingMethod;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.impl.MergerWithChildCollections;
import com.nextep.datadesigner.vcs.impl.RepositoryFile;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.model.IDataDelta;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IRepositoryFile;

/**
 * @author Christophe Fondacci
 */
public class DataSetMerger extends MergerWithChildCollections {

	public static final String CATEGORY_COLUMNS = "Dataset columns";
	public static final String CATEGORY_CONTENTS = "Dataset contents";
	public static final String ATTR_TABLE = "Related table";
	public static final String ATTR_FILEGEN = "File generated";
	public static final String ATTR_LOAD_METHOD = "Loading method";
	public static final String ATTR_TERMINATION = "Terminated by";
	public static final String ATTR_ENCLOSURE = "Enclosed by";
	public static final String ATTR_ENCLOS_OPTIONAL = "Optionally";
	public static final String CATEG_DATAFILES = "Datafiles";
	public static final String ATTR_VERSION_TAG = "Version tag";

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IDataSet set = (IDataSet) target;
		// Filling name / description information
		fillName(result, set);
		if (set.getName() == null) {
			return null;
		}
		// Filling indexed table reference
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),
				getMergeStrategy().getComparisonScope());
		IReference r = (IReference) m.buildMergedObject(result.getSubItems(ATTR_TABLE).iterator()
				.next(), activity);
		set.setTableReference(r);
		// Filling columns
		List<?> columnsRefs = getMergedList(CATEGORY_COLUMNS, result, activity);
		for (Object o : columnsRefs) {
			final IReference colRef = (IReference) o;
			if (colRef != null) {
				set.addColumnRef(colRef);
			}
		}
		// Filling data lines
		// List<?> lines = getMergedList(CATEGORY_CONTENTS, result, activity);
		// for (Object o : lines) {
		// IDataLine line = (IDataLine) o;
		// set.addDataLine(line);
		// }

		// Filling file generation attributes
		set.setFileGenerated(Boolean.parseBoolean(getStringProposal(ATTR_FILEGEN, result)));
		set.setLoadingMethod(LoadingMethod.valueOf(getStringProposal(ATTR_LOAD_METHOD, result)));
		set.setFieldsTermination(getStringProposal(ATTR_TERMINATION, result));
		set.setFieldsEnclosure(getStringProposal(ATTR_ENCLOSURE, result));
		set.setOptionalEnclosure(Boolean.parseBoolean(getStringProposal(ATTR_ENCLOS_OPTIONAL,
				result)));
		// Merging datafiles
		List<?> files = getMergedList(CATEG_DATAFILES, result, activity);
		for (Object o : files) {
			final IRepositoryFile f = (IRepositoryFile) o;
			// We don't merge repository files, so we must load them from their ID (only thing to
			// merge actually)
			IRepositoryFile repoFile = (IRepositoryFile) CorePlugin.getIdentifiableDao().load(
					RepositoryFile.class, f.getUID());
			set.addDataFile(repoFile);
		}
		// Saving set
		save(set);
		// Returning filled and saved object
		return set;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IDataSet sourceSet = (IDataSet) source;
		IDataSet targetSet = (IDataSet) target;
		DataSetComparisonItem result = new DataSetComparisonItem(sourceSet, targetSet,
				getMergeStrategy().getComparisonScope());
		result.addSubItem(new ComparisonAttribute(ATTR_NAME, source == null ? null
				: notNull(sourceSet.getName()), target == null ? null
				: notNull(targetSet.getName()), ComparisonScope.REPOSITORY));
		result.addSubItem(new ComparisonAttribute(ATTR_DESC, source == null ? null
				: notNull(sourceSet.getDescription()), target == null ? null : notNull(targetSet
				.getDescription()), ComparisonScope.REPOSITORY));
		// Adding table reference attribute
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),
				getMergeStrategy().getComparisonScope());
		if (getMergeStrategy().getComparisonScope() == ComparisonScope.REPOSITORY) {
			result.addSubItem(ATTR_TABLE, m.compare(
					source == null ? null : sourceSet.getTableReference(), target == null ? null
							: targetSet.getTableReference()));
		}
		// Comparing structure
		listCompare(CATEGORY_COLUMNS, result, sourceSet == null ? Collections.EMPTY_LIST
				: sourceSet.getColumnsRef(),
				targetSet == null ? Collections.EMPTY_LIST : targetSet.getColumnsRef(), true);
		// Comparing contents
		final IDataService dataService = DbgmPlugin.getService(IDataService.class);
		// Optionally loading sets
		if (sourceSet != null) {
			dataService.loadDataLinesFromRepository(sourceSet, new NullProgressMonitor());
		}
		if (targetSet != null) {
			dataService.loadDataLinesFromRepository(targetSet, new NullProgressMonitor());
		}
		final IDataDelta delta = dataService.computeDataSetDelta(sourceSet, targetSet);
		result.setDataDelta(delta);
		if (delta != null) {
			DifferenceType deltaDiff = delta.getDifferenceType();
			if (deltaDiff != DifferenceType.EQUALS) {
				result.setDifferenceType(deltaDiff == result.getDifferenceType() ? deltaDiff
						: DifferenceType.DIFFER);
				// For compatibility with some assertions in synchronization (typically that when
				// all sub items have their target element set as the merge proposal, we get a
				// target proposal), we need to simulate a new comparison child item which
				// represents
				// the "lines" item
				result.addSubItem(new ComparisonResult(delta, null, getMergeStrategy()
						.getComparisonScope()));
			}
		}
		// listCompare(CATEGORY_CONTENTS, result, sourceSet == null ? Collections.EMPTY_LIST
		// : sourceSet.getDataLines(),
		// targetSet == null ? Collections.EMPTY_LIST : targetSet.getDataLines());

		// File generation
		result.addSubItem(new ComparisonAttribute(ATTR_FILEGEN, sourceSet == null ? null : String
				.valueOf(sourceSet.isFileGenerated()), targetSet == null ? null : String
				.valueOf(targetSet.isFileGenerated()), ComparisonScope.REPOSITORY));
		result.addSubItem(new ComparisonAttribute(ATTR_LOAD_METHOD, sourceSet == null ? null
				: sourceSet.getLoadingMethod().name(), targetSet == null ? null : targetSet
				.getLoadingMethod().name(), ComparisonScope.REPOSITORY));
		result.addSubItem(new ComparisonAttribute(ATTR_TERMINATION, sourceSet == null ? null
				: sourceSet.getFieldsTermination(), targetSet == null ? null : targetSet
				.getFieldsTermination(), ComparisonScope.REPOSITORY));
		result.addSubItem(new ComparisonAttribute(ATTR_ENCLOSURE, sourceSet == null ? null
				: sourceSet.getFieldsEnclosure(), targetSet == null ? null : targetSet
				.getFieldsEnclosure(), ComparisonScope.REPOSITORY));
		result.addSubItem(new ComparisonAttribute(ATTR_ENCLOS_OPTIONAL, sourceSet == null ? null
				: String.valueOf(sourceSet.isOptionalEnclosure()), targetSet == null ? null
				: String.valueOf(targetSet.isOptionalEnclosure()), ComparisonScope.REPOSITORY));
		// Datafile comparison
		listCompare(CATEG_DATAFILES, result,
				sourceSet == null ? Collections.EMPTY_LIST : sourceSet.getDataFiles(),
				targetSet == null ? Collections.EMPTY_LIST : targetSet.getDataFiles());
		return result;
	}

}
