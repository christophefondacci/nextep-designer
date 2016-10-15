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
package com.nextep.designer.sqlgen.ui.impl;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.ui.SQLGenImages;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.ui.compare.AbstractComparisonEditorProvider;

public class SqlComparisonEditorProvider extends AbstractComparisonEditorProvider {

	@Override
	public String getEditorId(IComparisonItem compItem, ComparedElement comparedElement) {
		return "com.neXtep.designer.sqlgen.ui.SQLEditor";
	}

	@Override
	public IEditorInput getEditorInput(IComparisonItem comparisonItem,
			ComparedElement comparedElement) {
		final ITypedObject current = getComparedElement(comparisonItem, comparedElement);
		final ITypedObject other = getComparedElement(comparisonItem,
				comparedElement == ComparedElement.SOURCE ? ComparedElement.TARGET
						: ComparedElement.SOURCE);
		ISQLGenerator generator = GeneratorFactory.getGenerator(current,
				DBGMHelper.getCurrentVendor());
		if (generator != null && !(current instanceof IReference)) {
			// Generating the SQL for item to display
			IGenerationResult result = generator.generateFullSQL(current);

			// We append everything in a root script for proper naming and optionally unwrapping
			// SQL wrappers
			ISQLScript sql = new SQLScript(ScriptType.CUSTOM);
			sql.setName(comparedElement.isSource() ? ((INamedObject) comparisonItem.getSource())
					.getName() : ((INamedObject) comparisonItem.getTarget()).getName());
			if (result != null) {
				for (ISQLScript s : result.buildScript()) {
					sql.appendScript(s);
				}
			}

			IComparisonItem sqlComparison = null;
			ISQLScript otherSql = new SQLScript(ScriptType.CUSTOM);
			// Generating the SQL of the other item to compute comparison information
			if (other != null) {
				IGenerationResult otherResult = generator.generateFullSQL(other);
				if (otherResult != null) {
					for (ISQLScript s : otherResult.buildScript()) {
						otherSql.appendScript(s);
					}
				}
			}
			// Getting comparator for SQL scripts
			IMerger m = MergerFactory.getMerger(IElementType.getInstance(ISQLScript.TYPE_ID),
					ComparisonScope.DATABASE);
			// Comparing
			sqlComparison = m.compare(comparedElement == ComparedElement.SOURCE ? sql : otherSql,
					comparedElement == ComparedElement.SOURCE ? otherSql : sql);
			IEditorInput sqlInput = UIControllerFactory.getController(
					IElementType.getInstance(ISQLScript.TYPE_ID)).getEditorInput(sql);
			return adapt(sqlInput, sqlComparison == null ? null : sqlComparison, comparedElement);
		}

		return null;
	}

	@Override
	public Image getIcon() {
		return SQLGenImages.ICON_SQL_TINY;
	}

	@Override
	public String getLabel() {
		return SQLMessages.getString("comparisonProvider.label.sql");
	}
}
