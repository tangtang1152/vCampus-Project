package com.vCampus.entity;

/**
 * 选课实体类
 * 表示学生选课信息，包含学生和课程的关联关系
 */
public class Choose {
    private String selectid;    // 选课记录ID（主键，如 "CH2024001"）
    private String studentId;   // 学号（外键，关联 Student.studentId）
    private String subjectId;   // 课程号（外键，关联 Subject.subjectId）

    // ==================== 构造函数 ====================
    public Choose() {
        this.selectid = "";
        this.studentId = "";
        this.subjectId = "";
    }

    public Choose(String selectid, String studentId, String subjectId) {
        this.selectid = selectid;
        this.studentId = studentId;
        this.subjectId = subjectId;
    }

    // ==================== Getter 方法 ====================
    public String getSelectid() {
        return selectid;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    // ==================== Setter 方法 ====================
    public void setSelectid(String chooseId) {
        this.selectid = chooseId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    // ==================== 辅助方法 ====================
    @Override
    public String toString() {
        return "Choose{" +
                "chooseId='" + selectid + '\'' +
                ", studentId='" + studentId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Choose choose = (Choose) o;
        return selectid.equals(choose.selectid);
    }

    @Override
    public int hashCode() {
        return selectid.hashCode();
    }
}