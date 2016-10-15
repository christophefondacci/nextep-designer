package com.nextep.designer.dbgm.postgre.model;

import java.util.Set;

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.model.ICheckConstraintContainer;
import com.nextep.designer.dbgm.model.IPhysicalObject;

/**
 * A PostgreSql specific definition of a database table
 * 
 * @author mattla
 * @author Christophe Fondacci
 */
public interface IPostgreSqlTable extends IBasicTable, IPhysicalObject, ICheckConstraintContainer {

	/**
	 * @return all inherited parent tables of this table
	 */
	Set<IReference> getInheritances();

	/**
	 * Adds a parent table inheritance to this current table
	 * 
	 * @param t
	 *            the inherited {@link IBasicTable} to add
	 */
	void addInheritance(IBasicTable t);

	/**
	 * Adds a parent table reference inheritance to this current table. This
	 * method should only be used for version control related operations.
	 * 
	 * @param r
	 *            the {@link IReference} of the inherited table to add
	 */
	void addInheritanceRef(IReference r);

	/**
	 * Removes a parent table inheritance from this current table
	 * 
	 * @param t
	 *            the inherited {@link IBasicTable} to remove
	 */
	void removeInheritance(IBasicTable t);

}
