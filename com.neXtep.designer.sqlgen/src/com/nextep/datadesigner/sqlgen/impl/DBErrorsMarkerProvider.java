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
package com.nextep.datadesigner.sqlgen.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerProvider;
import com.nextep.designer.core.model.MarkerScope;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.ErrorInfo;
import com.nextep.designer.sqlgen.services.ICaptureService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ITargetSet;
import com.nextep.designer.vcs.model.IVersionable;

public class DBErrorsMarkerProvider extends Observable implements IMarkerProvider {

	private MultiValueMap markersMap;
	private boolean validated = false;
	private Long validationTimeout = null;
	private static final Log log = LogFactory.getLog(DBErrorsMarkerProvider.class);

	public DBErrorsMarkerProvider() {
		markersMap = new MultiValueMap();
	}

	// @SuppressWarnings("unchecked")
	// @Override
	// public Collection<IMarker> getMarkers() {
	// if (!validated) {
	// recompileAndLoadMarkers();
	// }
	// return markersMap.values();
	// }

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IMarker> getMarkersFor(ITypedObject o) {
		if (!validated) {
			recompileAndLoadMarkers();
		}
		if (o instanceof IReferenceable) {
			return markersMap.getCollection(((IReferenceable) o).getReference());
		}
		return markersMap.getCollection(o);
	}

	@Override
	public void invalidate() {
		this.validated = false;
	}

	@Override
	public void invalidate(Object o) {
	}

	private synchronized void recompileAndLoadMarkers() {
		if (!isValidated()) {
			final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
			// Resetting markers
			markersMap = new MultiValueMap();
			// Fetching for all connections
			final ITargetSet targetSet = VCSPlugin.getViewService().getCurrentViewTargets();
			if (targetSet != null) {
				final Collection<IConnection> connections = targetSet.getTarget(SQLGenUtil
						.getDefaultTargetType());
				for (IConnection conn : connections) {
					final IDatabaseConnector<?> connector = CorePlugin.getConnectionService()
							.getDatabaseConnector(conn.getDBVendor());
					Connection c = null;
					Statement stmt = null;
					try {
						c = connector.connect(conn);
						stmt = c.createStatement();
						// Recompiling first
						stmt.execute("begin  dbms_utility.compile_schema(user,false); end;");
						// Fetching errors

					} catch (SQLException e) {
						markersMap.put(conn, coreFactory.createMarker(conn, MarkerType.ERROR,
								"Connection failed: " + e.getMessage()));
						// Setting up a timeout before retrying
						validationTimeout = System.currentTimeMillis() + 120000;
					} finally {
						CaptureHelper.safeClose(null, stmt);
						if (c != null) {
							try {
								c.close();
							} catch (SQLException e) {
								log.error("Unable to close connection", e);
							}
						}
					}
					final ICaptureService captureService = SQLGenPlugin
							.getService(ICaptureService.class);
					final Collection<ErrorInfo> errors = captureService.getErrorsFromDatabase(conn,
							new NullProgressMonitor());
					// Hashing current view contents by name
					Map<String, ITypedObject> objMap = hashCurrentViewByName();
					for (ErrorInfo i : errors) {
						final ITypedObject o = objMap.get(i.getObjectName());
						IMarker m = coreFactory.createMarker(o,
								"ERROR".equals(i.getAttribute()) ? MarkerType.ERROR
										: MarkerType.WARNING, i.getErrorMessage());
						m.setAttribute(IMarker.ATTR_LINE, i.getLine());
						m.setAttribute(IMarker.ATTR_COL, i.getCol());
						// Specific quick'n dirty fix for Triggers
						if (o != null && o.getType() == IElementType.getInstance(ITrigger.TYPE_ID)) {
							final ITrigger trg = (ITrigger) o;
							if (trg.isCustom()) {
								Pattern pat = Pattern.compile("(DECLARE|BEGIN)");
								Matcher mat = pat.matcher(trg.getSql().toUpperCase());
								if (mat.find()) {
									int index = mat.start();
									pat = Pattern.compile("\n");
									mat = pat.matcher(trg.getSql().substring(0, index));
									int lineShift = 0;
									while (mat.find()) {
										lineShift++;
									}
									m.setAttribute(IMarker.ATTR_LINE, i.getLine() + lineShift);
								}
							}
						}
						m.setAttribute(IMarker.ATTR_EXTERNAL_TYPE, i.getObjectTypeName());
						m.setAttribute(IMarker.ATTR_CONTEXT, conn);
						if (o instanceof IReferenceable) {
							markersMap.put(((IReferenceable) o).getReference(), m);
						} else {
							markersMap.put(o, m);
						}
					}
				}
				validated = true;
			}
		}
	}

	private Map<String, ITypedObject> hashCurrentViewByName() {
		Map<String, ITypedObject> map = new HashMap<String, ITypedObject>();
		Collection<IVersionable<?>> all = VersionHelper.getAllVersionables(
				VersionHelper.getCurrentView(), null);
		for (IVersionable<?> v : all) {
			map.put(v.getName(), v);
		}
		return map;
	}

	@Override
	public MarkerScope getProvidedMarkersScope() {
		return MarkerScope.DEFAULT;
	}

	/**
	 * @return the validated
	 */
	public boolean isValidated() {
		if (validated) {
			validationTimeout = null;
			return validated;
		} else {
			if (validationTimeout != null) {
				// If we set a timeout then we consider the state valid until the timeout is reached
				// See bug DES-968.
				return (validationTimeout >= System.currentTimeMillis());
			} else {
				return false;
			}
		}
	}
}
