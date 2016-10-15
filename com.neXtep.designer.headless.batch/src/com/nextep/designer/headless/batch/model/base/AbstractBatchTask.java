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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.headless.batch.model.base;

import java.util.Collections;
import java.util.List;
import com.nextep.designer.headless.batch.model.IBatchTask;
import com.nextep.designer.headless.batch.services.IBatchTaskService;

/**
 * @author Christophe Fondacci
 */
public abstract class AbstractBatchTask implements IBatchTask {

	private IBatchTaskService batchTaskService;
	private String id;
	private String description;
	private List<String> usedOptions = Collections.emptyList();

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public List<String> getUsedOptionGroups() {
		return usedOptions;
	}

	@Override
	public void setUsedOptionGroups(List<String> optionGroups) {
		this.usedOptions = optionGroups;
	}

	@Override
	public void setBatchTaskService(IBatchTaskService batchTaskService) {
		this.batchTaskService = batchTaskService;
	}

	@Override
	public IBatchTaskService getBatchTaskService() {
		return batchTaskService;
	}
}
