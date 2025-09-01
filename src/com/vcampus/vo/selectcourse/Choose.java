package com.vcampus.vo.selectcourse;

import java.io.Serializable;

public class Choose implements Serializable {
	// 属性
	private String selectid;
	private String cid;
	private String tid;
	private String score;

	// 无参构造方法
	public Choose() {

	}

	// 有参构造方法
	public Choose(String selectid, String cid, String tid, String score) {
		this.selectid = selectid;
		this.cid = cid;
		this.tid = tid;
		this.score = score;
	}

	// getter和setter方法
	public String getSelectid() {
		return selectid;
	}

	public void setSelectid(String selectid) {
		this.selectid = selectid;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	// toString方法
	@Override
	public String toString() {
		return "Choose [selectid=" + selectid + ", cid=" + cid + ", tid=" + tid + ", score=" + score + "]";
	}
}