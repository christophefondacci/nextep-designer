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
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.gui.editors.IAnnotatedInput;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.gui.editors.ISubmitable;
import com.nextep.datadesigner.dbgm.gui.editors.SQLComparisonEditorInput;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.impl.merge.PackageMerger;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.vcs.gui.rcp.IComparisonItemEditorInput;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.services.IParsingService;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * @author Christophe Fondacci
 */
public class PackageEditorInput implements ISQLEditorInput<IPackage>, IEventListener, ISubmitable,
		IAnnotatedInput {

	// private static final Log log = LogFactory.getLog(PackageEditorInput.class);
	private IPackage pkg;

	public PackageEditorInput(IPackage pkg) {
		this.pkg = pkg;
		if (pkg != null) {
			Designer.getListenerService().registerListener(this, pkg, this);
		}
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	@Override
	public boolean exists() {
		return true;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageFactory.getImageDescriptor(pkg.getType().getIcon());
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	@Override
	public String getName() {
		return pkg.getName();
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
		return getName();
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IComparisonItemEditorInput.class) {
			SQLComparisonEditorInput input = new SQLComparisonEditorInput(this,
					PackageMerger.ATTR_BODY);
			return input;
		}
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#getModel()
	 */
	@Override
	public IPackage getModel() {
		return pkg;
	}

	/**
	 * @see com.nextep.datadesigner.model.IModelOriented#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(IPackage model) {
		this.pkg = model;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#getSql()
	 */
	@Override
	public String getSql() {
		return pkg.getBodySourceCode();
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#save()
	 */
	@Override
	public void save(final IDocumentProvider provider) {
		ControllerFactory.getController(pkg.getType()).save(pkg);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#setSql(java.lang.String)
	 */
	@Override
	public void setSql(String sql) {
		String namedSql = adjustName(sql);
		pkg.setBodySourceCode(namedSql);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDatabaseType() {
		return "PACKAGE BODY"; //$NON-NLS-1$
	}

	@Override
	public boolean showSubmit() {
		return true;
	}

	@Override
	public Map<Annotation, Position> getAnnotationMap(IDocument doc) {
		return SQLEditorUIServices.getInstance().getCompilationMarkersFor(getModel(), doc,
				getDatabaseType());
	}

	@Override
	public Collection<String> getAnnotationTypes() {
		return Arrays.asList("org.eclipse.ui.workbench.texteditor.error", //$NON-NLS-1$
				"org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
	}

	private String adjustName(String body) {
		final IParsingService parsingService = getParsingService();

		// Parsing name
		String parsedName = parsingService.parseName(IElementType.getInstance(IPackage.TYPE_ID),
				body);
		String renamedBody = body;
		if (parsedName != null && !parsedName.equalsIgnoreCase(getName())) {
			boolean confirmed = MessageDialog.openQuestion(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					SQLMessages.getString("rename.package.confirmTitle"), //$NON-NLS-1$
					SQLMessages.getString("rename.package.confirmMsg")); //$NON-NLS-1$
			if (confirmed) {
				// Renaming element
				pkg.setName(parsedName);
				// Renaming SPEC only (because BODY will be updated by the returned body string
				String newSpec = parsingService.getRenamedSql(
						IElementType.getInstance(IPackage.TYPE_ID), pkg.getSpecSourceCode(),
						parsedName);
				pkg.setSpecSourceCode(newSpec);
				// Renaming body
				renamedBody = parsingService.getRenamedSql(
						IElementType.getInstance(IPackage.TYPE_ID), body, parsedName);
			} else {
				// If user did not confirm, we restore original name on the BODY
				renamedBody = parsingService.getRenamedSql(
						IElementType.getInstance(IPackage.TYPE_ID), body, pkg.getName());
			}
		}
		return renamedBody;
	}

	private IParsingService getParsingService() {
		return DbgmPlugin.getService(IParsingService.class);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof PackageEditorInput
				&& getModel() == ((IModelOriented<?>) obj).getModel();
	}

	@Override
	public int hashCode() {
		return getModel().hashCode();
	}
}
