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
package com.nextep.designer.headless.services.impl;

import java.util.ArrayList;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.IObservable;

/**
 * @author Christophe Fondacci
 */
public class HeadLessListenerService implements IListenerService {

	@Override
	public void setDispatchMode(DispatchMode mode) {

	}

	@Override
	public DispatchMode getDispatchMode() {
		return DispatchMode.ASYNCHED;
	}

	@Override
	public void registerListener(Object instigator, IObservable o, IEventListener l) {
		o.addListener(l);
	}

	@Override
	public void activateListeners(Object instigator) {

	}

	@Override
	public void unregisterListeners(Object instigator) {

	}

	@Override
	public void unregisterListener(IObservable o, IEventListener l) {
		o.removeListener(l);
	}

	@Override
	public void switchListeners(IObservable oldObservable, IObservable newObservable) {
		if (oldObservable != null && oldObservable.getListeners() != null) {
			for (IEventListener l : new ArrayList<IEventListener>(oldObservable.getListeners())) {
				newObservable.addListener(l);
				oldObservable.removeListener(l);
			}
		}
	}

	@Override
	public void notifyListeners(IObservable observable, ChangeEvent event, Object arg) {
		for (IEventListener l : observable.getListeners()) {
			l.handleEvent(event, observable, arg);
		}
	}

}
