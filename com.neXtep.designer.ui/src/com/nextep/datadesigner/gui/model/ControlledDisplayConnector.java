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

import java.text.MessageFormat;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.UIImages;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.IUIComponentContainer;

/**
 * @author Christophe Fondacci
 */
public abstract class ControlledDisplayConnector extends
		ListeningControlledConnector<Control, IDisplayConnector> implements IDisplayConnector,
		ITitleAreaComponent {

	/** A debug snapshot used in debug mode */
	private Object observableSnap;
	/** Our initialization flag */
	private boolean initialized = false;
	private Control control;
	private IUIComponentContainer container;

	protected ControlledDisplayConnector(IObservable model, ITypedObjectUIController c) {
		super(model, c);
	}

	protected void addNoMarginLayout(Composite c, int numColumns) {
		GridLayout layout = new GridLayout();
		layout.marginBottom = layout.marginHeight = layout.marginLeft = layout.marginRight = layout.marginTop = layout.marginWidth = 0;
		layout.numColumns = numColumns;
		c.setLayout(layout);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return UIImages.WIZARD_GENERIC;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#focus(com.nextep.datadesigner.gui.model.IDisplayConnector)
	 */
	@Override
	public void focus(IDisplayConnector childFocus) {
		getSWTConnector().setFocus();
		refreshConnector();
	}

	/**
	 * Creates a new connector in the parent folder
	 * 
	 * @param parent the parent SWT container
	 * @param connectedObject the object which will be displayed in this connector
	 */
	protected abstract Control createSWTControl(Composite parent);

	/**
	 * Creates a new connector in the parent folder
	 * 
	 * @param parent the parent SWT container
	 * @param connectedObject the object which will be displayed in this connector
	 */
	public Control create(Composite parent) {
		control = createSWTControl(parent);
		initialize();
		Designer.getListenerService().activateListeners(this);
		control.addDisposeListener(this);
		if (Designer.isDebugging()) {
			observableSnap = Observable.getSnapshot();
		}

		return control;
	}

	/**
	 * The default behaviour does nothing apart setting the initialized flag. Extensions should
	 * always call this superclass method to set the correct initialization status.
	 * 
	 * @see com.nextep.datadesigner.gui.model.IConnector#initialize()
	 */
	@Override
	public void initialize() {
		this.initialized = true;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#isInitialized()
	 */
	@Override
	public final boolean isInitialized() {
		return initialized;
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	@Override
	public void widgetDisposed(DisposeEvent event) {
		super.widgetDisposed(event);
		// Debugging
		if (Designer.isDebugging()) {
			if (observableSnap != null) {
				Observable.dumpSnapshotDelta(observableSnap);
			}
		}
	}

	/**
	 * Default getTitle() implementation assuming we have a {@link INamedObject} model and
	 * displaying the model name with a " {Type} Edition" suffix where {Type} is retrieved assuming
	 * our model is also a {@link ITypedObject} model. Other model implementation should override.
	 * 
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		return ((INamedObject) getModel()).getName();
	}

	/**
	 * Pre-implementation of the title area to help concrete implementations
	 */
	public String getAreaTitle() {
		String title = ""; //$NON-NLS-1$
		if (getModel() instanceof ITypedObject) {
			final ITypedObject typedObj = (ITypedObject) getModel();
			title = MessageFormat
					.format(UIMessages.getString("wizard.compatibility.typedCreationTitle"), typedObj.getType().getName()); //$NON-NLS-1$
		} else {
			title = UIMessages.getString("wizard.compatibility.genericCreationTitle"); //$NON-NLS-1$
		}
		return title;
	}

	/**
	 * Default implementation assuming our model is a named object.
	 * 
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		INamedObject model = (INamedObject) getModel();
		switch (event) {
		case NAME_CHANGED:
			model.setName((String) data);
			break;
		case DESCRIPTION_CHANGED:
			model.setDescription((String) data);
			break;
		case VALIDATE:
			break;
		case MODEL_CHANGED:
		default:
			if (control != null && !control.isDisposed()) {
				refreshConnector();
			}
		}
	}

	@Override
	public void setUIComponentContainer(IUIComponentContainer container) {
		this.container = container;
	}

	@Override
	public IUIComponentContainer getUIComponentContainer() {
		return container;
	}

	@Override
	public String getDescription() {
		final Object model = getModel();
		String typeName = ""; //$NON-NLS-1$
		if (model instanceof ITypedObject) {
			typeName = ((ITypedObject) model).getType().getName();
			if (typeName != null) {
				typeName = typeName.toLowerCase();
			}
		}
		return MessageFormat.format(UIMessages.getString("connector.description"), typeName); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return getConnectorIcon();
	}

	@Override
	public void dispose() {
		releaseConnector();
	}
}
