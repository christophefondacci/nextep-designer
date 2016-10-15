package com.nextep.designer.capture.services;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This service provides features to capture data from a database.
 * 
 * @author Christophe Fondacci
 */
public interface IDataCaptureService {

	/**
	 * This method captures the data of the specified tables collection using the provided
	 * connection. Data will be returned as a collection of {@link IDataSet}.
	 * 
	 * @param c the {@link IConnection} to fetch table data from
	 * @param tablesToCapture collection of tables to capture
	 * @return a collection of captured dataset
	 */
	Collection<IVersionable<IDataSet>> captureTablesData(IConnection c,
			Collection<IBasicTable> tablesToCapture, IProgressMonitor monitor);
}
