/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.nextep.designer.diagram.model.impl;

import com.nextep.designer.diagram.model.Column;
import com.nextep.designer.diagram.model.Constraint;
import com.nextep.designer.diagram.model.ModelPackage;
import com.nextep.designer.diagram.model.Table;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Table</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link com.nextep.designer.diagram.model.impl.TableImpl#getColumns <em>Columns</em>}</li>
 *   <li>{@link com.nextep.designer.diagram.model.impl.TableImpl#getName <em>Name</em>}</li>
 *   <li>{@link com.nextep.designer.diagram.model.impl.TableImpl#getSourceConstraints <em>Source Constraints</em>}</li>
 *   <li>{@link com.nextep.designer.diagram.model.impl.TableImpl#getTargetConstraint <em>Target Constraint</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TableImpl extends EObjectImpl implements Table {
	/**
	 * The cached value of the '{@link #getColumns() <em>Columns</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumns()
	 * @generated
	 * @ordered
	 */
	protected EList<Column> columns;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getSourceConstraints() <em>Source Constraints</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceConstraints()
	 * @generated
	 * @ordered
	 */
	protected EList<Constraint> sourceConstraints;

	/**
	 * The cached value of the '{@link #getTargetConstraint() <em>Target Constraint</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetConstraint()
	 * @generated
	 * @ordered
	 */
	protected EList<Constraint> targetConstraint;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TableImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.TABLE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Column> getColumns() {
		if (columns == null) {
			columns = new EObjectContainmentEList<Column>(Column.class, this, ModelPackage.TABLE__COLUMNS);
		}
		return columns;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.TABLE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Constraint> getSourceConstraints() {
		if (sourceConstraints == null) {
			sourceConstraints = new EObjectContainmentEList<Constraint>(Constraint.class, this, ModelPackage.TABLE__SOURCE_CONSTRAINTS);
		}
		return sourceConstraints;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Constraint> getTargetConstraint() {
		if (targetConstraint == null) {
			targetConstraint = new EObjectContainmentEList<Constraint>(Constraint.class, this, ModelPackage.TABLE__TARGET_CONSTRAINT);
		}
		return targetConstraint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelPackage.TABLE__COLUMNS:
				return ((InternalEList<?>)getColumns()).basicRemove(otherEnd, msgs);
			case ModelPackage.TABLE__SOURCE_CONSTRAINTS:
				return ((InternalEList<?>)getSourceConstraints()).basicRemove(otherEnd, msgs);
			case ModelPackage.TABLE__TARGET_CONSTRAINT:
				return ((InternalEList<?>)getTargetConstraint()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.TABLE__COLUMNS:
				return getColumns();
			case ModelPackage.TABLE__NAME:
				return getName();
			case ModelPackage.TABLE__SOURCE_CONSTRAINTS:
				return getSourceConstraints();
			case ModelPackage.TABLE__TARGET_CONSTRAINT:
				return getTargetConstraint();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelPackage.TABLE__COLUMNS:
				getColumns().clear();
				getColumns().addAll((Collection<? extends Column>)newValue);
				return;
			case ModelPackage.TABLE__NAME:
				setName((String)newValue);
				return;
			case ModelPackage.TABLE__SOURCE_CONSTRAINTS:
				getSourceConstraints().clear();
				getSourceConstraints().addAll((Collection<? extends Constraint>)newValue);
				return;
			case ModelPackage.TABLE__TARGET_CONSTRAINT:
				getTargetConstraint().clear();
				getTargetConstraint().addAll((Collection<? extends Constraint>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ModelPackage.TABLE__COLUMNS:
				getColumns().clear();
				return;
			case ModelPackage.TABLE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ModelPackage.TABLE__SOURCE_CONSTRAINTS:
				getSourceConstraints().clear();
				return;
			case ModelPackage.TABLE__TARGET_CONSTRAINT:
				getTargetConstraint().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ModelPackage.TABLE__COLUMNS:
				return columns != null && !columns.isEmpty();
			case ModelPackage.TABLE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ModelPackage.TABLE__SOURCE_CONSTRAINTS:
				return sourceConstraints != null && !sourceConstraints.isEmpty();
			case ModelPackage.TABLE__TARGET_CONSTRAINT:
				return targetConstraint != null && !targetConstraint.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(')');
		return result.toString();
	}

} //TableImpl
