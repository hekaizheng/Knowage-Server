package it.eng.spagobi.metadata.metadata;

// Generated 12-apr-2016 10.43.25 by Hibernate Tools 3.4.0.CR1

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

/**
 * SbiMetaObjTable generated by hbm2java
 */
public class SbiMetaObjTable extends SbiHibernateModel {

	private SbiMetaObjTableId id;
	private SbiMetaTable sbiMetaTable;

	public SbiMetaObjTable() {
	}

	public SbiMetaObjTable(SbiMetaObjTableId id, SbiMetaTable sbiMetaTable) {
		this.id = id;
		this.sbiMetaTable = sbiMetaTable;
	}

	public SbiMetaObjTableId getId() {
		return this.id;
	}

	public void setId(SbiMetaObjTableId id) {
		this.id = id;
	}

	public SbiMetaTable getSbiMetaTable() {
		return this.sbiMetaTable;
	}

	public void setSbiMetaTable(SbiMetaTable sbiMetaTable) {
		this.sbiMetaTable = sbiMetaTable;
	}

}
