package com.vcampus.vo.selectcourse;

import java.io.Serializable;

public class Student implements Serializable {
    // 属性
    private String sid;
    private String sname;
    private String sex;
    private String email;
    private String idCard;
    private String cid;
    
    // 无参构造方法
	public Student() {
        
    }
	
	// 有参构造方法
	public Student(String sid, String sname, String sex, String email, String idCard, String cid) {
        this.sid = sid;
        this.sname = sname;
        this.sex = sex;
        this.email = email;
        this.idCard = idCard;
        this.cid = cid;
	}
	
	// getter和setter方法
	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getSname() {
		return sname;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}
	
	public String getSex() {
		return sex;
	}
	
	public void setSex(String sex) {	
        this.sex = sex;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getIdCard() {
		return idCard;
	}
	
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	
	public String getCid() {
		return cid;
	}
	
	public void setCid(String cid) {
		this.cid = cid;
	}
	
	// 重写toString方法
	@Override
    public String toString() {
        return "Student{" +
                "sid='" + sid + '\'' +
                ", sname='" + sname + '\'' +
                ", sex='" + sex + '\'' +
                ", email='" + email + '\'' +
                ", idCard='" + idCard + '\'' +
                ", cid='" + cid + '\'' +
                '}';
	}
}