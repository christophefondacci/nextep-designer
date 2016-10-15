package com.nextep.designer.tutorial.model;

import com.nextep.datadesigner.dbgm.model.IDatabaseObject;

public interface ISynonym extends IDatabaseObject<ISynonym> {

	public static final String TYPE_ID = "SYNONYM";
	
	String getSynonymedObject();
	void setSynonymedObject(String obj);
}
