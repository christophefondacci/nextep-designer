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
package com.nextep.installer.services.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextep.installer.InstallerMessages;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.factories.InstallerFactory;
import com.nextep.installer.helpers.Assert;
import com.nextep.installer.helpers.ServicesHelper;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.ICheck;
import com.nextep.installer.model.IDBObject;
import com.nextep.installer.model.IDatabaseObjectCheck;
import com.nextep.installer.model.IDatabaseStructure;
import com.nextep.installer.model.IDatabaseStructureBuilder;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.model.IRelease;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.model.impl.DBObject;
import com.nextep.installer.model.impl.DatabaseStructure;
import com.nextep.installer.model.impl.Delivery;
import com.nextep.installer.model.impl.Release;
import com.nextep.installer.services.IAdminService;
import com.nextep.installer.services.ILoggingService;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class AdminService implements IAdminService {

	public static final int DATE_PADDING = 17;
	public static final int RELEASE_PADDING = 15;
	public static final int MODULE_PADDING = 30;

	private static int latestInstalledReleaseId = 1;

	public boolean check(IInstallConfiguration configuration, IDelivery delivery) {
		if (configuration.isOptionDefined(InstallerOption.FULL_INSTALL)) {
			/*
			 * On full install, we always consider the check as OK to avoid fetching the database
			 * structure all the time. In this case we assume the caller of the install will perform
			 * a full check at the end of the install.
			 */
			return true;
		} else {
			boolean checkOk = true;
			final IDatabaseStructure dbStruct = buildDatabaseStructure(configuration);
			for (ICheck check : delivery.getChecks()) {
				final boolean lastCheck = check.check(dbStruct);
				checkOk = (checkOk && lastCheck);
			}
			return checkOk;
		}
	}

	private IDatabaseStructure buildDatabaseStructure(IInstallConfiguration configuration) {
		final ILoggingService logger = ServicesHelper.getLoggingService();
		final DBVendor vendor = configuration.getTarget().getVendor();
		final Connection conn = configuration.getTargetConnection();
		final IDatabaseStructureBuilder builder = InstallerFactory
				.getDatabaseStructureBuilder(vendor);
		try {
			return builder.buildStructure(null, conn);
		} catch (SQLException e) {
			// Logging in verbose mode
			if (configuration.isOptionDefined(InstallerOption.VERBOSE)) {
				logger.error("Unable to fetch database structure : " + e.getMessage(), e);
			}
			// Returning empty structure
			return new DatabaseStructure();
		}
	}

	public boolean checkAll(IInstallConfiguration configuration, boolean errorsOnly)
			throws InstallerException {
		if (configuration.isOptionDefined(InstallerOption.FULL_INSTALL)) {
			/*
			 * On full install, we always consider the check as OK to avoid fetching the database
			 * structure all the time. In this case we assume the caller of the install will perform
			 * a full check at the end of the install.
			 */
			return true;
		}
		return checkAllForce(configuration, errorsOnly);
	}

	public boolean checkAllForce(IInstallConfiguration configuration, boolean errorsOnly)
			throws InstallerException {
		final ILoggingService logger = ServicesHelper.getLoggingService();
		final Connection conn = configuration.getAdminConnection();
		final IDatabaseTarget target = configuration.getTarget();
		final String user = target.getUser();
		final String database = target.getDatabase();
		final DBVendor vendor = target.getVendor();

		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			StringBuilder buf = new StringBuilder(200);
			buf.append("SELECT " //$NON-NLS-1$
					+ "    r.irel_id " //$NON-NLS-1$
					+ "  , m.module_name " //$NON-NLS-1$
					+ "  , r.rel_major " //$NON-NLS-1$
					+ "  , r.rel_minor " //$NON-NLS-1$
					+ "  , r.rel_iteration " //$NON-NLS-1$
					+ "  , r.rel_patch " //$NON-NLS-1$
					+ "  , r.rel_revision " //$NON-NLS-1$
					+ "FROM nadm_installed_releases r " //$NON-NLS-1$
					+ "  INNER JOIN nadm_modules m " //$NON-NLS-1$
					+ "    ON r.module_refid = m.module_refid " //$NON-NLS-1$
					+ "WHERE r.last = 'Y' "); //$NON-NLS-1$

			// Restricting owner and database for standalone admins only
			if (!configuration.isAdminInTarget()) {
				buf.append("  AND UPPER(r.owner) = UPPER(?) " //$NON-NLS-1$
						+ "  AND UPPER(r.owner_database) = UPPER(?) "); //$NON-NLS-1$
			}

			// Retrieving currently installed modules from admin schema
			stmt = conn.prepareStatement(buf.toString());
			// Setting parameters user / database for standalone admins only
			if (!configuration.isAdminInTarget()) {
				stmt.setString(1, user);
				stmt.setString(2, database);
			}
			rset = stmt.executeQuery();

			// Building release list
			List<IDelivery> deliveries = new ArrayList<IDelivery>();
			while (rset.next()) {
				final IRelease rel = new Release();
				rel.setId(rset.getLong(1));
				rel.setMajor(rset.getInt(3));
				rel.setMinor(rset.getInt(4));
				rel.setIteration(rset.getInt(5));
				rel.setPatch(rset.getInt(6));
				rel.setRevision(rset.getInt(7));
				final IDelivery dlv = new Delivery(false);
				dlv.setName(rset.getString(2));
				dlv.setRelease(rel);
				dlv.setDBVendor(vendor);
				deliveries.add(dlv);
			}
			rset.close();

			// For each release, we retrieve and process checks
			boolean checkSuccess = true;
			Map<IDelivery, List<IDBObject>> missingMap = new HashMap<IDelivery, List<IDBObject>>();
			// Fetching database structure
			final IDatabaseStructure dbStruct = buildDatabaseStructure(configuration);
			for (IDelivery dlv : deliveries) {
				// Retrieving check
				ICheck check = getReleaseCheck(configuration, dlv.getRelease());
				// Checking
				final boolean success = check.check(dbStruct);
				checkSuccess = checkSuccess && success;
				// Updating missing objects
				missingMap.put(dlv, ((IDatabaseObjectCheck) check).getMissingObjects());
			}

			// Displaying results
			if (deliveries.size() > 0 && (!errorsOnly || !checkSuccess)) {
				logger.log(""); //$NON-NLS-1$
				logger.log(""); //$NON-NLS-1$
				logger.out(
						InstallerMessages.getString("service.admin.structureCheckColumn"), MODULE_PADDING); //$NON-NLS-1$
				logger.out(
						InstallerMessages.getString("service.admin.releaseColumn"), RELEASE_PADDING); //$NON-NLS-1$
				logger.log(InstallerMessages.getString("service.admin.statusColumn")); //$NON-NLS-1$
				logger.out(logger.getSeparator('-', MODULE_PADDING - 1), MODULE_PADDING);
				logger.out(logger.getSeparator('-', RELEASE_PADDING - 1), RELEASE_PADDING);
				logger.log(logger.getSeparator('-', checkSuccess ? 8 : 60));
				for (IDelivery dlv : missingMap.keySet()) {
					final List<IDBObject> missing = missingMap.get(dlv);
					boolean failed = false;
					for (IDBObject obj : missing) {
						logger.out(dlv.getName(), MODULE_PADDING);
						logger.out(dlv.getRelease().toString(), RELEASE_PADDING);
						logger.log(MessageFormat.format(InstallerMessages
								.getString("service.admin.missingObject"), obj.getType(), obj //$NON-NLS-1$
								.getName()));
						failed = true;
					}
					if (!failed) {
						logger.out(dlv.getName(), MODULE_PADDING);
						logger.out(dlv.getRelease().toString(), RELEASE_PADDING);
						logger.log("OK"); //$NON-NLS-1$
					}
				}
				logger.log(""); //$NON-NLS-1$
			}

			return checkSuccess;
		} catch (SQLException e) {
			throw new InstallerException(
					InstallerMessages.getString("service.admin.structureCheckFailException") //$NON-NLS-1$
							+ e.getMessage(), e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				throw new InstallerException(
						InstallerMessages.getString("service.admin.structureCheckFailClose") //$NON-NLS-1$
								+ e.getMessage(), e);
			}
		}
	}

	public IRelease getRelease(IInstallConfiguration configuration, IDelivery delivery)
			throws InstallerException {
		return getRelease(configuration, configuration.getAdminConnection(), delivery.getRefUID(),
				true);
	}

	public IRelease getRelease(IInstallConfiguration configuration, long moduleRefId, boolean raise)
			throws InstallerException {
		return getRelease(configuration, configuration.getAdminConnection(), moduleRefId, raise);
	}

	public IRelease getRelease(IInstallConfiguration configuration, Connection conn,
			long moduleRefId, boolean raise) throws InstallerException {
		final ILoggingService logger = ServicesHelper.getLoggingService();

		PreparedStatement stmt = null;
		ResultSet rset = null;
		final IDatabaseTarget target = configuration.getTarget();

		try {
			StringBuilder buf = new StringBuilder(500);
			buf.append("SELECT " //$NON-NLS-1$
					+ "    m.module_name " //$NON-NLS-1$
					+ "  , r.rel_major " //$NON-NLS-1$
					+ "  , r.rel_minor " //$NON-NLS-1$
					+ "  , r.rel_iteration " //$NON-NLS-1$
					+ "  , r.rel_patch " //$NON-NLS-1$
					+ "  , r.rel_revision " //$NON-NLS-1$
					+ "  , r.irel_id " //$NON-NLS-1$
					+ "FROM nadm_modules m " //$NON-NLS-1$
					+ "  INNER JOIN nadm_installed_releases r " //$NON-NLS-1$
					+ "    ON r.module_refid = m.module_refid " //$NON-NLS-1$
					+ "WHERE m.module_refid = ? " //$NON-NLS-1$
					+ "  AND r.last = 'Y' "); //$NON-NLS-1$

			// Restricting owner and database for standalone admins ONLY
			if (!configuration.isAdminInTarget() && moduleRefId != 99999999999L) {
				buf.append("  AND UPPER(r.owner) = UPPER(?) " //$NON-NLS-1$
						+ "  AND UPPER(r.owner_database) = UPPER(?) "); //$NON-NLS-1$
			}
			stmt = conn.prepareStatement(buf.toString());
			stmt.setLong(1, moduleRefId);

			// Setting owner and database for standalone admins
			if (!configuration.isAdminInTarget() && moduleRefId != 99999999999L) {
				stmt.setString(2, target.getUser());
				stmt.setString(3, target.getDatabase());
			}
			rset = stmt.executeQuery();

			/*
			 * Fix of bug INS-12: fetching all matched lines, only take the highest release because
			 * of potential collisions due to case-insensitivity.
			 */
			IRelease maxRel = null;
			while (rset.next()) {
				IRelease rel = new Release();
				rel.setMajor(rset.getInt(2));
				rel.setMinor(rset.getInt(3));
				rel.setIteration(rset.getInt(4));
				rel.setPatch(rset.getInt(5));
				rel.setRevision(rset.getInt(6));
				rel.setId(rset.getLong(7));
				if (maxRel == null || rel.compareTo(maxRel) > 0) {
					maxRel = rel;
				}
			}

			/*
			 * #INS-28 : Emulating a connection with auto-commit=true to avoid the creation of a
			 * blocking transaction that freezes the installation when trying to alter the tables
			 * NADM_MODULES or NADM_INSTALLED_RELEASES.
			 */
			if (!conn.getAutoCommit()) {
				conn.commit();
			}
			return maxRel;
		} catch (SQLException e) {
			if (!raise) {
				return null; // Compatibility with command line installer
			} else {
				throw new InstallerException(
						InstallerMessages.getString("service.admin.getReleaseFailException"), e); //$NON-NLS-1$
			}
		} finally {
			try {
				if (rset != null) {
					rset.close();
				}
			} catch (SQLException e) {
				logger.error(InstallerMessages.getString("service.admin.closeResultSetException"), //$NON-NLS-1$
						e);
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error(InstallerMessages.getString("service.admin.closeStatementException"), //$NON-NLS-1$
						e);
			}
		}
	}

	public void installInitialRelease(IInstallConfiguration configuration, IDelivery delivery)
			throws InstallerException {
		installRelease(configuration, delivery, delivery.getFromRelease(), true);
	}

	public void installRelease(IInstallConfiguration configuration, IDelivery delivery,
			boolean success) throws InstallerException {
		installRelease(configuration, delivery, delivery.getRelease(), success);
	}

	private void installRelease(IInstallConfiguration configuration, IDelivery delivery,
			IRelease release, boolean success) throws InstallerException {
		final Connection conn = configuration.getAdminConnection();
		final IDatabaseTarget target = configuration.getTarget();
		final String owner = target.getUser();
		final String database = target.getDatabase();

		// Assertions
		Assert.notNull(delivery, InstallerMessages.getString("service.admin.nullDeliveryError")); //$NON-NLS-1$
		Assert.notNull(delivery.getName(),
				InstallerMessages.getString("service.admin.nullNameError")); //$NON-NLS-1$
		PreparedStatement stmt = null;
		ResultSet rset = null;
		boolean initialConnectionAutocommit = true;
		try {
			initialConnectionAutocommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			/*
			 * Columns names in the SET clause cannot be qualified with an alias name because it
			 * would fail in Postgres.
			 */
			stmt = conn.prepareStatement("UPDATE nadm_modules m " //$NON-NLS-1$
					+ "  SET module_name = ? " //$NON-NLS-1$
					+ "    , udate = ? " //$NON-NLS-1$
					+ "WHERE m.module_refid = ? "); //$NON-NLS-1$
			stmt.setString(1, delivery.getName());
			stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			stmt.setLong(3, delivery.getRefUID());
			stmt.execute();

			// Check whether we have updated something
			if (stmt.getUpdateCount() == 0) {
				stmt.close();
				// If not, we insert the new module
				stmt = conn.prepareStatement("INSERT INTO nadm_modules ( " //$NON-NLS-1$ 
						+ "    module_refid " //$NON-NLS-1$
						+ "  , module_name " //$NON-NLS-1$
						+ "  , cdate " //$NON-NLS-1$
						+ "  , udate " //$NON-NLS-1$
						+ ") VALUES ( " //$NON-NLS-1$
						+ "  ?, ?, ?, ? " //$NON-NLS-1$
						+ ") "); //$NON-NLS-1$
				stmt.setLong(1, delivery.getRefUID());
				stmt.setString(2, delivery.getName());
				stmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
				stmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
				stmt.execute();
			}
			stmt.close();

			/*
			 * Manually computing next ID: Really cannot understand why MySQL does not return a
			 * non-null ID on the second admin install.
			 */
			long relId = latestInstalledReleaseId++;
			stmt = conn.prepareStatement("SELECT (MAX(irel_id) + 1) AS rel_id " //$NON-NLS-1$
					+ "FROM nadm_installed_releases "); //$NON-NLS-1$
			rset = stmt.executeQuery();
			if (rset.next()) {
				// Only taking the db id if > 0
				final long fetchedRelId = rset.getLong(1);
				if (fetchedRelId > 0) {
					relId = fetchedRelId;
				}
			}
			rset.close();
			stmt.close();

			// Unflag the last installed release (may update 0 line for new
			// modules)
			// Bug INS-12: Handling case-insensitivity for unflagging releases
			StringBuilder buf = new StringBuilder(200);

			/*
			 * Columns names in the SET clause cannot be qualified with an alias name because it
			 * would fail in Postgres.
			 */
			buf.append("UPDATE nadm_installed_releases r " //$NON-NLS-1$
					+ "  SET last = 'N' " //$NON-NLS-1$
					+ "WHERE r.module_refid = ? " //$NON-NLS-1$
					+ "  AND r.last = 'Y' "); //$NON-NLS-1$

			// Restricting owner & database for standalone admins ONLY
			if (!configuration.isAdminInTarget()) {
				buf.append("  AND UPPER(r.owner) = UPPER(?) " //$NON-NLS-1$
						+ "  AND UPPER(r.owner_database) = UPPER(?) "); //$NON-NLS-1$
			}

			stmt = conn.prepareStatement(buf.toString());
			stmt.setLong(1, delivery.getRefUID());

			// Registering owner & database info for standalone admins
			if (!configuration.isAdminInTarget()) {
				stmt.setString(2, owner);
				stmt.setString(3, database);
			}
			stmt.execute();
			stmt.close();

			// Installs the new release
			stmt = conn.prepareStatement("INSERT INTO nadm_installed_releases ( " //$NON-NLS-1$
					+ "    module_refid " //$NON-NLS-1$
					+ "  , rel_major " //$NON-NLS-1$
					+ "  , rel_minor " //$NON-NLS-1$
					+ "  , rel_iteration " //$NON-NLS-1$
					+ "  , rel_patch " //$NON-NLS-1$
					+ "  , rel_revision " //$NON-NLS-1$
					+ "  , owner " //$NON-NLS-1$
					+ "  , owner_database " //$NON-NLS-1$
					+ "  , status " //$NON-NLS-1$
					+ "  , irel_id " //$NON-NLS-1$
					+ "  , cdate " //$NON-NLS-1$
					+ ") VALUES ( " //$NON-NLS-1$
					+ "  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? " //$NON-NLS-1$
					+ ") "); //$NON-NLS-1$
			setReleaseInputs(configuration, delivery, stmt);
			stmt.setString(9, success ? "OK" : "FAILED"); //$NON-NLS-1$ //$NON-NLS-2$
			stmt.setLong(10, relId);
			// Setting current date
			stmt.setDate(11, new java.sql.Date(System.currentTimeMillis()));
			stmt.execute();
			stmt.close();
			// Setting release ID
			delivery.getRelease().setId(relId);
			rset.close();
			stmt.close();
			installChecks(configuration);
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				throw new InstallerException(MessageFormat.format(
						InstallerMessages.getString("service.admin.installReleaseFail"), //$NON-NLS-1$
						ex.getMessage()), ex);
			}
			throw new InstallerException(MessageFormat.format(
					InstallerMessages.getString("service.admin.installReleaseFail"), //$NON-NLS-1$
					e.getMessage()), e);
		} finally {
			try {
				if (rset != null) {
					rset.close();
				}
			} catch (SQLException e) {
				throw new InstallerException(
						MessageFormat.format(InstallerMessages
								.getString("service.admin.closeReleaseResultSetException"), e //$NON-NLS-1$
								.getMessage()), e);
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				throw new InstallerException(
						MessageFormat.format(InstallerMessages
								.getString("service.admin.closeReleaseStatementException"), e //$NON-NLS-1$
								.getMessage()), e);
			}
			try {
				conn.setAutoCommit(initialConnectionAutocommit);
			} catch (SQLException e) {
				// Skipping
			}
		}
	}

	private void setReleaseInputs(IInstallConfiguration configuration, IDelivery delivery,
			PreparedStatement stmt) throws SQLException {
		final IDatabaseTarget target = configuration.getTarget();
		final String owner = target.getUser();
		final String database = target.getDatabase();
		stmt.setLong(1, delivery.getRefUID());
		stmt.setInt(2, getRelease(delivery).getMajor());
		stmt.setInt(3, getRelease(delivery).getMinor());
		stmt.setInt(4, getRelease(delivery).getIteration());
		stmt.setInt(5, getRelease(delivery).getPatch());
		stmt.setInt(6, getRelease(delivery).getRevision());
		stmt.setString(7, owner);
		stmt.setString(8, database);
	}

	/**
	 * Define the release to install, given the delivery.
	 * 
	 * @param delivery delivery currently processed
	 * @return the release to install
	 */
	protected IRelease getRelease(IDelivery delivery) {
		return delivery.getRelease();
	}

	/**
	 * Installs checks for the new release in database admin.
	 * 
	 * @param conn admin connection
	 */
	protected void installChecks(IInstallConfiguration configuration) throws InstallerException {
		final Connection conn = configuration.getAdminConnection();
		final IDelivery delivery = configuration.getDelivery();
		final String owner = configuration.getTarget().getUser();
		Assert.notNull(delivery, "Cannot find delivery to install"); //$NON-NLS-1$

		// Installing checks
		if (delivery.getChecks() != null) {
			for (ICheck check : delivery.getChecks()) {
				check.install(conn, owner, getRelease(delivery));
			}
		}
	}

	public void showInstalledReleases(IInstallConfiguration configuration)
			throws InstallerException {
		final ILoggingService logger = ServicesHelper.getLoggingService();

		final Connection conn = configuration.getAdminConnection();
		final IDatabaseTarget target = configuration.getTarget();
		Assert.notNull(target,
				"You need to specify the target database to see installed modules.\n" //$NON-NLS-1$
						+ "Configure the connection through command line options " //$NON-NLS-1$
						+ "or enter \"--help\" to see all options available."); //$NON-NLS-1$
		final String user = target.getUser();
		final String database = target.getDatabase();

		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			StringBuilder buf = new StringBuilder(500);
			buf.append("SELECT " //$NON-NLS-1$
					+ "    m.module_name " //$NON-NLS-1$
					+ "  , r.rel_major " //$NON-NLS-1$ 
					+ "  , r.rel_minor " //$NON-NLS-1$
					+ "  , r.rel_iteration " //$NON-NLS-1$
					+ "  , r.rel_patch " //$NON-NLS-1$
					+ "  , r.rel_revision " //$NON-NLS-1$
					+ "  , r.cdate " //$NON-NLS-1$
					+ "FROM nadm_modules m " //$NON-NLS-1$
					+ "  INNER JOIN nadm_installed_releases r " //$NON-NLS-1$
					+ "    ON r.module_refid = m.module_refid " //$NON-NLS-1$
					+ "WHERE r.last = 'Y' "); //$NON-NLS-1$

			if (!configuration.isAdminInTarget()) {
				buf.append("  AND UPPER(r.owner) = UPPER(?) " //$NON-NLS-1$
						+ "  AND UPPER(r.owner_database) = UPPER(?) "); //$NON-NLS-1$
			}
			stmt = conn.prepareStatement(buf.toString());
			if (!configuration.isAdminInTarget()) {
				stmt.setString(1, user);
				stmt.setString(2, database);
			}
			rset = stmt.executeQuery();

			// Displaying headers
			logger.out(InstallerMessages.getString("service.admin.moduleNameCol"), MODULE_PADDING); //$NON-NLS-1$
			logger.out(InstallerMessages.getString("service.admin.releaseCol"), RELEASE_PADDING); //$NON-NLS-1$
			logger.log(InstallerMessages.getString("service.admin.installDateCol")); //$NON-NLS-1$
			logger.out(logger.getSeparator('-', MODULE_PADDING - 1), MODULE_PADDING);
			logger.out(logger.getSeparator('-', RELEASE_PADDING - 1), RELEASE_PADDING);
			logger.log(logger.getSeparator('-', DATE_PADDING - 1));
			SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy hh:mm"); //$NON-NLS-1$
			while (rset.next()) {
				logger.out(rset.getString(1), MODULE_PADDING);
				IRelease rel = new Release(rset.getInt(2), rset.getInt(3), rset.getInt(4),
						rset.getInt(5), rset.getInt(6));
				logger.out(rel.toString(), RELEASE_PADDING);

				/*
				 * FIXME [BGA] Captured data type changed from Timestamp to Date for MSSQL database.
				 * Run regression tests on other supported databases to check if this fix is
				 * compatible with other vendors.
				 */
				//
				try {
					logger.log(f.format(rset.getDate(7)));
				} catch (SQLException e) {
					try {
						logger.log(f.format(rset.getTimestamp(7)));
					} catch (SQLException ex) {
						logger.log(InstallerMessages.getString("service.admin.unknownDate")); //$NON-NLS-1$
					}
				}
			}
		} catch (SQLException e) {
			throw new InstallerException(MessageFormat.format(
					InstallerMessages.getString("service.admin.showInstalledReleasesFail"), e //$NON-NLS-1$
							.getMessage()), e);
		} finally {
			try {
				if (rset != null) {
					rset.close();
				}
			} catch (SQLException e) {
				throw new InstallerException(MessageFormat.format(
						"Unable to close resultset while fetching installed release info: {0}", //$NON-NLS-1$
						e.getMessage()), e);
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				throw new InstallerException(MessageFormat.format(
						"Unable to close statement while fetching installed release info: {0}", //$NON-NLS-1$
						e.getMessage()), e);
			}
		}
	}

	public ICheck getReleaseCheck(IInstallConfiguration configuration, IRelease release)
			throws InstallerException {
		final Connection conn = configuration.getAdminConnection();
		final IDatabaseTarget target = configuration.getTarget();
		final DBVendor vendor = target.getVendor();

		// Preparing returned list
		IDatabaseObjectCheck check = InstallerFactory.buildDatabaseObjectCheckerFor(vendor);

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement("SELECT " //$NON-NLS-1$
					+ "    ro.object_type " //$NON-NLS-1$
					+ "  , ro.object_name " //$NON-NLS-1$
					+ "FROM nadm_release_objects ro " //$NON-NLS-1$
					+ "WHERE ro.irel_id = ? "); //$NON-NLS-1$
			stmt.setLong(1, release.getId());
			ResultSet rset = stmt.executeQuery();

			while (rset.next()) {
				DBObject dbObj = new DBObject(rset.getString(1), rset.getString(2));
				check.addObject(dbObj);
			}
			rset.close();
		} catch (SQLException e) {
			throw new InstallerException(MessageFormat.format(
					InstallerMessages.getString("service.admin.getReleaseChecksFail"), //$NON-NLS-1$
					e.getMessage()), e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				throw new InstallerException(MessageFormat.format(
						"Unable to close statement after check retrieval: {0}", e.getMessage()), e); //$NON-NLS-1$
			}
		}
		return check;
	}

	/**
	 * Service injection setter, used when the installer is invoked from neXtep designer IDE through
	 * DS injection. This setter registers the service globally on the {@link NextepInstaller}
	 * static bean for compatibility with standalone mode.
	 * 
	 * @param service logging service implementation
	 */
	public void setLoggingService(ILoggingService service) {
		NextepInstaller.registerService(ILoggingService.class, service);
	}

}
