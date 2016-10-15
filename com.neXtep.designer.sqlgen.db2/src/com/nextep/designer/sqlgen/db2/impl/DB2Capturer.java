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
package com.nextep.designer.sqlgen.db2.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.dbgm.impl.Synonym;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.dbgm.model.TriggerTime;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.ICaptureContext;
import com.nextep.designer.sqlgen.model.ICapturer;
import com.nextep.designer.sqlgen.model.IMutableCaptureContext;
import com.nextep.designer.sqlgen.model.base.AbstractCapturer;
import com.nextep.designer.sqlgen.services.ICaptureService;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Bruno Gautier
 */
// TODO [BGA] Convert all java.sql.Statement to java.sql.PreparedStatement
public final class DB2Capturer extends AbstractCapturer {

	private static final Log LOGGER = LogFactory.getLog(DB2Capturer.class);

	private ICapturer jdbcCapturer = null;
	private int counter = 0;

	private int getCounter() {
		return counter;
	}

	@Override
	public void initialize(IConnection conn, IMutableCaptureContext context) {
		super.initialize(conn, context);
		jdbcCapturer = SQLGenPlugin.getService(ICaptureService.class).getCapturer(DBVendor.JDBC);
	}

	@Override
	public Collection<IBasicTable> getTables(ICaptureContext context, IProgressMonitor monitor) {
		return jdbcCapturer.getTables(context, monitor);
	}

	@Override
	public Collection<IIndex> getIndexes(ICaptureContext context, IProgressMonitor monitor) {
		Map<MultiKey, IIndex> indexes = new HashMap<MultiKey, IIndex>();
		final Connection conn = (Connection) context.getConnectionObject();
		IFormatter formatter = context.getConnection().getDBVendor().getNameFormatter();

		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();

			rset = stmt
					.executeQuery("SELECT i.tabname, i.indname, i.indextype, i.uniquerule, i.remarks, " //$NON-NLS-1$
							+ "c.colname, c.colseq, c.colorder " //$NON-NLS-1$
							+ "FROM syscat.indexes i, syscat.indexcoluse c " //$NON-NLS-1$
							+ "WHERE i.indschema = '" + context.getSchema() + "' AND i.tabschema = i.indschema " //$NON-NLS-1$ //$NON-NLS-2$
							+ "AND i.uniquerule <> 'P' AND i.indextype = 'REG' " //$NON-NLS-1$
							+ "AND c.indname = i.indname AND c.indschema = i.indschema " //$NON-NLS-1$
							+ "ORDER BY i.tabname, i.indname, c.colseq"); //$NON-NLS-1$
			CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);

			IIndex currIndex = null;
			String currIndexName = null;
			boolean indexIsValid = false;

			while (rset.next()) {
				final String tableName = rset.getString("tabname"); //$NON-NLS-1$
				final String indexName = rset.getString("indname"); //$NON-NLS-1$
				final String indexType = rset.getString("indextype"); //$NON-NLS-1$
				final String uniqueRule = rset.getString("uniquerule"); //$NON-NLS-1$
				final String indexDesc = rset.getString("remarks"); //$NON-NLS-1$
				final String indexColumnName = rset.getString("colname"); //$NON-NLS-1$
				final int indexColumnSeq = rset.getInt("colseq"); //$NON-NLS-1$
				final String ascOrDesc = rset.getString("colorder"); //$NON-NLS-1$

				if (indexName != null && !"".equals(indexName.trim())) { //$NON-NLS-1$
					if (LOGGER.isDebugEnabled()) {
						String logPrefix = "[" + indexName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug("= " + logPrefix + " Index Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug(logPrefix + "[SYSCAT.INDEXES.TABNAME] " + tableName); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.INDEXES.INDEXTYPE] " + indexType); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.INDEXES.UNIQUERULE] " + uniqueRule); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.INDEXES.REMARKS] " + indexDesc); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.INDEXCOLUSE.COLNAME] " + indexColumnName); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.INDEXCOLUSE.COLSEQ] " + indexColumnSeq); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.INDEXCOLUSE.COLORDER] " + ascOrDesc); //$NON-NLS-1$
					}

					if (null == currIndexName || !currIndexName.equals(indexName) || indexIsValid) {
						currIndexName = indexName;
						final String formatTableName = formatter.format(tableName);
						final String formatIndexName = formatter.format(indexName);
						final String formatIndexColumnName = formatter.format(indexColumnName);

						if (null == currIndex || !formatIndexName.equals(currIndex.getIndexName())) {
							IVersionable<IIndex> v = VersionableFactory
									.createVersionable(IIndex.class);
							currIndex = v.getVersionnedObject().getModel();
							currIndex.setName(formatIndexName);
							currIndex.setDescription(indexDesc);
							currIndex.setIndexType("U".equals(uniqueRule) ? IndexType.UNIQUE //$NON-NLS-1$
									: IndexType.NON_UNIQUE);
							indexes.put(new MultiKey(formatTableName, formatIndexName), currIndex);
							indexIsValid = true;
							CaptureHelper.updateMonitor(monitor, getCounter(), 5, 1);
						}

						final IBasicColumn column = (IBasicColumn) context.getCapturedObject(
								IElementType.getInstance(IBasicColumn.TYPE_ID),
								CaptureHelper.getUniqueObjectName(formatTableName,
										formatIndexColumnName));
						if (column != null) {
							/*
							 * Columns are ordered by SYSCAT.INDEXCOLUSE.COLSEQ in the returned
							 * Resultset, so we don't have to specify the position of the index
							 * column when adding it to the index.
							 */
							currIndex.addColumnRef(column.getReference());
						} else {
							LOGGER.warn("Index ["
									+ formatIndexName
									+ "] has been ignored during import because the referencing column ["
									+ formatTableName + "[" + formatIndexColumnName //$NON-NLS-1$
									+ "]] could not be found in the current workspace");
							indexIsValid = false;

							/*
							 * Now the index is invalid, we remove it from the indexes list that
							 * will be returned to the caller of this method.
							 */
							indexes.remove(new MultiKey(formatTableName, formatIndexName));
						}
					}
				}
			}

			/*
			 * Once the list of valid indexes has been set, we can link the indexes with their
			 * corresponding table.
			 */
			for (MultiKey key : indexes.keySet()) {
				String tableName = (String) key.getKey(0);
				IIndex index = indexes.get(key);
				final IBasicTable table = context.getTable(tableName);
				index.setIndexedTableRef(table.getReference());
				table.addIndex(index);
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch indexes from DB2 server: " + sqle.getMessage(), sqle);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return indexes.values();
	}

	@Override
	public Collection<IView> getViews(ICaptureContext context, IProgressMonitor monitor) {
		Map<String, IView> views = new HashMap<String, IView>();
		final IConnection conn = context.getConnection();
		final DBVendor connectionVendor = conn.getDBVendor();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			List<String> viewsWithAliasNames = new ArrayList<String>();
			stmt = ((Connection) context.getConnectionObject()).createStatement();

			rset = stmt
					.executeQuery("SELECT v.viewname, v.text FROM syscat.tables t, syscat.views v " //$NON-NLS-1$
							+ "WHERE t.tabschema = '" + context.getSchema() + "' AND t.type = 'V' " //$NON-NLS-1$ //$NON-NLS-2$
							+ "AND v.viewname = t.tabname AND v.viewschema = t.tabschema"); //$NON-NLS-1$
			CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);

			while (rset.next()) {
				final String viewName = rset.getString("viewname"); //$NON-NLS-1$
				final String sqlText = rset.getString("text"); //$NON-NLS-1$

				if (viewName != null && !"".equals(viewName.trim())) { //$NON-NLS-1$
					if (LOGGER.isDebugEnabled()) {
						String logPrefix = "[" + viewName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug("= " + logPrefix + " View Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug(logPrefix + "[SYSCAT.VIEWS.TEXT] " + sqlText); //$NON-NLS-1$
					}

					String viewQuery = CaptureHelper.getQueryFromCreateAsStatement(sqlText);

					if (viewQuery != null && !"".equals(viewQuery.trim())) { //$NON-NLS-1$
						IVersionable<IView> v = VersionableFactory.createVersionable(IView.class);
						IView view = v.getVersionnedObject().getModel();
						view.setName(connectionVendor.getNameFormatter().format(viewName));
						view.setSQLDefinition(viewQuery);

						/*
						 * If the SQL definition of the view contains a columns list clause, we add
						 * the unformatted view name to the list of views names for which columns
						 * aliases must be retrieved.
						 */
						if (CaptureHelper.containsColumnsListClause(sqlText)) {
							viewsWithAliasNames.add(viewName);
						}

						views.put(viewName, view);
						CaptureHelper.updateMonitor(monitor, getCounter(), 5, 1);
					} else {
						LOGGER.warn("View [" + viewName
								+ "] has been ignored during import because the SQL definition "
								+ "could not be extracted from dictionary tables. "
								+ "View definition [" + sqlText + "]"); //$NON-NLS-2$
					}
				}
			}

			if (viewsWithAliasNames.size() > 0) {
				rset = stmt.executeQuery("SELECT t.tabname viewname, c.colname, c.colno " //$NON-NLS-1$
						+ "FROM syscat.tables t, syscat.columns c " //$NON-NLS-1$
						+ "WHERE t.tabschema = '" + context.getSchema() + "' AND t.type = 'V' " //$NON-NLS-1$ //$NON-NLS-2$
						+ "AND c.tabname = t.tabname AND c.tabschema = t.tabschema " //$NON-NLS-1$
						+ "AND t.tabname IN (" //$NON-NLS-1$
						+ CaptureHelper.getCommaSeparatedValues(viewsWithAliasNames) + ") " //$NON-NLS-1$
						+ "ORDER BY t.tabname, c.colno"); //$NON-NLS-1$
				CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);

				IView currView = null;
				String currViewName = null;
				while (rset.next()) {
					final String viewName = rset.getString("viewname"); //$NON-NLS-1$
					final String colName = rset.getString("colname"); //$NON-NLS-1$
					final int colNumber = rset.getInt("colno"); //$NON-NLS-1$

					if (viewName != null && !"".equals(viewName.trim())) { //$NON-NLS-1$
						if (LOGGER.isDebugEnabled()) {
							String logPrefix = "[" + viewName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
							LOGGER.debug("= " + logPrefix + " View columns aliases Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
							LOGGER.debug(logPrefix + "[SYSCAT.COLUMNS.COLNAME] " + colName); //$NON-NLS-1$
							LOGGER.debug(logPrefix + "[SYSCAT.COLUMNS.COLNO] " + colNumber); //$NON-NLS-1$
						}

						if (null == currViewName || !currViewName.equals(viewName)) {
							currViewName = viewName;
							currView = views.get(currViewName);
						}

						if (currView != null) {
							currView.addColumnAlias(connectionVendor.getNameFormatter().format(
									colName));
							CaptureHelper.updateMonitor(monitor, getCounter(), 5, 1);
						}
					}
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch views from DB2 server: " + sqle.getMessage(), sqle);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return views.values();
	}

	@Override
	public Collection<ITrigger> getTriggers(ICaptureContext context, IProgressMonitor monitor) {
		monitor.subTask("Retrieving triggers...");
		Collection<ITrigger> triggers = new ArrayList<ITrigger>();
		final IConnection conn = context.getConnection();
		final DBVendor connectionVendor = conn.getDBVendor();
		IFormatter formatter = connectionVendor.getNameFormatter();

		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = ((Connection) context.getConnectionObject()).createStatement();

			rset = stmt
					.executeQuery("SELECT trigname, remarks, tabname, text, " //$NON-NLS-1$
							+ "DECODE(valid,'N','INVALID','X','INOPERATIVE','Y','VALID') valid, " //$NON-NLS-1$
							+ "DECODE(trigtime,'A','AFTER','B','BEFORE','I','INSTEAD') trigtime, " //$NON-NLS-1$
							+ "DECODE(trigevent,'D','DELETE','I','INSERT','U','UPDATE') trigevent, " //$NON-NLS-1$
							+ "DECODE(granularity,'R','ROW','S','STATEMENT') granularity " //$NON-NLS-1$
							+ "FROM syscat.triggers " //$NON-NLS-1$
							+ "WHERE trigschema = '" + context.getSchema() + "' AND tabschema = trigschema"); //$NON-NLS-1$ //$NON-NLS-2$
			CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);

			while (rset.next()) {
				final String trigName = rset.getString("trigname"); //$NON-NLS-1$
				final String trigDesc = rset.getString("remarks"); //$NON-NLS-1$
				final String triggableName = rset.getString("tabname"); //$NON-NLS-1$
				final String sqlText = rset.getString("text"); //$NON-NLS-1$
				// final String valid = rset.getString("valid"); //$NON-NLS-1$
				final String trigTime = rset.getString("trigtime"); //$NON-NLS-1$
				final String trigEvent = rset.getString("trigevent"); //$NON-NLS-1$
				final String granularity = rset.getString("granularity"); //$NON-NLS-1$

				if (trigName != null && !"".equals(trigName.trim())) { //$NON-NLS-1$
					if (LOGGER.isDebugEnabled()) {
						String logPrefix = "[" + trigName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug("= " + logPrefix + " Trigger Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug(logPrefix + "[SYSCAT.TRIGGERS.TABNAME] " + triggableName); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.TRIGGERS.TRIGTIME] " + trigTime.charAt(0)); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.TRIGGERS.TRIGEVENT] " //$NON-NLS-1$
								+ trigEvent.charAt(0));
						LOGGER.debug(logPrefix + "[SYSCAT.TRIGGERS.GRANULARITY] " //$NON-NLS-1$
								+ granularity.charAt(0));
						// LOGGER.debug(logPrefix + "[SYSCAT.TRIGGERS.VALID] " + valid); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.TRIGGERS.TEXT] " + sqlText); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.TRIGGERS.REMARKS] " + trigDesc); //$NON-NLS-1$
					}

					String formattedTriggableName = formatter.format(triggableName);
					IReference triggableRef = null;
					final IBasicTable table = context.getTable(formattedTriggableName);
					if (table != null) {
						triggableRef = context.getTable(formattedTriggableName).getReference();
					} else {
						final Object viewObj = context.getCapturedObject(
								IElementType.getInstance(IView.TYPE_ID), formattedTriggableName);
						if (viewObj != null) {
							triggableRef = ((IView) viewObj).getReference();
						} else {
							LOGGER.warn("Trigger ["
									+ trigName
									+ "] has been ignored during import because the referenced table or view ["
									+ formattedTriggableName
									+ "] could not be found in the current workspace");
							continue;
						}
					}

					if (sqlText != null && !"".equals(sqlText.trim())) { //$NON-NLS-1$
						IVersionable<ITrigger> v = VersionableFactory
								.createVersionable(ITrigger.class);
						ITrigger trigger = v.getVersionnedObject().getModel();
						trigger.setName(formatter.format(trigName));
						trigger.setDescription(trigDesc);
						trigger.setTriggableRef(triggableRef);
						trigger.setTime(TriggerTime.valueOf(trigTime));
						trigger.addEvent(TriggerEvent.valueOf(trigEvent));
						trigger.setSourceCode(sqlText);
						trigger.setCustom(true);

						// TODO [BGA]: Add the granularity to the common trigger model

						triggers.add(trigger);
						CaptureHelper.updateMonitor(monitor, getCounter(), 5, 1);
					} else {
						LOGGER.warn("Trigger [" + trigName
								+ "] has been ignored during import because the SQL definition "
								+ "could not be extracted from dictionary tables. "
								+ "Trigger definition [" + sqlText + "]"); //$NON-NLS-2$
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Unable to fetch triggers from DB2 server: " + e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return triggers;
	}

	@Override
	public Collection<ISynonym> getSynonyms(ICaptureContext context, IProgressMonitor monitor) {
		monitor.subTask("Retrieving synonyms...");
		final String schema = context.getSchema();
		Collection<ISynonym> synonyms = new ArrayList<ISynonym>();

		Statement stmt = null;
		try {
			stmt = ((Connection) context.getConnectionObject()).createStatement();

			ResultSet rset = null;
			try {
				// TODO : Switch to prepare statement here !
				rset = stmt
						.executeQuery("SELECT DECODE(tabschema,'SYSPUBLIC',1,0) is_public, " //$NON-NLS-1$
								+ "tabname synname, remarks, '" + IBasicTable.TYPE_ID + "' syntype, " //$NON-NLS-1$ //$NON-NLS-2$
								+ "base_tabname refname, DECODE(base_tabschema,'" + schema + "',NULL,base_tabschema) refschema " //$NON-NLS-1$ //$NON-NLS-2$
								+ "FROM syscat.tables WHERE type = 'A' AND (tabschema = '" + schema + "' " //$NON-NLS-1$ //$NON-NLS-2$
								+ "OR (tabschema = 'SYSPUBLIC' AND base_tabschema = '" + schema + "'))"); //$NON-NLS-1$ //$NON-NLS-2$
				CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);

				Collection<ISynonym> tablesSynonyms = getSynonymsFromResultSet(context, rset,
						monitor);
				synonyms.addAll(tablesSynonyms);
			} catch (SQLException sqle) {
				LOGGER.error(
						"Unable to fetch tables synonyms from DB2 server: " + sqle.getMessage(),
						sqle);
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			try {
				rset = stmt
						.executeQuery("SELECT DECODE(seqschema,'SYSPUBLIC',1,0) is_public, " //$NON-NLS-1$
								+ "seqname synname, remarks, '" + ISequence.TYPE_ID + "' syntype, " //$NON-NLS-1$ //$NON-NLS-2$
								+ "base_seqname refname, DECODE(base_seqschema,'" + schema + "',NULL,base_seqschema) refschema " //$NON-NLS-1$ //$NON-NLS-2$
								+ "FROM syscat.sequences WHERE seqtype = 'A' AND (seqschema = '" + schema + "' " //$NON-NLS-1$ //$NON-NLS-2$
								+ "OR (seqschema = 'SYSPUBLIC' AND base_seqschema = '" + schema + "'))"); //$NON-NLS-1$ //$NON-NLS-2$
				CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);

				Collection<ISynonym> sequencesSynonyms = getSynonymsFromResultSet(context, rset,
						monitor);
				synonyms.addAll(sequencesSynonyms);
			} catch (SQLException sqle) {
				LOGGER.error(
						"Unable to fetch sequences synonyms from DB2 server: " + sqle.getMessage(),
						sqle);
			} finally {
				CaptureHelper.safeClose(rset, null);
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch synonyms from DB2 server: " + sqle.getMessage(), sqle);
		} finally {
			CaptureHelper.safeClose(null, stmt);
		}

		return synonyms;
	}

	/**
	 * Returns a <code>Collection</code> of <code>ISynonym</code> objects representing the synonyms
	 * described by each row of the specified <code>ResultSet</code>.
	 * 
	 * @param rset a {@link ResultSet} where each line is a description of a synonym; must not be
	 *        <code>null</code>
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a {@link Collection} of {@link ISynonym} objects if specified <code>ResultSet</code>
	 *         is not empty, an empty <code>Collection</code> otherwise
	 * @throws SQLException if a database access error occurs
	 */
	private Collection<ISynonym> getSynonymsFromResultSet(ICaptureContext context, ResultSet rset,
			IProgressMonitor monitor) throws SQLException {
		Collection<ISynonym> synonyms = new ArrayList<ISynonym>();
		final String schema = context.getSchema();
		while (rset.next()) {
			final boolean isPublic = rset.getBoolean("is_public"); //$NON-NLS-1$
			final String synName = rset.getString("synname"); //$NON-NLS-1$
			final String synDesc = rset.getString("remarks"); //$NON-NLS-1$
			final String synType = rset.getString("syntype"); //$NON-NLS-1$
			final String refDbObjName = rset.getString("refname"); //$NON-NLS-1$
			final String refDbObjSchemaName = rset.getString("refschema"); //$NON-NLS-1$

			if (synName != null && !"".equals(synName.trim())) { //$NON-NLS-1$
				if (LOGGER.isDebugEnabled()) {
					String syscatTableName = null;
					String fieldPrefix = null;
					if (synType.equals(IBasicTable.TYPE_ID)) {
						syscatTableName = "TABLES"; //$NON-NLS-1$
						fieldPrefix = "TAB"; //$NON-NLS-1$
					} else if (synType.equals(ISequence.TYPE_ID)) {
						syscatTableName = "SEQUENCES"; //$NON-NLS-1$
						fieldPrefix = "SEQ"; //$NON-NLS-1$
					}
					String logPrefix = "[" + synName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					String tabPrefix = "SYSCAT." + syscatTableName; //$NON-NLS-1$
					LOGGER.debug("= " + logPrefix + " Synonym Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
					LOGGER.debug(logPrefix + "[" + tabPrefix + ".TYPE] A"); //$NON-NLS-1$ //$NON-NLS-2$
					LOGGER.debug(logPrefix + "[" + tabPrefix + "." + fieldPrefix + "SCHEMA] " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ (isPublic ? "SYSPUBLIC" : schema)); //$NON-NLS-1$
					LOGGER.debug(logPrefix + "[" + tabPrefix + ".BASE_" + fieldPrefix + "NAME] " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ refDbObjName);
					LOGGER.debug(logPrefix + "[" + tabPrefix + ".BASE_" + fieldPrefix + "SCHEMA] " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ (null == refDbObjSchemaName ? schema : refDbObjSchemaName));
					LOGGER.debug(logPrefix + "[" + tabPrefix + ".REMARKS] " + synDesc); //$NON-NLS-1$ //$NON-NLS-2$
				}

				// FIXME [BGA]: Remove this test when the public and referred object type
				// attributes are supported, either by the generic model or the DB2 specific
				// model.
				if (!isPublic && synType.equals(IBasicTable.TYPE_ID)) {
					IVersionable<ISynonym> vSynonym = VersionableFactory
							.createVersionable(ISynonym.class);

					Synonym synonym = (Synonym) vSynonym.getVersionnedObject().getModel();
					synonym.setName(synName);
					synonym.setDescription(synDesc);
					synonym.setRefDbObjSchemaName(refDbObjSchemaName);
					synonym.setRefDbObjName(refDbObjName);

					synonyms.add(synonym);
					CaptureHelper.updateMonitor(monitor, getCounter(), 5, 1);
				}
			}
		}

		return synonyms;
	}

	@Override
	public Collection<ISequence> getSequences(ICaptureContext context, IProgressMonitor monitor) {
		monitor.subTask("Retrieving sequences...");
		Collection<ISequence> sequences = new ArrayList<ISequence>();

		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = ((Connection) context.getConnectionObject()).createStatement();

			rset = stmt
					.executeQuery("SELECT seqname, remarks, start, minvalue, maxvalue, increment, cache, " //$NON-NLS-1$
							+ "DECODE(cycle,'Y',1,0) is_cycle, DECODE(order,'Y',1,0) is_order " //$NON-NLS-1$
							+ "FROM syscat.sequences WHERE seqschema = '" + context.getSchema() + "' AND seqtype <> 'A'"); //$NON-NLS-1$ //$NON-NLS-2$
			CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);

			while (rset.next()) {
				final String seqName = rset.getString("seqname"); //$NON-NLS-1$
				final String seqDesc = rset.getString("remarks"); //$NON-NLS-1$
				final BigDecimal seqStart = rset.getBigDecimal("start"); //$NON-NLS-1$
				final BigDecimal seqMinvalue = rset.getBigDecimal("minvalue"); //$NON-NLS-1$
				final BigDecimal seqMaxvalue = rset.getBigDecimal("maxvalue"); //$NON-NLS-1$

				/*
				 * FIXME [BGA]: The increment value in DB2 dictionary tables is stored as a
				 * DECIMAL(31,0), like the start, min and max values of the sequences. We should
				 * check if it poses a problem with the mapping with a "Long" in the neXtep model
				 * and a "BIGINT" in the neXtep repository.
				 */
				final long seqIncrement = rset.getLong("increment"); //$NON-NLS-1$
				final int seqCacheSize = rset.getInt("cache"); //$NON-NLS-1$
				final boolean isCycle = rset.getBoolean("is_cycle"); //$NON-NLS-1$
				final boolean isOrder = rset.getBoolean("is_order"); //$NON-NLS-1$

				if (seqName != null && !"".equals(seqName.trim())) { //$NON-NLS-1$
					if (LOGGER.isDebugEnabled()) {
						String logPrefix = "[" + seqName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug("= " + logPrefix + " Sequence Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug(logPrefix + "[SYSCAT.SEQUENCES.START] " + seqStart); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.SEQUENCES.MINVALUE] " + seqMinvalue); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.SEQUENCES.MAXVALUE] " + seqMaxvalue); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.SEQUENCES.INCREMENT] " + seqIncrement); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.SEQUENCES.CACHE] " + seqCacheSize); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[SYSCAT.SEQUENCES.CYCLE] " //$NON-NLS-1$
								+ (isCycle ? "Y" : "N")); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug(logPrefix + "[SYSCAT.SEQUENCES.ORDER] " //$NON-NLS-1$
								+ (isOrder ? "Y" : "N")); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug(logPrefix + "[SYSCAT.SEQUENCES.REMARKS] " + seqDesc); //$NON-NLS-1$
					}

					if (seqStart != null && seqMinvalue != null && seqMaxvalue != null) {
						IVersionable<ISequence> v = VersionableFactory
								.createVersionable(ISequence.class);

						ISequence sequence = v.getVersionnedObject().getModel();
						sequence.setName(seqName);
						sequence.setDescription(seqDesc);
						sequence.setStart(seqStart);
						sequence.setMinValue(seqMinvalue);
						sequence.setMaxValue(seqMaxvalue);
						sequence.setIncrement(seqIncrement);
						sequence.setCached(seqCacheSize > 0);
						sequence.setCacheSize(seqCacheSize);
						sequence.setCycle(isCycle);
						sequence.setOrdered(isOrder);

						sequences.add(sequence);
						CaptureHelper.updateMonitor(monitor, getCounter(), 5, 1);
					} else {
						LOGGER.warn("Sequence [" + seqName
								+ "] has been ignored during import because one of its "
								+ "start, minimum or maximum values could not be fetched "
								+ "from database");
					}
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch synonyms from DB2 server: " + sqle.getMessage(), sqle);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return sequences;
	}

}
