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
package com.nextep.designer.unittest.core;

import junit.framework.TestCase;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.IListenerService.DispatchMode;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;

public class ListenerServiceTest extends TestCase {

	private class TestObservable extends Observable {

	}

	private class TestListener implements IEventListener, IModelOriented<Object> {

		private ChangeEvent listenedEvent;
		private Object model;
		private IObservable listenedSource;

		public TestListener(Object model) {
			this.model = model;
		}

		@Override
		public void handleEvent(ChangeEvent event, IObservable source, Object data) {
			this.listenedEvent = event;
			listenedSource = source;
		}

		@Override
		public void setModel(Object model) {
			this.model = model;
		}

		@Override
		public Object getModel() {
			return model;
		}

		public void reset() {
			listenedEvent = null;
			listenedSource = null;
		}
	}

	@Override
	protected void runTest() throws Throwable {

		IListenerService service = Designer.getListenerService();
		// Init
		final TestObservable obs1 = new TestObservable();
		final TestListener listener = new TestListener(obs1);
		// Registering
		service.registerListener(this, obs1, listener);
		// Check registration
		assertTrue("Listener has not been added to observable",
				obs1.getListeners().contains(listener));
		// Notification test
		obs1.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		assertEquals("Listened event differs from sent event", ChangeEvent.MODEL_CHANGED,
				listener.listenedEvent);
		assertEquals("Listened observable differs from registered observable", obs1,
				listener.listenedSource);
		// Switch test
		listener.reset();
		final TestObservable obs2 = new TestObservable();
		service.switchListeners(obs1, obs2);
		assertFalse("Old observable still has listeners after a switch", obs1.getListeners()
				.contains(listener));
		assertTrue("New observable do not have old observable listeners after a switch", obs2
				.getListeners().contains(listener));
		assertEquals("New observable is not the model of the model oriented listener", obs2,
				listener.getModel());
		// Notification test
		obs1.notifyListeners(ChangeEvent.ALIAS_CHANGED, null);
		assertNull("Old observable is still listened", listener.listenedEvent);
		obs2.notifyListeners(ChangeEvent.BREAKPOINT_ADDED, null);
		assertEquals("New observable is not listened after a switch", ChangeEvent.BREAKPOINT_ADDED,
				listener.listenedEvent);
		assertEquals("New observable is not sent as the source of the event after a switch", obs2,
				listener.listenedSource);

		// Blocking test
		listener.reset();
		service.setDispatchMode(DispatchMode.ASYNCHED);
		TestListener beforeListenerObs2 = new TestListener(obs2);
		service.registerListener(this, obs2, beforeListenerObs2);
		service.switchListeners(obs2, obs1);
		TestListener beforeListenerObs1 = new TestListener(obs1);
		service.registerListener(this, obs1, beforeListenerObs1);
		obs1.notifyListeners(ChangeEvent.UPDATES_LOCKED, null);
		TestListener afterListenerObs1 = new TestListener(obs1);
		service.registerListener(this, obs1, afterListenerObs1);
		service.setDispatchMode(DispatchMode.SYNCHED);
		assertEquals("Listener did not get the event in a blocked listeners context",
				ChangeEvent.UPDATES_LOCKED, listener.listenedEvent);
		assertEquals("Listener had an event coming from the wrong source in a blocked context",
				obs1, listener.listenedSource);
		assertEquals("Listener registered after blocking did not receive events",
				ChangeEvent.UPDATES_LOCKED, beforeListenerObs2.listenedEvent);
		assertEquals("Listener registered on target after blocking did not receive events",
				ChangeEvent.UPDATES_LOCKED, beforeListenerObs1.listenedEvent);
		assertNull("Listener registered during block after notification received events",
				afterListenerObs1.listenedSource);
	}

	@Override
	public String getName() {
		return "Listener service test";
	}
}
