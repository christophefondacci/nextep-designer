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
package com.nextep.designer.data.ui.editors;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.data.ui.DataUiMessages;
import com.nextep.designer.dbgm.mergers.DataSetComparisonItem;
import com.nextep.designer.dbgm.model.IDataDelta;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.ui.compare.IComparisonEditorProvider;

public class DataSetComparisonEditorProvider implements IComparisonEditorProvider {

	@Override
	public IEditorInput getEditorInput(IComparisonItem comparisonItem) {
		// switch (comparisonItem.getDifferenceType()) {
		// case MISSING_SOURCE:
		// return new TypedEditorInput((ITypedObject) comparisonItem.getTarget());
		// case MISSING_TARGET:
		// case EQUALS:
		// return new TypedEditorInput((ITypedObject) comparisonItem.getSource());
		// case DIFFER:
		if (comparisonItem instanceof DataSetComparisonItem) {
			final DataSetComparisonItem item = (DataSetComparisonItem) comparisonItem;
			// final IDataDelta delta =
			// DbgmPlugin.getService(IDataService.class).computeDataSetDelta(
			// (IDataSet) item.getSource(), (IDataSet) item.getTarget());
			final IDataDelta delta = item.getDataDelta();
			return new DataDeltaEditorInput((IDataSet) comparisonItem.getSource(), delta);
		} else {
			switch (comparisonItem.getDifferenceType()) {
			case MISSING_SOURCE:
				return new TypedEditorInput((ITypedObject) comparisonItem.getTarget());
			case MISSING_TARGET:
			case EQUALS:
				return new TypedEditorInput((ITypedObject) comparisonItem.getSource());
			}
		}
		throw new ErrorException(DataUiMessages.getString("comparison.provider.invalidComparison")); //$NON-NLS-1$
	}

	@Override
	public String getEditorId(IComparisonItem comparisonItem) {
		// switch (comparisonItem.getDifferenceType()) {
		// case MISSING_SOURCE:
		// case MISSING_TARGET:
		// case EQUALS:
		// return DataSetComparisonEditor.EDITOR_ID;
		// case DIFFER:
		if (comparisonItem instanceof DataSetComparisonItem) {
			return DataSetComparisonMultiEditor.EDITOR_ID;
		} else {
			return DataSetComparisonEditor.EDITOR_ID;
		}
		// }
		// throw new ErrorException("Cannot display comparison because of invalid comparison data");
	}

	@Override
	public Image getIcon() {
		return DBGMImages.ICON_DATASET;
	}

	@Override
	public String getLabel() {
		return DataUiMessages.getString("comparison.provider.label"); //$NON-NLS-1$
	}

}
