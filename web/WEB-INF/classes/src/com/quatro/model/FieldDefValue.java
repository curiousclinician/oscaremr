package com.quatro.model;
import org.caisi.model.BaseObject;

public class FieldDefValue extends BaseObject{
		private String tableId;
	    private String fieldName;
	    private String fieldDesc;
	    private String fieldType;
	    private String lookupTable;           
	    private String fieldSQL;
	    private boolean editable;
	    private boolean auto;
	    private boolean unique;
	    private int genericIdx;
	    private int fieldIndex;
	    
	    private String val = "";
	    private String valDesc = "";
	    
		public String getValDesc() {
			return valDesc;
		}

		public void setValDesc(String valDesc) {
			this.valDesc = valDesc;
		}

		public String getVal() {
			return val;
		}

		public void setVal(String val) {
			this.val = val;
		}

		public FieldDefValue() {
		}
	    
		public String getFieldDesc() {
			return fieldDesc;
		}
		public void setFieldDesc(String fieldDesc) {
			this.fieldDesc = fieldDesc;
		}
		public String getFieldName() {
			return fieldName;
		}
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}
		public String getFieldSQL() {
			return fieldSQL;
		}
		public void setFieldSQL(String fieldSQL) {
			this.fieldSQL = fieldSQL;
		}
		public String getFieldType() {
			return fieldType;
		}
		public void setFieldType(String fieldType) {
			this.fieldType = fieldType;
		}
		public String getLookupTable() {
			return lookupTable;
		}
		public void setLookupTable(String lookupTable) {
			this.lookupTable = lookupTable;
		}
		public String getTableId() {
			return tableId;
		}
		public void setTableId(String tableId) {
			this.tableId = tableId;
		}

		public boolean isEditable() {
			return editable;
		}
		
		public void setEditable(boolean editable) {
			this.editable = editable;
		}

		public int getFieldIndex() {
			return fieldIndex;
		}

		public void setFieldIndex(int fieldIndex) {
			this.fieldIndex = fieldIndex;
		}

		public boolean isAuto() {
			return auto;
		}

		public void setAuto(boolean auto) {
			this.auto= auto;
		}

		public int getGenericIdx() {
			return genericIdx;
		}

		public void setGenericIdx(int genericIdx) {
			this.genericIdx = genericIdx;
		}

		public boolean isUnique() {
			return unique;
		}

		public void setUnique(boolean unique) {
			this.unique = unique;
		}
	}
