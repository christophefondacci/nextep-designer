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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.oracle.ui.factories;

import org.eclipse.core.databinding.conversion.IConverter;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.oracle.model.PhysicalOrganisation;

/**
 * @author Christophe Fondacci
 */
public final class OracleConverterFactory {

	private OracleConverterFactory() {

	}

	public static IConverter createPhysicalOrganizationTargetConverter() {
		return new IConverter() {

			@Override
			public Object getToType() {
				return String.class;
			}

			@Override
			public Object getFromType() {
				return PhysicalOrganisation.class;
			}

			@Override
			public Object convert(Object fromObject) {
				return ((PhysicalOrganisation) fromObject).getLabel();
			}
		};
	}

	public static IConverter createPhysicalOrganizationModelConverter() {
		return new IConverter() {

			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object getToType() {
				return PhysicalOrganisation.class;
			}

			@Override
			public Object convert(Object fromObject) {
				for (PhysicalOrganisation o : PhysicalOrganisation.values()) {
					if (o.getLabel().equals(fromObject)) {
						return o;
					}
				}
				return null;
			}
		};
	}

	public static IConverter createPartitioningMethodTargetConverter() {
		return new IConverter() {

			@Override
			public Object getToType() {
				return String.class;
			}

			@Override
			public Object getFromType() {
				return PartitioningMethod.class;
			}

			@Override
			public Object convert(Object fromObject) {
				return fromObject == null ? PartitioningMethod.NONE.name()
						: ((PartitioningMethod) fromObject).name();
			}
		};
	}

	public static IConverter createPartitioningMethodModelConverter() {
		return new IConverter() {

			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object getToType() {
				return PartitioningMethod.class;
			}

			@Override
			public Object convert(Object fromObject) {
				return fromObject == null || "".equals(((String) fromObject).trim()) ? PartitioningMethod.NONE //$NON-NLS-1$
						: PartitioningMethod.valueOf((String) fromObject);
			}
		};
	}

	public static <T extends Enum<T>> IConverter createGenericEnumModelConverter(
			final Class<T> enumClass) {
		return new IConverter() {

			@Override
			public Object getToType() {
				return enumClass;
			}

			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object convert(Object fromObject) {
				return Enum.valueOf(enumClass, (String) fromObject);
			}
		};
	}

	public static <T extends Enum<T>> IConverter createGenericEnumTargetConverter(
			final Class<T> enumClass) {
		return new IConverter() {

			@Override
			public Object getToType() {
				return String.class;
			}

			@Override
			public Object getFromType() {
				return enumClass;
			}

			@Override
			public Object convert(Object fromObject) {
				return ((T) fromObject).name();
			}
		};
	}

}
