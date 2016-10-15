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
package com.nextep.installer.helpers;

import java.util.ArrayList;
import java.util.List;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IInstallConfiguration;

public final class PostgreSqlHelper {

	private PostgreSqlHelper() {
	}

	public static final List<String> getProcessBuilderArgs(IInstallConfiguration conf) {
		final IDatabaseTarget target = conf.getTarget();
		final String login = target.getUser();
		final String password = target.getPassword();
		String host = target.getHost();
		final String port = target.getPort();
		final String sid = target.getDatabase();
		final String bin = ExternalProgramHelper.getProgramLocation(conf, "psql");

		List<String> args = new ArrayList<String>();
		args.add(bin == null ? "psql" : bin);
		StringBuffer buf = new StringBuffer(50);
		if (host == null) {
			host = "127.0.0.1";
		}
		buf.append("host='" + host + "'");
		if (port != null) {
			buf.append(" port=" + port);
		}
		buf.append(" dbname='" + sid + "'");
		buf.append(" user='" + login + "'");
		if (password != null) {
			buf.append(" password='" + password + "'");
		}
		// buf.append("\"");
		args.add(buf.toString());
		return args;
	}
}
