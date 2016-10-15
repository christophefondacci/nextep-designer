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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.texteditor.IDocumentProvider;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.gui.editors.ISubmitable;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * @author Christophe Fondacci
 */
public class ViewEditorInput implements ISQLEditorInput<IView>, IEventListener, ISubmitable {

	private IView model;

	public ViewEditorInput(IView view) {
		this.model = view;
		if (view != null) {
			Designer.getListenerService().registerListener(this, this.model, this);
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput#getSql()
	 */
	@Override
	public String getSql() {
		return getModel().getSQLDefinition();
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
		getModel().setSQLDefinition(sql);
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageFactory.getImageDescriptor(IElementType.getInstance(IView.TYPE_ID).getIcon());
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	@Override
	public String getName() {
		return getModel() != null ? getModel().getName() : "[None]";
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
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
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.model.IModelOriented#getModel()
	 */
	@Override
	public IView getModel() {
		return model;
	}

	/**
	 * @see com.nextep.datadesigner.model.IModelOriented#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(IView model) {
		this.model = model;
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ViewEditorInput) {
			return getModel() == ((ViewEditorInput) obj).getModel();
		}
		return false;
	}

	@Override
	public String getDatabaseType() {
		return "VIEW";
	}

	@Override
	public boolean showSubmit() {
		return true;
	}
}
