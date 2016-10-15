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
package com.nextep.datadesigner.dbgm.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.ITriggable;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.IViewLinker;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.model.ITargetSet;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * @author Christophe Fondacci
 */
public class DBGMLinker implements IViewLinker {

	private static final Log log = LogFactory.getLog(DBGMLinker.class);

	/**
	 * @see com.nextep.datadesigner.vcs.services.IViewLinker#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Database generic model";
	}

	/**
	 * @see com.nextep.datadesigner.vcs.services.IViewLinker#link(com.nextep.designer.vcs.model.IWorkspace)
	 */
	@Override
	public void link(IWorkspace view) {
		// Linking indexes
		Collection<IVersionable<?>> indexes = (Collection<IVersionable<?>>) VersionHelper
				.getAllVersionables(view, IElementType.getInstance(IIndex.INDEX_TYPE));
		for (IVersionable<?> v : indexes) {
			IIndex index = (IIndex) v.getVersionnedObject().getModel();
			index.getIndexedTable().addIndex(index);
		}
		// Linking datasets
		Collection<IVersionable<?>> datasets = (Collection<IVersionable<?>>) VersionHelper
				.getAllVersionables(view, IElementType.getInstance(IDataSet.TYPE_ID));
		for (IVersionable<?> v : datasets) {
			IDataSet dSet = (IDataSet) v.getVersionnedObject().getModel();
			dSet.getTable().addDataSet(dSet);
		}
		// Linking triggers
		Collection<IVersionable<?>> triggers = (Collection<IVersionable<?>>) VersionHelper
				.getAllVersionables(view, IElementType.getInstance(ITrigger.TYPE_ID));
		for (IVersionable<?> v : triggers) {
			ITrigger trigger = (ITrigger) v.getVersionnedObject().getModel();
			ITriggable r = (ITriggable) VersionHelper.getReferencedItem(trigger.getTriggableRef());
			r.addTrigger(trigger);
		}
		// Saving old target set
		final ITargetSet oldTargetSet = DBGMHelper.getTargetSet();
		// Notifying new target set / reinitializes
		Designer.getListenerService().switchListeners(oldTargetSet, DBGMHelper.getTargetSet());
	}

	@SuppressWarnings("unchecked")
	public void relink(ITypedObject o, MultiValueMap invRefMap) {
		if (o instanceof IVersionContainer) {
			// If we have a container we need to relink all elements it contains
			Collection<IVersionable<?>> versionables = ((IVersionContainer) o).getContents();
			for (IVersionable<?> v : versionables) {
				relink(v, invRefMap);
			}
		}
		if (o.getType() == IElementType.getInstance(IBasicTable.TYPE_ID)
				|| o instanceof IBasicTable) {
			IBasicTable t = (IBasicTable) o;
			// t.getIndexes().clear();
			// t.getDataSets().clear();
			// t.getTriggers().clear();
			for (IIndex i : new ArrayList<IIndex>(t.getIndexes())) {
				t.removeIndex(i);
			}
			// Because their could be some hashcode problems during reverse synch, we clear any
			// residual index
			t.getIndexes().clear();
			for (IDataSet d : new ArrayList<IDataSet>(t.getDataSets())) {
				t.removeDataSet(d);
			}
			t.getDataSets().clear();
			for (ITrigger tr : new ArrayList<ITrigger>(t.getTriggers())) {
				t.removeTrigger(tr);
			}
			t.getTriggers().clear();
			try {
				Collection<IReferencer> referers = invRefMap.getCollection(t.getReference());
				if (referers == null) {
					referers = Collections.emptyList();
				}
				for (IReferencer r : referers) {
					if (r instanceof IReferenceable) {
						if (((IReferenceable) r).getReference().isVolatile() != t.getReference()
								.isVolatile()) {
							continue;
						}
					}
					if (r instanceof IIndex) {
						t.addIndex((IIndex) r);
					}
					if (r instanceof IDataSet) {
						t.addDataSet((IDataSet) r);
					}
					if (r instanceof ITrigger) {
						t.addTrigger((ITrigger) r);
					}
				}
			} catch (UnresolvedItemException e) {
				log.debug("Unable to retrieve reverse dependencies during the link of "
						+ t.getType().getName() + " " + t.getName(), e);
			}
		} else if (o.getType() == IElementType.getInstance(IIndex.INDEX_TYPE)) {
			final IIndex index = (IIndex) o;
			IBasicTable t = index.getIndexedTable();
			// Only linking on same scope resolution
			if (index.getReference().isVolatile() == t.getReference().isVolatile()) {
				for (IIndex i : new ArrayList<IIndex>(t.getIndexes())) {
					if (i.getReference().equals(index.getReference())) {
						t.removeIndex(i);
					}
				}
				// Bug
				t.removeIndex(index);
				t.addIndex(index);
			}
		} else if (o.getType() == IElementType.getInstance(IDataSet.TYPE_ID)) {
			final IDataSet set = (IDataSet) o;
			IBasicTable t = set.getTable();
			// Only linking on same scope resolution
			if (set.getReference().isVolatile() == t.getReference().isVolatile()) {
				for (IDataSet ds : new ArrayList<IDataSet>(t.getDataSets())) {
					if (ds.getReference().equals(set.getReference())) {
						t.removeDataSet(ds);
					}
				}
				// Bug
				t.removeDataSet(set);
				t.addDataSet(set);
			}
		} else if (o.getType() == IElementType.getInstance(ITrigger.TYPE_ID)) {
			final ITrigger trigger = (ITrigger) o;
			try {
				ITriggable t = (ITriggable) VersionHelper.getReferencedItem(trigger
						.getTriggableRef());
				if (t.getReference().isVolatile() == trigger.getReference().isVolatile()) {
					for (ITrigger trig : new ArrayList<ITrigger>(t.getTriggers())) {
						if (trig.getReference().equals(trigger.getReference())) {
							t.removeTrigger(trig);
						}
					}
					t.removeTrigger(trigger);
					t.addTrigger(trigger);
				}
			} catch (RuntimeException e) {
				log.warn("Problems while relinking trigger " + trigger.getName()
						+ " back to repository");
			}
		}
	}
}
