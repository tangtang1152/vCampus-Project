package com.vCampus.entity;

public class Student {
    // 私有数据成员
    private int studentId;
    private String studentName;
    private String key;

    // 构造函数
    public Student(int studentId, String studentName, String key) {
        setInfo(studentId, studentName, key);
    }

    // set函数
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setInfo(int studentId, String studentName, String key) {
        setStudentId(studentId);
        setStudentName(studentName);
        setKey(key);
    }

    // get函数
    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getKey() {
        return key;
    }

    public static void main(String[] args) {
        System.out.println("hello student!");
    }
}