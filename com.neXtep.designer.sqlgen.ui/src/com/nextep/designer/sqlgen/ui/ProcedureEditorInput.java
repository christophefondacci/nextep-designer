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
package com.nextep.designer.sqlgen.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import com.nextep.datadesigner.dbgm.gui.editors.IAnnotatedInput;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.gui.editors.ISubmitable;
import com.nextep.datadesigner.dbgm.gui.editors.SQLComparisonEditorInput;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.impl.merge.ProcedureMerger;
import com.nextep.datadesigner.vcs.gui.rcp.IComparisonItemEditorInput;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.services.IParsingService;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;
import com.nextep.designer.ui.factories.ImageFactory;

public class ProcedureEditorInput implements ISQLEditorInput<IProcedure>, IEventListener,
		ISubmitable, IAnnotatedInput {

	private IProcedure procedure;

	// private static final Log log = LogFactory.getLog(ProcedureEditorInput.class);
	public ProcedureEditorInput(IProcedure proc) {
		setModel(proc);
	}

	@Override
	public String getSql() {
		return getModel().getSQLSource();
	}

	@Override
	public void save(IDocumentProvider provider) {
		ControllerFactory.getController(IElementType.getInstance(IProcedure.TYPE_ID)).save(
				getModel());

	}

	@Override
	public void setSql(String sql) {

		getModel().setSQLSource(adjustName(sql));
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
		return "Procedure " + getName(); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IComparisonItemEditorInput.class) {
			return new SQLComparisonEditorInput(this, ProcedureMerger.ATTR_SOURCE);
		}
		return null;
	}

	@Override
	public IProcedure getModel() {
		return procedure;
	}

	@Override
	public void setModel(IProcedure model) {
		this.procedure = model;
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDatabaseType() {
		return getModel().getLanguageType() == LanguageType.JAVA ? "JAVA SOURCE" : "PROCEDURE"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean showSubmit() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProcedureEditorInput) {
			return ((ProcedureEditorInput) obj).getModel() == getModel();
		}
		return false;
	}

	@Override
	public Map<Annotation, Position> getAnnotationMap(IDocument doc) {
		return SQLEditorUIServices.getInstance().getCompilationMarkersFor(getModel(), doc);
	}

	@Override
	public Collection<String> getAnnotationTypes() {
		return Arrays.asList("org.eclipse.ui.workbench.texteditor.error", //$NON-NLS-1$
				"org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
	}

	private String adjustName(String sql) {
		final IParsingService parsingService = getParsingService();

		// Parsing name
		String parsedName = parsingService.parseName(IElementType.getInstance(IProcedure.TYPE_ID),
				sql);
		String renamedSql = sql;
		if (parsedName != null && !parsedName.equalsIgnoreCase(getName())) {
			boolean confirmed = MessageDialog.openQuestion(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					SQLMessages.getString("rename.proc.confirmTitle"), //$NON-NLS-1$
					SQLMessages.getString("rename.proc.confirmMsg")); //$NON-NLS-1$
			if (confirmed) {
				// Renaming element
				procedure.setName(parsedName);
				// Renaming SQL which will be returned by this method
				renamedSql = parsingService.getRenamedSql(
						IElementType.getInstance(IProcedure.TYPE_ID), sql, parsedName);
			} else {
				// If user did not confirm, we restore original name on the BODY
				renamedSql = parsingService.getRenamedSql(
						IElementType.getInstance(IProcedure.TYPE_ID), sql, procedure.getName());
			}
		}
		return renamedSql;
	}

	private IParsingService getParsingService() {
		return DbgmPlugin.getService(IParsingService.class);
	}
}
