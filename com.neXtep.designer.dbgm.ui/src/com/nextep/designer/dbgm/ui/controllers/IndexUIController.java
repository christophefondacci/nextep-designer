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
package com.nextep.designer.dbgm.ui.controllers;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.dbgm.gui.IndexEditorGUI;
import com.nextep.datadesigner.dbgm.gui.navigators.IndexNavigator;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.gui.external.VersionableController;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;
import com.nextep.designer.ui.model.ITypedFormPage;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class IndexUIController extends VersionableController {

	private static final Log log = LogFactory.getLog(IndexUIController.class);

	public IndexUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.COLUMN_ADDED);
		addSaveEvent(ChangeEvent.COLUMN_REMOVED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new IndexEditorGUI((IIndex) content, this);
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new IndexNavigator((IIndex) model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		return null;
	}

	@Override
	public Object newInstance(Object parent) {
		// Checking if parent is modifiable
		IVersionable<IIndex> versionable = VersionableFactory.createVersionable(IIndex.class);

		IIndex index = versionable.getVersionnedObject().getModel();
		IVersionContainer container = null;
		IBasicTable indexedTable = null;
		// If we try to add an index on a container, we first locate the table to index
		if (parent instanceof IVersionContainer) {
			container = (IVersionContainer) parent;
			// Prompting the user for a table to index, since an index must be defined on a table
			ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
			indexedTable = (IBasicTable) uiService.findElement(Display.getDefault()
					.getActiveShell(), DBGMUIMessages
					.getString("controller.index.selectTableTitle"), IElementType //$NON-NLS-1$
					.getInstance(IBasicTable.TYPE_ID));
			// When user has not selected a table, he cancelled the whole index creation operation
			if (indexedTable == null) {
				throw new CancelException();
			}
		} else if (parent instanceof IBasicTable) {
			// Our parent is the table to index
			indexedTable = (IBasicTable) parent;
			container = VersionHelper.getVersionable(indexedTable).getContainer();
		} else {
			throw new ErrorException(
					DBGMUIMessages.getString("controller.index.parentNotFoundError")); //$NON-NLS-1$
		}

		// Setuping the index
		versionable.setContainer(container);
		index.setIndexedTableRef(indexedTable.getReference());
		final String name = (indexedTable.getShortName() != null
				&& !"".equals(indexedTable.getShortName()) ? indexedTable //$NON-NLS-1$
				.getShortName() : indexedTable.getName())
				+ "_I"; //$NON-NLS-1$
		index.setName(getAvailableName(getType(), name));
		// Rechecking with index name
		index.setName(getAvailableName(getType(), index.getName()));

		// We have a consistent index here
		save(index);
		container.addVersionable(versionable, new ImportPolicyAddOnly());
		if (parent instanceof IBasicTable) {
			((IBasicTable) parent).addIndex(index);
		} else {
			try {
				index.getIndexedTable().addIndex(index);
			} catch (ErrorException e) {
				// log.warn("Index may not be linked proper")
			}
		}

		return index;
	}

	@Override
	public Object emptyInstance(String name, Object parent) {
		return newInstance(parent);

	}

	@Override
	protected IVersionContainer emptyInstanceGetContainer(Object parent) {
		final IBasicTable t = (IBasicTable) parent;
		return VersionHelper.getVersionable(t).getContainer();
	}

	@Override
	protected void beforeCreation(IVersionable<?> v, Object parent) {
		IIndex i = (IIndex) v.getVersionnedObject().getModel();
		i.setIndexedTableRef(((IBasicTable) parent).getReference());
	}

	@Override
	public void save(IdentifiedObject o) {
		super.save(o);
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
		final List<ITypedFormPage> pages = uiService.createContributedPagesFor(
				IElementType.getInstance(IIndex.INDEX_TYPE), null, null);
		if (pages.size() <= 1) {
			return new TypedEditorInput(((IIndex) model).getIndexedTable());
		} else {
			return new TypedEditorInput(model);
		}
	}

	@Override
	public String getEditorId() {
		return TypedFormRCPEditor.EDITOR_ID;
	}
}
