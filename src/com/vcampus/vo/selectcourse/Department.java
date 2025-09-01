package com.vcampus.vo.selectcourse;

import java.io.Serializable;

public class Department implements Serializable {
	// 属性
	private String deptid;
	private String deptname;

	// 无参构造方法
	public Department() {

	}

	// 有参构造方法
	public Department(String deptid, String deptname) {
		this.deptid = deptid;
		this.deptname = deptname;
	}

	// getter和setter方法
	public String getDeptid() {
		return deptid;
	}

	public void setDeptid(String deptid) {
		this.deptid = deptid;
	}

	public String getDeptname() {
		return deptname;
	}

	public void setDeptname(String deptname) {
		this.deptname = deptname;
	}

	// toString方法
	@Override
	public String toString() {
		return "Department [deptid=" + deptid + ", deptname=" + deptname + "]";
	}
}


