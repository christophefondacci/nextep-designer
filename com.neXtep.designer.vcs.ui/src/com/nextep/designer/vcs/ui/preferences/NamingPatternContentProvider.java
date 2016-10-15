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
package com.nextep.designer.vcs.ui.preferences;

import java.util.Collection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.services.NamingService;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.INamingPattern;

public class NamingPatternContentProvider implements IStructuredContentProvider, IEventListener {

	private Collection<INamingPattern> patterns;
	private TableViewer viewer;

	public NamingPatternContentProvider(TableViewer v) {
		this.viewer = v;
	}

	@Override
	public void dispose() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (patterns != null) {
			for (INamingPattern p : patterns) {
				Designer.getListenerService().unregisterListener(p, this);
			}
		}
		this.patterns = (Collection<INamingPattern>) newInput;
		if (patterns != null) {
			for (INamingPattern p : patterns) {
				Designer.getListenerService().registerListener(viewer.getControl(), p, this);
			}
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return patterns.toArray();
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		viewer.update(source, null);
		NamingService.getInstance().invalidate();
		CorePlugin.getIdentifiableDao().save((IdentifiedObject) source);
	}

	public void add(INamingPattern p) {
		Designer.getListenerService().registerListener(viewer.getControl(), p, this);
		viewer.add(p);
		patterns.add(p);
		NamingService.getInstance().invalidate();
		CorePlugin.getIdentifiableDao().save(p);
	}

	public void remove(INamingPattern p) {
		Designer.getListenerService().unregisterListener(p, this);
		viewer.remove(p);
		patterns.remove(p);
		NamingService.getInstance().invalidate();
		CorePlugin.getIdentifiableDao().delete(p);
	}
}
