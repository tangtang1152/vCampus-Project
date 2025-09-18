package com.vCampus.dao;

import com.vCampus.entity.Subject;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程数据访问对象实现类
 */
public class SubjectDaoImpl extends AbstractBaseDaoImpl<Subject, String> implements ISubjectDao {

    @Override
    protected String getTableName() {
        return "tbl_subject";
    }

    @Override
    protected String getIdColumnName() {
        return "subjectId";
    }

    @Override
    protected Subject createEntityFromResultSet(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        // 原有字段映射
        subject.setSubjectId(rs.getString("subjectId"));
        subject.setSubjectName(rs.getString("subjectName"));
        subject.setSubjectDate(rs.getDate("subjectDate"));
        subject.setSubjectNum(rs.getInt("subjectNum"));
        subject.setCredit(rs.getDouble("credit"));
        subject.setTeacherId(rs.getString("teacherId"));
        // 新增字段映射（从数据库结果集读取）
        subject.setWeekRange(rs.getString("weekRange"));
        subject.setWeekType(rs.getString("weekType"));
        subject.setClassTime(rs.getString("classTime"));
        subject.setClassroom(rs.getString("classroom"));
        return subject;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Subject subject) throws SQLException {
        // 原有字段参数（1-6）
        pstmt.setString(1, subject.getSubjectId());
        pstmt.setString(2, subject.getSubjectName());
        pstmt.setDate(3, new java.sql.Date(subject.getSubjectDate().getTime()));
        pstmt.setInt(4, subject.getSubjectNum());
        pstmt.setDouble(5, subject.getCredit());
        pstmt.setString(6, subject.getTeacherId());
        // 新增字段参数（7-10）
        pstmt.setString(7, subject.getWeekRange());
        pstmt.setString(8, subject.getWeekType());
        pstmt.setString(9, subject.getClassTime());
        pstmt.setString(10, subject.getClassroom());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Subject subject) throws SQLException {
        // 原有字段参数（1-5）
        pstmt.setString(1, subject.getSubjectName());
        pstmt.setDate(2, new java.sql.Date(subject.getSubjectDate().getTime()));
        pstmt.setInt(3, subject.getSubjectNum());
        pstmt.setDouble(4, subject.getCredit());
        pstmt.setString(5, subject.getTeacherId());
        // 新增字段参数（6-9）
        pstmt.setString(6, subject.getWeekRange());
        pstmt.setString(7, subject.getWeekType());
        pstmt.setString(8, subject.getClassTime());
        pstmt.setString(9, subject.getClassroom());
        // 条件字段（课程ID，第10个参数）
        pstmt.setString(10, subject.getSubjectId());
    }

    @Override
    public boolean insert(Subject subject, Connection conn) throws SQLException {
        // 截断超长字段（新增对classTime和classroom的处理）
        String truncatedSubjectName = ValidationService.truncateString(
            subject.getSubjectName(), DBConstants.SUBJECT_NAME_MAX_LENGTH);
        String truncatedTeacherId = ValidationService.truncateString(
            subject.getTeacherId(), DBConstants.TEACHER_ID_MAX_LENGTH);
        String truncatedClassTime = ValidationService.truncateString(
            subject.getClassTime(), 200); // 假设上课时间最大长度200
        String truncatedClassroom = ValidationService.truncateString(
            subject.getClassroom(), 100); // 假设教室名称最大长度100
        
        // 更新截断后的值
        subject.setSubjectName(truncatedSubjectName);
        subject.setTeacherId(truncatedTeacherId);
        subject.setClassTime(truncatedClassTime);
        subject.setClassroom(truncatedClassroom);

        // 新增字段的SQL插入语句
        String sql = "INSERT INTO tbl_subject (" +
                     "subjectId, subjectName, subjectDate, subjectNum, credit, teacherId, " +
                     "weekRange, weekType, classTime, classroom" +  // 新增字段
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";  // 10个参数
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setInsertParameters(pstmt, subject);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("插入课程记录失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Subject subject, Connection conn) throws SQLException {
        // 新增字段的SQL更新语句
        String sql = "UPDATE tbl_subject SET " +
                     "subjectName = ?, subjectDate = ?, subjectNum = ?, " +
                     "credit = ?, teacherId = ?, " +
                     "weekRange = ?, weekType = ?, classTime = ?, classroom = ? " +  // 新增字段
                     "WHERE subjectId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setUpdateParameters(pstmt, subject);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // 以下方法无需修改（查询逻辑会自动包含新增字段，因为使用SELECT *）
    @Override
    public List<Subject> findByTeacherId(String teacherId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_subject WHERE teacherId = ?";
        List<Subject> subjects = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(createEntityFromResultSet(rs));
                }
            }
        }
        return subjects;
    }

    @Override
    public List<Subject> findBySubjectName(String subjectName, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_subject WHERE subjectName LIKE ?";
        List<Subject> subjects = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + subjectName + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(createEntityFromResultSet(rs));
                }
            }
        }
        return subjects;
    }

    public Subject findBySubjectId(String subjectId, Connection conn) throws SQLException {
        return findById(subjectId, conn);
    }
}