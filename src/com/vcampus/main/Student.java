package com.vcampus.main;

public class Student {
	// 私有数据成员
	private int id;
	private String name;
	private String key;

    //构造函数
	public Student(int id, String name,String key) {
		setInfo(id, name,key);
	}

	// set函数
	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public void setKey(String key) {
		this.key=key;
	}

	public void setInfo(int id, String name,String key) {
		setId(id);
		setName(name);
		setKey(key);
	}

	// get函数
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getKey() {
		return key;
	}

	public static void main(String[] args) {
		System.out.println("hello student!");
	}
}

