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
package com.nextep.designer.sqlgen.ui.dbgm;

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
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.gui.editors.IAnnotatedInput;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.gui.editors.ISubmitable;
import com.nextep.datadesigner.dbgm.gui.editors.SQLComparisonEditorInput;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.gui.rcp.IComparisonItemEditorInput;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.mergers.TriggerMerger;
import com.nextep.designer.dbgm.services.IParsingService;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * @author Christophe Fondacci
 */
public class TriggerEditorInput implements ISQLEditorInput<ITrigger>, ISubmitable, IEventListener,
		IAnnotatedInput {

	private ITrigger model;

	public TriggerEditorInput(ITrigger trigger) {
		this.model = trigger;
		if (trigger != null) {
			Designer.getListenerService().registerListener(this, this.model, this);
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#getSql()
	 */
	@Override
	public String getSql() {
		return getModel().getSql();
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#save(org.eclipse.ui.texteditor.IDocumentProvider)
	 */
	@Override
	public void save(IDocumentProvider provider) {
		CorePlugin.getIdentifiableDao().save(getModel());
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#setSql(java.lang.String)
	 */
	@Override
	public void setSql(String sql) {
		getModel().setSourceCode(adjustName(sql));
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	@Override
	public boolean exists() {
		return false;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageFactory.getImageDescriptor(getModel().getType().getIcon());
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	@Override
	public String getName() {
		return getModel().getName();
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return getModel().getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IComparisonItemEditorInput.class) {
			return new SQLComparisonEditorInput(this, TriggerMerger.ATTR_SQL);
		}
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.model.IModelOriented#getModel()
	 */
	@Override
	public ITrigger getModel() {
		return model;
	}

	/**
	 * @see com.nextep.datadesigner.model.IModelOriented#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(ITrigger model) {
		this.model = model;

	}

	@Override
	public String getDatabaseType() {
		return "TRIGGER";
	}

	@Override
	public boolean showSubmit() {
		return false;
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TriggerEditorInput) {
			return getModel() == ((TriggerEditorInput) obj).getModel();
		}
		return false;
	}

	private String adjustName(String sql) {
		final IParsingService parsingService = getParsingService();

		// Parsing name
		if (model.isCustom()) {
			String parsedName = parsingService.parseName(
					IElementType.getInstance(ITrigger.TYPE_ID), sql);
			String renamedSql = sql;
			if (parsedName != null && !parsedName.equalsIgnoreCase(getName())) {
				boolean confirmed = MessageDialog.openQuestion(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						SQLMessages.getString("rename.trigger.confirmTitle"), //$NON-NLS-1$
						SQLMessages.getString("rename.trigger.confirmMsg")); //$NON-NLS-1$
				if (confirmed) {
					// Renaming element
					model.setName(parsedName);
					// Renaming SQL which will be returned by this method
					renamedSql = parsingService.getRenamedSql(
							IElementType.getInstance(IProcedure.TYPE_ID), sql, parsedName);
				} else {
					// If user did not confirm, we restore original name on the BODY
					renamedSql = parsingService.getRenamedSql(
							IElementType.getInstance(IProcedure.TYPE_ID), sql, model.getName());
				}
			}
			return renamedSql;
		} else {
			return sql;
		}
	}

	private IParsingService getParsingService() {
		return DbgmPlugin.getService(IParsingService.class);
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
}
