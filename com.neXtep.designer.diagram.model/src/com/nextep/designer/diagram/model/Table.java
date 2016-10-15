/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.nextep.designer.diagram.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Table</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.nextep.designer.diagram.model.Table#getColumns <em>Columns</em>}</li>
 *   <li>{@link com.nextep.designer.diagram.model.Table#getName <em>Name</em>}</li>
 *   <li>{@link com.nextep.designer.diagram.model.Table#getSourceConstraints <em>Source Constraints</em>}</li>
 *   <li>{@link com.nextep.designer.diagram.model.Table#getTargetConstraint <em>Target Constraint</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.nextep.designer.diagram.model.ModelPackage#getTable()
 * @model
 * @generated
 */
public interface Table extends EObject {
	/**
	 * Returns the value of the '<em><b>Columns</b></em>' containment reference list.
	 * The list contents are of type {@link com.nextep.designer.diagram.model.Column}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Columns</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Columns</em>' containment reference list.
	 * @see com.nextep.designer.diagram.model.ModelPackage#getTable_Columns()
	 * @model containment="true" required="true"
	 * @generated
	 */
	EList<Column> getColumns();

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see com.nextep.designer.diagram.model.ModelPackage#getTable_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link com.nextep.designer.diagram.model.Table#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Source Constraints</b></em>' containment reference list.
	 * The list contents are of type {@link com.nextep.designer.diagram.model.Constraint}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Source Constraints</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source Constraints</em>' containment reference list.
	 * @see com.nextep.designer.diagram.model.ModelPackage#getTable_SourceConstraints()
	 * @model containment="true"
	 * @generated
	 */
	EList<Constraint> getSourceConstraints();

	/**
	 * Returns the value of the '<em><b>Target Constraint</b></em>' containment reference list.
	 * The list contents are of type {@link com.nextep.designer.diagram.model.Constraint}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Target Constraint</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Target Constraint</em>' containment reference list.
	 * @see com.nextep.designer.diagram.model.ModelPackage#getTable_TargetConstraint()
	 * @model containment="true"
	 * @generated
	 */
	EList<Constraint> getTargetConstraint();

} // Table
