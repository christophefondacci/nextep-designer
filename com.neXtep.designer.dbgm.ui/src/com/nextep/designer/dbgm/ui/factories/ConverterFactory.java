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
package com.nextep.designer.dbgm.ui.factories;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.NumberToStringConverter;
import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import com.nextep.datadesigner.dbgm.model.ForeignKeyAction;
import com.nextep.datadesigner.dbgm.model.LengthType;
import com.nextep.designer.dbgm.model.PartitioningMethod;

/**
 * This class is a factory of converters, thus allowing factorization of the converter definition
 * for the data binding framework when dealing with common use cases.
 * 
 * @author Christophe Fondacci
 */
public final class ConverterFactory {

	private ConverterFactory() {
	}

	/**
	 * Createds a new String to integer converter
	 * 
	 * @param rawType indicates whether the integer value is a raw type or boxed integer class type.
	 * @param zeroMeansEmpty indicates whether empty string should be converted to zero integer
	 *        value or not
	 * @return the corresponding {@link IConverter}
	 */
	public static IConverter createToIntegerConverter(final boolean rawType,
			final boolean zeroMeansEmpty) {
		return new IConverter() {

			private final IConverter baseConverter = StringToNumberConverter.toInteger(rawType);

			@Override
			public Object getToType() {
				return baseConverter.getToType();
			}

			@Override
			public Object getFromType() {
				return baseConverter.getFromType();
			}

			@Override
			public Object convert(Object fromObject) {
				if (zeroMeansEmpty) {
					final String fromString = (String) fromObject;
					if (fromString == null || fromString.isEmpty()) {
						return 0;
					}
				}
				return baseConverter.convert(fromObject);
			}
		};
	}

	/**
	 * Creates a new integer to string converter.
	 * 
	 * @param rawType indicates whether the integer value is a raw type or boxed integer class type.
	 * @param zeroMeansEmpty indicates whether empty string should be converted to zero integer
	 *        value or not
	 * @return the corresponding {@link IConverter}
	 */
	public static IConverter createFromIntegerConverter(final boolean rawType,
			final boolean zeroMeansEmpty) {
		return new IConverter() {

			private final IConverter baseConverter = NumberToStringConverter.fromInteger(rawType);

			@Override
			public Object getToType() {
				return baseConverter.getToType();
			}

			@Override
			public Object getFromType() {
				return baseConverter.getFromType();
			}

			@Override
			public Object convert(Object fromObject) {
				if (zeroMeansEmpty) {
					final Integer fromInteger = (Integer) fromObject;
					if (fromInteger == null || fromInteger <= 0) {
						return "";
					}
				}
				return baseConverter.convert(fromObject);
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

	public static IConverter createLengthTypeTargetConverter() {
		return new IConverter() {

			@Override
			public Object getToType() {
				return String.class;
			}

			@Override
			public Object getFromType() {
				return LengthType.class;
			}

			@Override
			public Object convert(Object fromObject) {
				return fromObject == null ? LengthType.UNDEFINED.name() : ((LengthType) fromObject)
						.name();
			}
		};
	}

	public static IConverter createLengthTypeModelConverter() {
		return new IConverter() {

			@Override
			public Object getToType() {
				return LengthType.class;
			}

			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object convert(Object fromObject) {
				return fromObject == null || "".equals(((String) fromObject).trim()) ? LengthType.UNDEFINED //$NON-NLS-1$
						: LengthType.valueOf((String) fromObject);
			}
		};
	}

	public static IConverter createForeignKeyActionTargetConverter() {
		return new IConverter() {

			@Override
			public Object getToType() {
				return String.class;
			}

			@Override
			public Object getFromType() {
				return ForeignKeyAction.class;
			}

			@Override
			public Object convert(Object fromObject) {
				return fromObject == null ? "" : ((ForeignKeyAction) fromObject).getLabel();
			}
		};
	}

	public static IConverter createForeignKeyActionModelConverter() {
		return new IConverter() {

			@Override
			public Object getToType() {
				return ForeignKeyAction.class;
			}

			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object convert(Object fromObject) {
				for (ForeignKeyAction action : ForeignKeyAction.values()) {
					if (action.getLabel().equals(fromObject)) {
						return action;
					}
				}
				return null;
			}
		};
	}
}
