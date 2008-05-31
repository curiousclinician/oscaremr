package com.quatro.model;

import java.util.ArrayList;

import com.quatro.util.KeyValueBean;

public class ReportTempCriValue {
    private int counter;
    private int templateNo;
    private String relation;
    private int fieldNo;
    private String op;
    private String val;
    private String valDesc;
//    private String ops;
    private boolean required = false;
    private String fieldName;
    private ReportFilterValue filter;
    private ArrayList operatorList;
    
    public int getCounter() {
		return counter;
	}
	public void setCounter(int counter) {
		this.counter = counter;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public int getFieldNo() {
		return fieldNo;
	}
	public void setFieldNo(int fieldNo) {
		this.fieldNo = fieldNo;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
//	public String getOps() {
//		return ops;
//	}
//	public void setOps(String ops) {
//		this.ops = ops;
//	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public boolean getRequired() {
		return required;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public int getTemplateNo() {
		return templateNo;
	}
	public void setTemplateNo(int templateNo) {
		this.templateNo = templateNo;
	}
	public String getVal() {
		return val;
	}
	public void setVal(String val) {
		this.val = val;
	}
	public String getValDesc() {
		return valDesc;
	}
	public void setValDesc(String valDesc) {
		this.valDesc = valDesc;
	}
	public ArrayList getOperatorList() {
		if(operatorList==null) operatorList= new ArrayList();
		return operatorList;
	}
	public void setOperatorList(ArrayList operatorList) {
		this.operatorList = operatorList;
	}
	public ReportFilterValue getFilter() {
		return filter;
	}
	public void setFilter(ReportFilterValue filter) {
		this.filter = filter;
	}
    
}
