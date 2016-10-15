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
 * A representation of the model object '<em><b>Diagram</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.nextep.designer.diagram.model.Diagram#getTableDiagram <em>Table Diagram</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.nextep.designer.diagram.model.ModelPackage#getDiagram()
 * @model
 * @generated
 */
public interface Diagram extends EObject {
	/**
	 * Returns the value of the '<em><b>Table Diagram</b></em>' containment reference list.
	 * The list contents are of type {@link com.nextep.designer.diagram.model.Table}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Table Diagram</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Table Diagram</em>' containment reference list.
	 * @see com.nextep.designer.diagram.model.ModelPackage#getDiagram_TableDiagram()
	 * @model containment="true"
	 * @generated
	 */
	EList<Table> getTableDiagram();

} // Diagram
