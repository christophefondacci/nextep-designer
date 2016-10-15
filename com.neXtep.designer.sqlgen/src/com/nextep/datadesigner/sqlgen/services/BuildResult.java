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
package com.nextep.datadesigner.sqlgen.services;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.Status;
import com.nextep.designer.sqlgen.model.IGenerationResult;

/**
 * A class containing information on the result
 * of a build operation.
 * 
 * @author Christophe Fondacci
 *
 */
public class BuildResult {

	private Date buildDate;
	private Status status;
	private ISQLScript script;
	private String logFilePath;
	private MultiValueMap contents;
	
	public static enum BuildType {
		ADDITION, UPDATE, REMOVAL;
	}
	
	public BuildResult(Date buildDate, ISQLScript script, String logFilePath) {
		this.buildDate = buildDate;
		this.script = script;
		this.logFilePath = logFilePath;
		contents = new MultiValueMap();
	}
	public Date getBuildDate() {
		return buildDate;
	}
	
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public ISQLScript getScript() {
		return script;
	}
	public String getLogFilePath() {
		return logFilePath;
	}
	public void buildContents(IGenerationResult r) {
		if(r!=null) {
			for(DatabaseReference ref : r.getAddedReferences().keySet()) {
				contents.put(BuildType.ADDITION, ref);
			}
			for(DatabaseReference ref : r.getUpdatedReferences().keySet()) {
				contents.put(BuildType.UPDATE, ref);
			}
			for(DatabaseReference ref : r.getDroppedReferences().keySet()) {
				contents.put(BuildType.REMOVAL, ref);
			}
		}
	}
	public Map<BuildType,Collection<DatabaseReference>> getContents() {
		return contents;
	}
	
}
