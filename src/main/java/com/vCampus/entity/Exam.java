package com.vCampus.entity;

import java.util.Date;

/**
 * 考试实体类
 * 表示系统中的考试信息，包含考试成绩和关联的选课信息
 */
public class Exam {
    private String examId;       // 考试号（主键，如 "E2024001"）
    private Date examDate;       // 考试日期
    private Double score;        // 成绩（如 85.5）
    private String chooseId;     // 选课号（外键，关联 Choose.chooseId）

    // ==================== 构造函数 ====================
    public Exam() {
        this.examId = "";
        this.examDate = new Date();
        this.score = 0.0;
        this.chooseId = "";
    }

    public Exam(String examId, Date examDate, Double score, String chooseId) {
        this.examId = examId;
        this.examDate = examDate;
        this.score = score;
        this.chooseId = chooseId;
    }

    // ==================== Getter 方法 ====================
    public String getExamId() {
        return examId;
    }

    public Date getExamDate() {
        return examDate;
    }

    public Double getScore() {
        return score;
    }

    public String getChooseId() {
        return chooseId;
    }

    // ==================== Setter 方法 ====================
    public void setExamId(String examId) {
        this.examId = examId;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public void setChooseId(String chooseId) {
        this.chooseId = chooseId;
    }

    // ==================== 辅助方法 ====================
    @Override
    public String toString() {
        return "Exam{" +
                "examId='" + examId + '\'' +
                ", examDate=" + examDate +
                ", score=" + score +
                ", chooseId='" + chooseId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return examId.equals(exam.examId);
    }

    @Override
    public int hashCode() {
        return examId.hashCode();
    }
}