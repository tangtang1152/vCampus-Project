package com.vcampus.vo.selectcourse;

import java.io.Serializable;

public class Teacher implements Serializable {
	// 属性
	private String tid;
	private String tname;
	private String technical;
	private String sex;
	private String deptid;

	// 无参构造方法
	public Teacher() {

	}
	
	// 有参构造方法
	public Teacher(String tid, String tname, String technical, String sex, String deptid) {
        this.tid = tid;
        this.tname = tname;
        this.technical = technical;
        this.sex = sex;
        this.deptid = deptid;
	}
	
	// getter和setter方法
	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getTname() {
		return tname;
	}

	public void setTname(String tname) {
		this.tname = tname;
	}
	public String getTechnical() {
		return technical;
	}

	public void setTechnical(String technical) {
		this.technical = technical;
	}
	
	public String getSex() {
		return sex;
	}
	
	public void setSex(String sex) {
        this.sex = sex;
	}
	
	public String getDeptid() {
		return deptid;
	}
	
	public void setDeptid(String deptid) {
		this.deptid = deptid;
	}
	
	// toString方法
	@Override
	public String toString() {
        return "Teacher [tid=" + tid + ", tname=" + tname + ", technical=" + technical + ", sex=" +sex + ", deptid=" + deptid + "]";
	}
	
	

}