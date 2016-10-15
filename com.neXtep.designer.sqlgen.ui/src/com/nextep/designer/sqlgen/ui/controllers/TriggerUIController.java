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
package com.nextep.designer.sqlgen.ui.controllers;

import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.dbgm.gui.TriggerEditorGUI;
import com.nextep.datadesigner.dbgm.gui.navigators.TriggerNavigator;
import com.nextep.datadesigner.dbgm.model.ITriggable;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.dbgm.model.TriggerTime;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.ui.dbgm.MixedSQLEditor;
import com.nextep.designer.sqlgen.ui.dbgm.MixedSQLEditorInput;
import com.nextep.designer.sqlgen.ui.dbgm.TriggerEditorInput;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 */
public class TriggerUIController extends AbstractUIController {

	public TriggerUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.TRIGGER_TIME_CHANGED);
		addSaveEvent(ChangeEvent.TRIGGER_EVENTS_CHANGED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new TriggerEditorGUI((ITrigger) content, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeGraphical(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeNavigator(java.lang.Object)
	 */
	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new TriggerNavigator((ITrigger) model, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeProperty(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	@Override
	public Object newInstance(Object parent) {
		IVersionable<ITrigger> v = VersionableFactory.createVersionable(ITrigger.class);
		ITrigger trigger = v.getVersionnedObject().getModel();

		ITriggable parentTriggable = null;
		IVersionContainer parentContainer = null;
		if (parent instanceof ITriggable) {
			parentTriggable = (ITriggable) parent;
			trigger.setTriggableRef(parentTriggable.getReference());
		} else if (parent instanceof IVersionContainer) {
			parentContainer = (IVersionContainer) parent;
		}
		trigger.setTime(TriggerTime.BEFORE);
		trigger.addEvent(TriggerEvent.INSERT);
		// Mysql / Oracle test (to avoid trigger double implementation for now)
		if (VCSPlugin.getViewService().getCurrentWorkspace().getDBVendor() == DBVendor.ORACLE) {
			trigger.addEvent(TriggerEvent.UPDATE);
			trigger.addEvent(TriggerEvent.DELETE);
		}
		trigger.setSourceCode("begin\r\n\r\nend;");
		newWizardEdition("New trigger creation...", initializeEditor(trigger));
		if (trigger.getTriggableRef() == null) {
			throw new CancelException("Cannot create a trigger on an undefined element");
		}
		// Our parent object may have been changed by the user
		parentTriggable = (ITriggable) VersionHelper.getReferencedItem(trigger.getTriggableRef());
		// Oracle triggers are always defined custom and locked in this mode
		if (VCSPlugin.getViewService().getCurrentWorkspace().getDBVendor() == DBVendor.ORACLE) {
			trigger.setCustom(true);
		}
		fillTriggerSource(trigger);
		// Saving trigger
		save(trigger);
		// Registering parent container
		if (parentContainer == null) {
			parentContainer = VersionHelper.getVersionable(parentTriggable).getContainer();
		}
		parentContainer.addVersionable(v, new ImportPolicyAddOnly());
		// Registering table trigger
		parentTriggable.addTrigger(trigger);

		// We're done
		return trigger;
	}

	private void fillTriggerSource(ITrigger t) {
		StringBuffer s = new StringBuffer(50);
		if (t.isCustom()) {
			s.append("TRIGGER " + t.getName() + " " + t.getTime().name() + " ");
			boolean first = true;
			for (TriggerEvent e : t.getEvents()) {
				if (first) {
					first = false;
				} else {
					s.append(" OR ");
				}
				s.append(e.name());
			}
			s.append(" ON "
					+ ((INamedObject) VersionHelper.getReferencedItem(t.getTriggableRef()))
							.getName() + "\n");
			s.append("FOR EACH ROW\nbegin\n\nend;");
		} else {
			s.append("begin\n\nend;");
		}
		t.setSourceCode(s.toString());
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		final ITrigger trigger = (ITrigger) model;
		return new MixedSQLEditorInput(trigger, new TriggerEditorInput(trigger));
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getEditorId()
	 */
	@Override
	public String getEditorId() {
		return MixedSQLEditor.EDITOR_ID;
	}
}
