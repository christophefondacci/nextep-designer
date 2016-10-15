/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.nextep.designer.diagram.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Constraint</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.nextep.designer.diagram.model.Constraint#getSource <em>Source</em>}</li>
 *   <li>{@link com.nextep.designer.diagram.model.Constraint#getTarget <em>Target</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.nextep.designer.diagram.model.ModelPackage#getConstraint()
 * @model
 * @generated
 */
public interface Constraint extends EObject {
	/**
	 * Returns the value of the '<em><b>Source</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Source</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source</em>' reference.
	 * @see #setSource(Table)
	 * @see com.nextep.designer.diagram.model.ModelPackage#getConstraint_Source()
	 * @model
	 * @generated
	 */
	Table getSource();

	/**
	 * Sets the value of the '{@link com.nextep.designer.diagram.model.Constraint#getSource <em>Source</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source</em>' reference.
	 * @see #getSource()
	 * @generated
	 */
	void setSource(Table value);

	/**
	 * Returns the value of the '<em><b>Target</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Target</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Target</em>' reference.
	 * @see #setTarget(Table)
	 * @see com.nextep.designer.diagram.model.ModelPackage#getConstraint_Target()
	 * @model
	 * @generated
	 */
	Table getTarget();

	/**
	 * Sets the value of the '{@link com.nextep.designer.diagram.model.Constraint#getTarget <em>Target</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target</em>' reference.
	 * @see #getTarget()
	 * @generated
	 */
	void setTarget(Table value);

} // Constraint
