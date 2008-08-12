package org.oscarehr.PMmodule.model;

import java.io.Serializable;

public class FieldDefinition implements Serializable {
	 private static final long serialVersionUID = 1L;
	 private String fieldName;
	 private Integer fieldLength;
	 private String fieldType;
	public Integer getFieldLength() {
		return fieldLength;
	}
	public void setFieldLength(Integer fieldLength) {
		this.fieldLength = fieldLength;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldType() {
		return fieldType;
	}
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
}
