package edu.ucsd.osdt.db;

import java.util.LinkedList;

public class dt2dbMap {

	String dtChannel = null;
	String tableName = null;
	String colName = null;
	LinkedList <String> dataList = new LinkedList<String>();
	LinkedList <String> valueList = new LinkedList<String>();
	LinkedList <String> typeList = new LinkedList<String>();
	
	public dt2dbMap (String dtc) {
		this.dtChannel = dtc;
	}
	
	public dt2dbMap (String dtc, String tbName, String cName) {
		this.dtChannel = dtc;
		this.tableName = tbName;
		this.colName = cName;
	}
	
	public void addValue (String d, String v, String t) {
		this.dataList.add(d);
		this.valueList.add(v);
		this.typeList.add(t);
	}
	
	public String getTableName () {
		return this.tableName;
	}
	
	public String getColName() {
		return this.colName;
	}
}
