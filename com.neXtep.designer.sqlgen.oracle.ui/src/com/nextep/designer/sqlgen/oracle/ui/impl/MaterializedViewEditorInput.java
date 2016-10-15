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
package com.nextep.designer.sqlgen.oracle.ui.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;
import com.nextep.designer.dbgm.oracle.model.MaterializedViewType;
import com.nextep.designer.dbgm.oracle.model.RefreshMethod;
import com.nextep.designer.sqlgen.oracle.ui.SQLOraUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IVersionable;

public class MaterializedViewEditorInput implements ISQLEditorInput<IMaterializedView> {

	private IMaterializedView view;

	public MaterializedViewEditorInput(IMaterializedView view) {
		this.view = view;
	}

	@Override
	public String getSql() {
		return getModel().getSql();
	}

	@Override
	public void save(IDocumentProvider provider) {
		// Saving view
		CorePlugin.getIdentifiableDao().save(getModel());
		// Parsing SQL when FAST refresh method to propose materialized view log generation
		final IMaterializedView view = getModel();
		if (view.getRefreshMethod() == RefreshMethod.FAST) {
			List<IVersionable<?>> tables = VersionHelper.getAllVersionables(
					VersionHelper.getCurrentView(), IElementType.getInstance(IBasicTable.TYPE_ID));
			List<IVersionable<?>> viewLogs = VersionHelper.getAllVersionables(
					VersionHelper.getCurrentView(),
					IElementType.getInstance(IMaterializedViewLog.TYPE_ID));
			final String s = view.getSql();
			if (s == null || "".equals(s.trim())) {
				return;
			}
			List<IBasicTable> logTables = new ArrayList<IBasicTable>();
			StringBuilder logTablesStr = new StringBuilder(100);
			// Looking for tables without logs
			for (IVersionable<?> t : tables) {
				if (s.contains(t.getName())) {
					final IBasicTable tab = (IBasicTable) t.getVersionnedObject().getModel();
					boolean logTabFound = false;
					for (IVersionable<?> log : viewLogs) {
						if (tab.getReference().equals(
								((IMaterializedViewLog) log.getVersionnedObject().getModel())
										.getTableReference())) {
							logTabFound = true;
							break;
						}
					}
					if (!logTabFound) {
						logTables.add(tab);
						logTablesStr.append(tab.getName() + "\n");
					}
				}
			}
			if (logTables.isEmpty())
				return;
			// Prompting user
			boolean isOk = MessageDialog.openQuestion(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SQLOraUIMessages
					.getString("promptMaterializedViewLogCreationTitle"), MessageFormat.format(
					SQLOraUIMessages.getString("promptMaterializedViewLogCreation"),
					logTablesStr.toString()));
			if (isOk) {
				List<ICommand> cmds = new ArrayList<ICommand>();
				for (final IBasicTable t : logTables) {
					cmds.add(new ICommand() {

						@Override
						public Object execute(Object... parameters) {
							IMaterializedViewLog l = (IMaterializedViewLog) UIControllerFactory
									.getController(
											IElementType.getInstance(IMaterializedViewLog.TYPE_ID))
									.emptyInstance("", t);
							if (view.getViewType() == MaterializedViewType.PRIMARY_KEY) {
								l.setPrimaryKey(true);
							} else {
								l.setRowId(true);
							}
							return null;
						}

						@Override
						public String getName() {
							return MessageFormat.format(
									SQLOraUIMessages.getString("addingMaterializedViewLog"),
									t.getName());
						}
					});
					CommandProgress.runWithProgress(false, cmds.toArray(new ICommand[cmds.size()]));
				}
			}

		}
	}

	@Override
	public void setSql(String sql) {
		getModel().setSql(sql);
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageFactory.getImageDescriptor(getModel().getType().getIcon());
	}

	@Override
	public String getName() {
		return getModel().getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public IMaterializedView getModel() {
		return view;
	}

	@Override
	public void setModel(IMaterializedView model) {
		this.view = model;
	}

}
