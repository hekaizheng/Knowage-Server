package it.eng.spagobi.metadata.metadata;

// Generated 12-apr-2016 10.43.25 by Hibernate Tools 3.4.0.CR1

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

/**
 * SbiMetaTableBc generated by hbm2java
 */
public class SbiMetaTableBc extends SbiHibernateModel {

	private SbiMetaTableBcId id;
	private SbiMetaTable sbiMetaTable;
	private SbiMetaBc sbiMetaBc;

	public SbiMetaTableBc() {
	}

	public SbiMetaTableBc(SbiMetaTableBcId id, SbiMetaTable sbiMetaTable, SbiMetaBc sbiMetaBc) {
		this.id = id;
		this.sbiMetaTable = sbiMetaTable;
		this.sbiMetaBc = sbiMetaBc;
	}

	public SbiMetaTableBcId getId() {
		return this.id;
	}

	public void setId(SbiMetaTableBcId id) {
		this.id = id;
	}

	public SbiMetaTable getSbiMetaTable() {
		return this.sbiMetaTable;
	}

	public void setSbiMetaTable(SbiMetaTable sbiMetaTable) {
		this.sbiMetaTable = sbiMetaTable;
	}

	public SbiMetaBc getSbiMetaBc() {
		return this.sbiMetaBc;
	}

	public void setSbiMetaBc(SbiMetaBc sbiMetaBc) {
		this.sbiMetaBc = sbiMetaBc;
	}

}
