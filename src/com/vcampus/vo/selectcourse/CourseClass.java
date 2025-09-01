package com.vcampus.vo.selectcourse;

import java.io.Serializable;

public class CourseClass implements Serializable {
    // 属性
    private String cid;
    private String cname;
    private String expername;
    private String deptid;
    
    // 无参构造方法
	public CourseClass() {
        
    }
	
	// 有参构造方法
	public CourseClass(String cid, String cname, String expername, String deptid) {
        this.cid = cid;
        this.cname = cname;
        this.expername = expername;
        this.deptid = deptid;
    }
	
	// getter和setter方法
	public String getCid() {
        return cid;
    }

	public void setCid(String cid) {
        this.cid = cid;
    }

	public String getCname() {
        return cname;
    }

	public void setCname(String cname) {
        this.cname = cname;
    }

	public String getExpername() {
        return expername;
    }

	public void setExpername(String expername) {
        this.expername = expername;
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
        return "CourseClass [cid=" + cid + ", cname=" + cname + ", expername=" + expername + ", deptid=" + deptid + "]";
    }
}