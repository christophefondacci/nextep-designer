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
package com.nextep.designer.synch.model.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.ISQLCommandWriter;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class SynchronizationResult implements ISynchronizationResult {

	private Collection<? extends IReferenceable> sources;
	private Collection<? extends IReferenceable> targets;
	private Collection<IComparisonItem> comparedItems;
	private Map<IReference, IComparisonItem> itemsRefMap;
	private IConnection connection;
	private IGenerationResult generationResult;
	private ISQLScript script;
	private ComparisonScope scope;
	private boolean dirty = true;
	private boolean dataSynchronization = false;

	public SynchronizationResult(Collection<? extends IReferenceable> sources,
			Collection<? extends IReferenceable> targets, Collection<IComparisonItem> items,
			IConnection connection, ComparisonScope scope) {
		this.sources = sources;
		this.targets = targets;
		this.comparedItems = items;
		this.connection = connection;
		this.scope = scope;
		itemsRefMap = new HashMap<IReference, IComparisonItem>();
		for (IComparisonItem item : comparedItems) {
			itemsRefMap.put(item.getReference(), item);
		}
	}

	@Override
	public Collection<IComparisonItem> getComparedItems() {
		return comparedItems;
	}

	@Override
	public IConnection getConnection() {
		return connection;
	}

	@Override
	public ISQLScript getGeneratedScript() {
		if (generationResult == null) {
			return null;
		} else {
			return script;
		}
	}

	@Override
	public void setGenerationResult(IGenerationResult result) {
		this.generationResult = result;
		Collection<ISQLScript> scripts = generationResult.buildScript();
		ISQLCommandWriter writer = SQLGenPlugin.getService(IGenerationService.class)
				.getCurrentSQLCommandWriter();

		if (script == null) {
			script = CorePlugin.getTypedObjectFactory().create(ISQLScript.class);
			script.setName(generationResult.getName());
			script.setDirectory(SQLGenUtil.getPreference(PreferenceConstants.TEMP_FOLDER));
			script.setExternal(true);
		}

		// The content of the SQL script is re-initialized before generating the new contents
		script.setSql(""); //$NON-NLS-1$

		if (scripts != null) {
			for (ISQLScript s : scripts) {
				script.appendScript(s);
			}

			if (script.getSql().length() == 0) {
				script.appendSQL(writer
						.promptMessage("Your repository is synchronized with your database")); //$NON-NLS-1$
				// .appendSQL(promptPrefix)
				//						.appendSQL("or the removal of some database objects have been disabled") //$NON-NLS-1$
				// .appendSQL(ISQLGenerator.NEWLINE).appendSQL(promptPrefix)
				//						.appendSQL("please check your drop strategies") //$NON-NLS-1$
				// .appendSQL(ISQLGenerator.NEWLINE);
			}
			// Bug #449 appending exit tag at the end of every synchronization script
			script.appendSQL(writer.exit());
		}
		script.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public IGenerationResult getGenerationResult() {
		return generationResult;
	}

	@Override
	public Collection<? extends IReferenceable> getSourceElements() {
		return sources;
	}

	@Override
	public Collection<? extends IReferenceable> getTargetElements() {
		return targets;
	}

	@Override
	public ComparisonScope getComparisonScope() {
		return scope;
	}

	@Override
	public IComparisonItem getComparisonItemFor(IReference r) {
		return itemsRefMap.get(r);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public void setDataSynchronization(boolean dataSynchro) {
		this.dataSynchronization = dataSynchro;
	}

	@Override
	public boolean isDataSynchronization() {
		return dataSynchronization;
	}

}
