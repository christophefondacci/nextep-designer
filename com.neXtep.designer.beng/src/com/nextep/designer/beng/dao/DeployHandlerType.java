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
/**
 *
 */
package com.nextep.designer.beng.dao;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import com.neXtep.shared.model.ArtefactType;


/**
 * @author Christophe Fondacci
 *
 */
public class DeployHandlerType implements UserType {

	private static final int[] SQL_TYPES = new int[]{Types.VARCHAR};

	/**
	 * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
	 */
	@Override
	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	/**
	 * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
	 */
	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	/**
	 * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
	 */
	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable)value;
	}

	/**
	 * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		return (x == y) || (x != null && y != null && (x.equals(y)));
	}

	/**
	 * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
	 */
	@Override
	public int hashCode(Object x) throws HibernateException {
		return (x==null ? 0 : x.hashCode());
	}

	/**
	 * @see org.hibernate.usertype.UserType#isMutable()
	 */
	@Override
	public boolean isMutable() {
		return false;
	}

	/**
	 * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
	 */
	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		String value = rs.getString(names[0]);
		if(value != null) {
			return ArtefactType.valueOf(value);
		}
		return null;
	}

	/**
	 * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
	 */
	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		if(value == null) {
			st.setNull(index,Types.VARCHAR);
		} else {
			st.setString(index,((ArtefactType)value).name());
		}

	}

	/**
	 * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	/**
	 * @see org.hibernate.usertype.UserType#returnedClass()
	 */
	@Override
	public Class returnedClass() {
		return String.class;
	}

	/**
	 * @see org.hibernate.usertype.UserType#sqlTypes()
	 */
	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

}
