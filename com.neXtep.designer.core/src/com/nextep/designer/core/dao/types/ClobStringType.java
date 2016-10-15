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
package com.nextep.designer.core.dao.types;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import oracle.sql.CLOB;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import com.mchange.v2.c3p0.dbms.OracleUtils;
import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.nextep.designer.core.CoreMessages;

/**
 * A hibernate usertype that can map a String value to a CLOB column.
 * 
 * @author Christophe Fondacci
 */
public class ClobStringType implements UserType {

	/** Name of the oracle driver -- used to support Oracle clobs as a special case */
	private static final String ORACLE_DRIVER_NAME = "Oracle JDBC driver"; //$NON-NLS-1$

	/** Version of the oracle driver being supported with clob. */
	private static final int ORACLE_DRIVER_MAJOR_VERSION = 9;
	private static final int ORACLE_DRIVER_MINOR_VERSION = 0;

	/**
	 * @see org.hibernate.usertype.UserType#sqlTypes()
	 */
	public int[] sqlTypes() {
		return new int[] { Types.CLOB };
	}

	/**
	 * @see org.hibernate.usertype.UserType#returnedClass()
	 */
	public Class returnedClass() {
		return String.class;
	}

	/**
	 * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
	 */
	public boolean equals(Object x, Object y) {
		return (x == y) || (x != null && y != null && (x.equals(y)));
	}

	/**
	 * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[],
	 *      java.lang.Object)
	 */
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		Reader clobReader = rs.getCharacterStream(names[0]);
		if (clobReader == null) {
			return null;
		}

		StringBuffer str = new StringBuffer(10240);
		BufferedReader bufferedClobReader = new BufferedReader(clobReader);
		try {
			// 10K buffer
			char[] buffer = new char[10240];
			int bytesRead = 0;
			while ((bytesRead = bufferedClobReader.read(buffer)) >= 0) {
				str.append(buffer, 0, bytesRead);
			}

			bufferedClobReader.close();
		} catch (IOException e) {
			throw new SQLException(e.toString());
		}

		return str.toString();
	}

	/**
	 * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement,
	 *      java.lang.Object, int)
	 */
	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		DatabaseMetaData dbMetaData = st.getConnection().getMetaData();
		if (value == null) {
			st.setNull(index, sqlTypes()[0]);
		} else if (ORACLE_DRIVER_NAME.equals(dbMetaData.getDriverName())) {
			if ((dbMetaData.getDriverMajorVersion() >= ORACLE_DRIVER_MAJOR_VERSION)
					&& (dbMetaData.getDriverMinorVersion() >= ORACLE_DRIVER_MINOR_VERSION)) {
				try {

					// Get the oracle connection class for checking
					Class oracleConnectionClass = Class.forName("oracle.jdbc.OracleConnection"); //$NON-NLS-1$

					Connection conn = st.getConnection();

					// Make sure connection object is right type
					// Create our CLOB
					CLOB tempClob = null;
					if (!oracleConnectionClass.isAssignableFrom(conn.getClass())) {
						if (conn instanceof NewProxyConnection) {
							tempClob = OracleUtils.createTemporaryCLOB(conn, true,
									CLOB.DURATION_SESSION);
						} else {
							throw new HibernateException(
									CoreMessages
											.getString("clobStringType.hibernateException.connectionMustBeOracleConnection") //$NON-NLS-1$
											+ CoreMessages
													.getString("clobStringType.hibernateException.classIs") + conn.getClass().getName()); //$NON-NLS-1$
						}
					} else {
						tempClob = CLOB.createTemporary(conn, true, CLOB.DURATION_SESSION);
					}

					// call open(CLOB.MODE_READWRITE);
					tempClob.open(CLOB.MODE_READWRITE);

					// call the getCharacterOutpitStream method
					Writer tempClobWriter = (Writer) tempClob.getCharacterOutputStream(); // getCharacterOutputStreamMethod.invoke(
					// tempClob, null );

					// write the string to the clob
					tempClobWriter.write((String) value);
					tempClobWriter.flush();
					tempClobWriter.close();

					// get the close method
					tempClob.close();

					// add the clob to the statement
					st.setClob(index, tempClob);
				} catch (IOException e) {
					throw new HibernateException(e.getMessage());
				} catch (ClassNotFoundException e) {
					// could not find the class with reflection
					throw new HibernateException(
							CoreMessages
									.getString("clobStringType.hibernateException.unableToFindClass") //$NON-NLS-1$
									+ "\n" + e.getMessage()); //$NON-NLS-1$
				}
			} else {
				throw new HibernateException(
						CoreMessages.getString("clobStringType.hibernateException.noClobSupport")); //$NON-NLS-1$
			}
		} else {
			String str = (String) value;
			StringReader r = new StringReader(str);
			st.setCharacterStream(index, r, str.length());
		}
	}

	/**
	 * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
	 */
	public Object deepCopy(Object value) {
		if (value == null)
			return null;
		return new String((String) value);
	}

	/**
	 * @see org.hibernate.usertype.UserType#isMutable()
	 */
	public boolean isMutable() {
		return false;
	}

	/**
	 * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
	 */
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	/**
	 * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
	 */
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	/**
	 * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
	 */
	@Override
	public int hashCode(Object x) throws HibernateException {
		return (x == null ? 0 : x.hashCode());
	}

	/**
	 * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object,
	 *      java.lang.Object)
	 */
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

}
