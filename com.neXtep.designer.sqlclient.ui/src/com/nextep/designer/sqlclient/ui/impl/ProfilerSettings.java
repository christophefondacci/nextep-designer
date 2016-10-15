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
package com.nextep.designer.sqlclient.ui.impl;

import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.sqlclient.ui.model.IProfilerSettings;

public class ProfilerSettings extends Observable implements
		IProfilerSettings {

	private int threads;
	private int duration;
	private int stepDuration;
	private boolean isFetching;
	private boolean isRandomOrder;
	
	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public int getThreadCount() {
		return threads;
	}

	@Override
	public void setDuration(int time) {
		this.duration = time;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setThreadCount(int count) {
		this.threads = count;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public int getThreadStepDuration() {
		return stepDuration;
	}

	@Override
	public void setThreadStepDuration(int time) {
		this.stepDuration = time;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public boolean isFetching() {
		return isFetching;
	}
	@Override
	public void setFetching(boolean fetching) {
		isFetching = fetching;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public boolean isRandomOrder() {
		return isRandomOrder;
	}

	@Override
	public void setRandomOrder(boolean randOrder) {
		isRandomOrder = randOrder;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}
	
}
