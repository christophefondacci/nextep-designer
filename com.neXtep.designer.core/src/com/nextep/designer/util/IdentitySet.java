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
package com.nextep.designer.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.collections.map.IdentityMap;

public class IdentitySet<E> implements Set<E> {
	private IdentityMap elts = new IdentityMap();
	@Override
	public boolean add(E e) {
		if(!elts.containsKey(e)) {
			elts.put(e, Boolean.TRUE);
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		elts.clear();
	}

	@Override
	public boolean contains(Object o) {
		return elts.containsKey(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return elts.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator() {
		return elts.mapIterator();
	}

	@Override
	public boolean remove(Object o) {
		return elts.remove(o)!=null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return elts.size();
	}

	@Override
	public Object[] toArray() {
		return elts.keySet().toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		return (T[])elts.keySet().toArray(a);
	}

}
