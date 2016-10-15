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
package com.nextep.designer.vcs.ui.jface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.vcs.model.IRepositoryUser;


/**
 * This provider provides all users defined in repository database.
 * 
 * @author Christophe Fondacci
 *
 */
public class UsersContentProvider implements IStructuredContentProvider, IEventListener {

	private Collection<IRepositoryUser> allUsers;
	private Viewer viewer;
	public UsersContentProvider(Viewer viewer) {
		this.viewer = viewer;
	}
	@Override
	public Object[] getElements(Object inputElement) {
		return allUsers.toArray();
	}

	@Override
	public void dispose() {
		unregisterListeners();
	}

	private void unregisterListeners() {
		if(allUsers!=null) {
			for(IRepositoryUser user : allUsers) {
				Designer.getListenerService().unregisterListener(user, this);
			}
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(allUsers != null) {
			unregisterListeners();
			allUsers.clear();
		} else {
			allUsers = new ArrayList<IRepositoryUser>();
		}
		if(newInput instanceof List<?>) {
			List<IRepositoryUser> users = (List<IRepositoryUser>)newInput;
			for(IRepositoryUser user : users) {
				Designer.getListenerService().registerListener(this, user, this);
				allUsers.add(user);
			}
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		viewer.refresh();
	}

}
