package com.vCampus.entity;

import java.util.Date;

public class Subject {
    // 原有字段保持不变
    private String subjectId;      // 课程编号
    private String subjectName;    // 课程名称
    private Date subjectDate;      // 开课日期（可保留，作为学期起始参考）
    private Integer subjectNum;    // 学时数
    private Double credit;         // 学分
    private String teacherId;      // 授课教师ID

    // 新增课程时间相关字段
    private String weekRange;      // 周次范围（如 "1-8" 表示1-8周，"1-16" 表示全学期）
    private String weekType;       // 单双周规则（"ALL"=每周，"ODD"=单周，"EVEN"=双周）
    private String classTime;      // 具体上课时间（如 "周一第1-2节,周三第3-4节"）
    private String classroom;      // 教室位置（如 "教101"）

    // ==================== 构造函数 ====================
    public Subject() {
        // 原有初始化逻辑
        this.subjectId = "";
        this.subjectName = "";
        this.subjectDate = new Date();
        this.subjectNum = 0;
        this.credit = 0.0;
        this.teacherId = "";
        // 新增字段初始化
        this.weekRange = "1-16";    // 默认全学期
        this.weekType = "ALL";      // 默认每周上课
        this.classTime = "";
        this.classroom = "";
    }

    // 带参构造函数同步更新，补充新增字段
    public Subject(String subjectId, String subjectName, Date subjectDate, 
                  Integer subjectNum, Double credit, String teacherId,
                  String weekRange, String weekType, String classTime, String classroom) {
        // 原有字段赋值
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.subjectDate = subjectDate;
        this.subjectNum = subjectNum;
        this.credit = credit;
        this.teacherId = teacherId;
        // 新增字段赋值
        this.weekRange = weekRange;
        this.weekType = weekType;
        this.classTime = classTime;
        this.classroom = classroom;
    }

    // ==================== Getter 和 Setter ====================

    public String getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public Date getSubjectDate() {
        return subjectDate;
    }

    public Integer getSubjectNum() {
        return subjectNum;
    }

    public Double getCredit() {
        return credit;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public void setSubjectDate(Date subjectDate) {
        this.subjectDate = subjectDate;
    }

    public void setSubjectNum(Integer subjectNum) {
        this.subjectNum = subjectNum;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    // 新增字段的getter/setter
    public String getWeekRange() {
        return weekRange;
    }

    public void setWeekRange(String weekRange) {
        this.weekRange = weekRange;
    }

    public String getWeekType() {
        return weekType;
    }

    public void setWeekType(String weekType) {
        this.weekType = weekType;
    }

    public String getClassTime() {
        return classTime;
    }

    public void setClassTime(String classTime) {
        this.classTime = classTime;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    // toString、equals、hashCode方法同步更新，包含新增字段
    @Override
    public String toString() {
        return "Subject{" +
                "subjectId='" + subjectId + '\'' +
                ", subjectName='" + subjectName + '\'' +
                // 省略部分原有字段...
                ", weekRange='" + weekRange + '\'' +
                ", weekType='" + weekType + '\'' +
                ", classTime='" + classTime + '\'' +
                ", classroom='" + classroom + '\'' +
                '}';
    }
}