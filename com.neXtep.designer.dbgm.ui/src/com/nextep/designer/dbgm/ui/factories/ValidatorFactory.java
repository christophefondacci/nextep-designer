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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

/**
 * This class is a factory of validators to be used with the data binding framework. This factory
 * provides the validators we need for common use cases.
 * 
 * @author Christophe Fondacci
 */
public final class ValidatorFactory {

	private ValidatorFactory() {
	}

	/**
	 * Creates a validator which validates that the input string could properly be converted into an
	 * integer value, else it returns the appropriate error status message.
	 * 
	 * @return the integer {@link IValidator}
	 */
	public static IValidator createIntegerValidator() {
		return new IValidator() {

			@Override
			public IStatus validate(Object value) {
				String s = String.valueOf(value);
				try {
					if (s != null && !"".equals(s.trim())) {
						NumberFormat.getIntegerInstance().parse(s);
					}
				} catch (ParseException e) {
					return ValidationStatus.error("Cannot convert '" + s
							+ "' into an integer value : " + e.getMessage());
				}
				return ValidationStatus.ok();
			}
		};
	}

	/**
	 * Creates a validator which validates that the input string could properly be used as a neXtep
	 * name, else it returns the appropriate error status message.
	 * 
	 * @param allowEmpty whether or not the validator allows empty name
	 * @return the integer {@link IValidator}
	 */
	public static IValidator createNameValidator(final boolean allowEmpty) {
		return new IValidator() {

			@Override
			public IStatus validate(Object value) {
				String s = String.valueOf(value);
				if (s.indexOf(' ') >= 0) {
					return ValidationStatus.error("Names cannot contain spaces");
				} else if (s.trim().isEmpty() && !allowEmpty) {
					return ValidationStatus.error("A name should be defined for this element");
				}
				return ValidationStatus.ok();
			}
		};
	}

	/**
	 * Creates a validator which enforces the non-emptiness of a value, and can also check that the
	 * value is a numeric value.
	 * 
	 * @param propertyName name of the property which this validator controls. It is used by error
	 *        messages.
	 * @param isNumeric whether or not the validator should check that the value is numeric
	 * @return the corresponding {@link IValidator}
	 */
	public static IValidator createNotEmptyValidator(final String propertyName,
			final boolean isNumeric) {
		return new IValidator() {

			@Override
			public IStatus validate(Object value) {
				String s = String.valueOf(value);
				if (s == null || "".equals(s.trim())) {
					return ValidationStatus.error(propertyName + " should not be empty.");
				} else if (isNumeric) {
					try {
						NumberFormat.getNumberInstance(Locale.ENGLISH).parse(s);
					} catch (ParseException e) {
						return ValidationStatus.error(propertyName
								+ " should be defined as a numeric value.");
					}
				}

				return ValidationStatus.ok();
			}
		};
	}
}
