package com.vCampus.dao;

import com.vCampus.entity.Subject;
import com.vCampus.util.ValidationService;
import com.vCampus.util.DBConstants;
import org.springframework.stereotype.Repository; // <-- 添加这个

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程数据访问对象实现类
 */
@Repository // <-- 添加这个注解
public class SubjectDaoImpl extends AbstractBaseDaoImpl<Subject, String> implements ISubjectDao {

    @Override
    protected String getTableName() { return "tbl_subject"; }

    @Override
    protected String getIdColumnName() { return "subjectId"; }

    @Override
    protected Subject createEntityFromResultSet(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setSubjectId(rs.getString("subjectId"));
        s.setSubjectName(rs.getString("subjectName"));
        s.setSubjectNum(rs.getInt("subjectNum"));
        s.setCredit(rs.getDouble("credit"));
        s.setTeacherId(rs.getString("teacherId"));
        s.setSubjectDate(rs.getDate("subjectDate"));
        s.setWeekRange(rs.getString("weekRange"));
        s.setWeekType(rs.getString("weekType"));
        s.setClassTime(rs.getString("classTime"));
        s.setClassroom(rs.getString("classroom"));
        return s;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Subject subject) throws SQLException {
        pstmt.setString(1, subject.getSubjectId());
        pstmt.setString(2, subject.getSubjectName());
        pstmt.setInt(3, subject.getSubjectNum());
        pstmt.setDouble(4, subject.getCredit());
        pstmt.setString(5, subject.getTeacherId());
        pstmt.setDate(6, new java.sql.Date(subject.getSubjectDate().getTime()));
        pstmt.setString(7, subject.getWeekRange());
        pstmt.setString(8, subject.getWeekType());
        pstmt.setString(9, subject.getClassTime());
        pstmt.setString(10, subject.getClassroom());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Subject subject) throws SQLException {
        // 与 update SQL 参数顺序保持一致
        pstmt.setString(1, subject.getSubjectName());
        pstmt.setDate(2, new java.sql.Date(subject.getSubjectDate().getTime()));
        pstmt.setInt(3, subject.getSubjectNum());
        pstmt.setDouble(4, subject.getCredit());
        pstmt.setString(5, subject.getTeacherId());
        pstmt.setString(6, subject.getWeekRange());
        pstmt.setString(7, subject.getWeekType());
        pstmt.setString(8, subject.getClassTime());
        pstmt.setString(9, subject.getClassroom());
        pstmt.setString(10, subject.getSubjectId());
    }

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
        subject.setSubjectName(truncatedSubjectName);
        subject.setTeacherId(truncatedTeacherId);
        subject.setClassTime(truncatedClassTime);
        subject.setClassroom(truncatedClassroom);
        return super.insert(subject, conn);
    }

    @Override
    public boolean update(Subject subject, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_subject SET subjectName = ?, subjectDate = ?, subjectNum = ?, credit = ?, teacherId = ?, weekRange = ?, weekType = ?, classTime = ?, classroom = ? WHERE subjectId = ?";

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

    // 抢课并发：原子扣减剩余名额（subjectNum 视为剩余名额）
    @Override
    public boolean decreaseSlotIfAvailable(String subjectId, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_subject SET subjectNum = subjectNum - 1 WHERE subjectId = ? AND subjectNum > 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subjectId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean increaseSlot(String subjectId, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_subject SET subjectNum = subjectNum + 1 WHERE subjectId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subjectId);
            return ps.executeUpdate() > 0;
        }
    }
}