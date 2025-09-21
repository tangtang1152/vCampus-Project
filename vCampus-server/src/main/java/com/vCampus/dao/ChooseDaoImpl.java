package com.vCampus.dao;

import com.vCampus.entity.Choose;
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
 * 选课数据访问对象实现类
 */
@Repository // <-- 添加这个注解
public class ChooseDaoImpl extends AbstractBaseDaoImpl<Choose, String> implements IChooseDao {

    @Override
    protected String getTableName() { return "tbl_choose"; }

    @Override
    protected String getIdColumnName() { return "selectid"; }

    @Override
    protected Choose createEntityFromResultSet(ResultSet rs) throws SQLException {
        Choose c = new Choose();
        c.setSelectid(rs.getString("selectid"));
        c.setStudentId(rs.getString("studentId"));
        c.setSubjectId(rs.getString("subjectId"));
        // 实体无 selectDate 字段，忽略该列
        return c;
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
        return super.insert(choose, conn);
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