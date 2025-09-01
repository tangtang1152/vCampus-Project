package com.vcampus.vo.selectcourse;

import java.io.Serializable;
import java.util.Date;

public class Subject implements Serializable {
	// 属性
	private String subjid;
	private Date date;
	private String subjname;
	private int num;
	private double credit;
	private String tid;

	// 无参构造方法
	public Subject() {

	}
	
	// 有参构造方法
	public Subject(String subjid, Date date, String subjname, int num, double credit, String tid) {
		this.subjid = subjid;
		this.date = date;
		this.subjname = subjname;
		this.num = num;
		this.credit = credit;
		this.tid = tid;
	}
	
	// getter和setter方法
	public String getSubjid() {
		return subjid;
	}

	public void setSubjid(String subjid) {
		this.subjid = subjid;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getSubjname() {
		return subjname;
	}

	public void setSubjname(String subjname) {
		this.subjname = subjname;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public double getCredit() {
		return credit;
	}

	public void setCredit(double credit) {
		this.credit = credit;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}
	
	// 重写toString方法
	@Override
	public String toString() {
		return "Subject [subjid=" + subjid + ", date=" + date + ", subjname=" + subjname + ", num=" + num + ", credit="
				+ credit + ", tid=" + tid + "]";
	}	
}