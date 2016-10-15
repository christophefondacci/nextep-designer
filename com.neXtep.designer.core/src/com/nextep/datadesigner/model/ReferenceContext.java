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
package com.nextep.datadesigner.model;

import java.util.Date;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.hibernate.Session;
import com.nextep.designer.core.model.impl.ReferenceManager;

/**
 * This class defines the context of a reference. 
 * Its aim is to categorize {@link IReference} instances within the 
 * {@link ReferenceManager} depending on the session which created
 * them (should there be a session).<br>
 * The flush time corresponds to the last time when the session has
 * been flushed. Since a single session could load a same reference
 * multiple times after being flushed, flush time is a reference context
 * key.
 * 
 * @author Christophe
 *
 */
public class ReferenceContext extends MultiKey {
	
	/** Serialization compliance */
	private static final long serialVersionUID = 1268269688710804597L;

	public ReferenceContext(Session s, Date flushTime) {
		super(s,flushTime);
	}

}
