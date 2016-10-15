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
/**
 *
 */
package com.nextep.datadesigner.gui.model;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.model.ICheckedObject;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;
import com.nextep.designer.ui.model.IValidatableUI;

/**
 * A wizard display connector is a wizard page implementing a neXtep UI connector.
 * 
 * @author Christophe Fondacci
 * @deprecated This base class should no longer be used as it ties implementation with a
 *             {@link WizardPage}. The preferred way is to implement the {@link ITitleAreaComponent}
 *             interface and to let the container be a wizard connector depending on the context.
 */
@Deprecated
public abstract class WizardDisplayConnector extends WizardPage implements IDisplayConnector,
		IValidatableUI, ITitleAreaComponent {

	private Collection<IDisplayConnector> connectors = new ArrayList<IDisplayConnector>();
	private boolean initialized = false;
	private IUIComponentContainer container;
	private Image image;
	private String description;

	public WizardDisplayConnector(String name, String title, ImageDescriptor image) {
		super(name, title, image);
		if (image != null) {
			this.image = image.createImage();
		} else {
			this.image = null;
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#addConnector(java.lang.Object)
	 */
	@Override
	public void addConnector(IDisplayConnector child) {
		connectors.add(child);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#focus(com.nextep.datadesigner.gui.model.IDisplayConnector)
	 */
	@Override
	public void focus(IDisplayConnector childFocus) {
		getSWTConnector().setFocus();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return ImageFactory.ICON_BLANK;
	}

	/**
	 * This implementation does nothing
	 * 
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();
		// setErrorMessage(getErrorMessage());
		checkModel();
	}

	/**
	 * This implementation does nothing
	 * 
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#initialize()
	 */
	@Override
	public void initialize() {
		initialized = true;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#isInitialized()
	 */
	@Override
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#removeConnector(java.lang.Object)
	 */
	@Override
	public void removeConnector(IDisplayConnector child) {
		connectors.remove(child);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectors()
	 */
	@Override
	public Collection<IDisplayConnector> getConnectors() {
		return connectors;
	}

	/**
	 * This implementation does nothing
	 * 
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	@Override
	public void widgetDisposed(DisposeEvent e) {
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		setControl(create(parent));
		checkModel();
		refreshConnector();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#releaseConnector()
	 */
	@Override
	public void releaseConnector() {
		// TODO Auto-generated method stub

	}

	protected void addNoMarginLayout(Composite c, int numColumns, boolean equalWidth) {
		GridLayout layout = new GridLayout();
		layout.marginBottom = layout.marginHeight = layout.marginLeft = layout.marginRight = layout.marginTop = layout.marginWidth = 0;
		layout.numColumns = numColumns;
		layout.makeColumnsEqualWidth = equalWidth;
		c.setLayout(layout);
	}

	public void setModel(Object model) {
	}

	/**
	 * Creates a new connector in the parent folder
	 * 
	 * @param parent the parent SWT container
	 * @param connectedObject the object which will be displayed in this connector
	 */
	public abstract Control createSWTControl(Composite parent);

	/**
	 * Creates a new connector in the parent folder
	 * 
	 * @param parent the parent SWT container
	 * @param connectedObject the object which will be displayed in this connector
	 */
	public Control create(Composite parent) {
		Control c = createSWTControl(parent);
		initialize();
		c.addDisposeListener(this);
		Designer.getListenerService().activateListeners(this);
		return c;
	}

	protected String strVal(Object o) {
		return o == null ? "" : notNull(o.toString());
	}

	protected String notNull(String str) {
		return str == null ? "" : str;
	}

	/**
	 * Convenience method that adds a grid layout which has a 0-sized horizontal and vertical margin
	 * to the specified composite.
	 * 
	 * @param c composite to setup layout
	 * @param numColumns number of columns of the grid layout
	 */
	protected void addNoMarginLayout(Composite c, int numColumns) {
		GridLayout layout = new GridLayout();
		layout.marginBottom = layout.marginHeight = layout.marginLeft = layout.marginRight = layout.marginTop = layout.marginWidth = 0;
		layout.numColumns = numColumns;
		c.setLayout(layout);
	}

	/**
	 * Checks the underlying model. Should generally not be extended.
	 */
	protected void checkModel() {
		try {
			if (getModel() instanceof ICheckedObject) {
				((ICheckedObject) getModel()).checkConsistency();
			}
			IUIComponentContainer container = getUIComponentContainer();
			if (container != null) {
				container.setErrorMessage(null);
			}
			setErrorMessage(null);
			setPageComplete(true);

		} catch (InconsistentObjectException e) {
			IUIComponentContainer container = getUIComponentContainer();
			if (container != null) {
				container.setErrorMessage(e.getReason());
			}
			setErrorMessage(e.getReason());
			setPageComplete(false);
		}
	}

	/**
	 * {@inheritDoc}<br>
	 * <br>
	 * This default implementation does nothing
	 */
	@Override
	public void cancel() {
	}

	/**
	 * {@inheritDoc}<br>
	 * <br>
	 * This default implementation does nothing
	 */
	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public IUIComponentContainer getUIComponentContainer() {
		return container;
	}

	@Override
	public void setUIComponentContainer(IUIComponentContainer container) {
		this.container = container;
	}

	@Override
	public Image getImage() {
		return image;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setMessage(String newMessage) {
		super.setMessage(newMessage);
		this.description = newMessage;
	}

	@Override
	public String getAreaTitle() {
		return getTitle();
	}

	@Override
	public void dispose() {
		if (image != null) {
			image.dispose();
		}
		super.dispose();
	}
}
