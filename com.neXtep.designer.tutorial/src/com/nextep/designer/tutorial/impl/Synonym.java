package com.nextep.designer.tutorial.impl;

import com.nextep.datadesigner.dbgm.impl.SynchedVersionable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.tutorial.model.ISynonym;

public class Synonym extends SynchedVersionable<ISynonym> implements
		ISynonym {

	private String obj;
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public String getSynonymedObject() {
		return obj;
	}

	@Override
	public void setSynonymedObject(String obj) {
		if(this.obj != obj) {
			this.obj = obj;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

}
