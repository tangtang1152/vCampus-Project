package com.vCampus.dao;

import com.vCampus.entity.Choose;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 选课数据访问对象实现类
 */
public class ChooseDaoImpl extends AbstractBaseDaoImpl<Choose, String> implements IChooseDao {

    @Override
    protected String getTableName() {
        return "tbl_choose";
    }

    @Override
    protected String getIdColumnName() {
        return "selectid";
    }

    @Override
    protected Choose createEntityFromResultSet(ResultSet rs) throws SQLException {
        Choose choose = new Choose();
        choose.setSelectid(rs.getString("selectid"));
        choose.setStudentId(rs.getString("studentId"));
        choose.setSubjectId(rs.getString("subjectId"));
        return choose;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Choose choose) throws SQLException {
        pstmt.setString(1, choose.getSelectid());
        pstmt.setString(2, choose.getStudentId());
        pstmt.setString(3, choose.getSubjectId());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Choose choose) throws SQLException {
        pstmt.setString(1, choose.getStudentId());
        pstmt.setString(2, choose.getSubjectId());
        pstmt.setString(3, choose.getSelectid());
    }

    @Override
    public boolean insert(Choose choose, Connection conn) throws SQLException {
        String truncatedStudentId = ValidationService.truncateString(
            choose.getStudentId(), DBConstants.STUDENT_ID_MAX_LENGTH);
        String truncatedSubjectId = ValidationService.truncateString(
            choose.getSubjectId(), DBConstants.SUBJECT_ID_MAX_LENGTH);
        
        choose.setStudentId(truncatedStudentId);
        choose.setSubjectId(truncatedSubjectId);

        String sql = "INSERT INTO tbl_choose (selectid, studentId, subjectId) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setInsertParameters(pstmt, choose);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("插入选课记录失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Choose choose, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_choose SET studentId = ?, subjectId = ? WHERE selectid = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setUpdateParameters(pstmt, choose);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<Choose> findByStudentId(String studentId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_choose WHERE studentId = ?";
        List<Choose> chooses = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    chooses.add(createEntityFromResultSet(rs));
                }
            }
        }
        return chooses;
    }

    @Override
    public List<Choose> findBySubjectId(String subjectId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_choose WHERE subjectId = ?";
        List<Choose> chooses = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    chooses.add(createEntityFromResultSet(rs));
                }
            }
        }
        return chooses;
    }

    @Override
    public Choose findByStudentAndSubject(String studentId, String subjectId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_choose WHERE studentId = ? AND subjectId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setString(2, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // 由于继承了AbstractBaseDaoImpl，findById方法已经实现，这里添加一个别名方法保持兼容性
    public Choose findBySelectId(String selectid, Connection conn) throws SQLException {
        return findById(selectid, conn);
    }
}