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
package com.nextep.designer.sqlgen.oracle.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

public class OracleClusterGenerator extends SQLGenerator {

	private static final Log log = LogFactory.getLog(OracleClusterGenerator.class);

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IOracleCluster src = (IOracleCluster) result.getSource();
		final IOracleCluster tgt = (IOracleCluster) result.getTarget();
		log.warn("Unable to generate a script to ALTER cluster "
				+ (src == null ? tgt.getName() : src.getName()
						+ ": drop cluster manually to allow regeneration"));
		return null;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		return null;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IOracleCluster cluster = (IOracleCluster) model;
		final String clusterName = getName(cluster);
		final String rawName = cluster.getName();

		ISQLScript s = new SQLScript(rawName, cluster.getDescription(), "", ScriptType.TABLE);
		s.appendSQL("Prompt Creating cluster '" + rawName + "'..." + NEWLINE);
		s.appendSQL("CREATE CLUSTER " + clusterName);
		IGenerationResult columnGeneration = generateChildren(cluster.getColumns(), false);
		addCommaSeparatedScripts(s, " ( ", ")", columnGeneration.getAdditions());

		if (cluster.getPhysicalProperties() != null) {
			IPhysicalProperties props = cluster.getPhysicalProperties();
			s.appendSQL("  ");
			for (PhysicalAttribute a : props.getAttributes().keySet()) {
				final Object val = props.getAttribute(a);
				if (val != null) {
					s.appendSQL(a.getName() + " " + val.toString() + " ");
				}
			}
			if (props.getTablespaceName() != null && !props.getTablespaceName().trim().isEmpty()) {
				s.appendSQL("TABLESPACE " + props.getTablespaceName());
			}
			s.appendSQL(NEWLINE);
		}
		s.appendSQL("/" + NEWLINE);
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(cluster.getType(), cluster.getName()), s);
		return r;
	}

}
