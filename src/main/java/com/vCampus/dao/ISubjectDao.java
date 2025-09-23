package com.vCampus.dao;

import com.vCampus.entity.Subject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 课程数据访问对象接口
 */
public interface ISubjectDao extends IBaseDao<Subject, String> {
    List<Subject> findByTeacherId(String teacherId, Connection conn) throws SQLException;
    List<Subject> findBySubjectName(String subjectName, Connection conn) throws SQLException;

    // 抢课并发：剩余名额原子扣减/回滚恢复（使用 subjectNum 表示“剩余名额”）
    boolean decreaseSlotIfAvailable(String subjectId, Connection conn) throws SQLException;
    boolean increaseSlot(String subjectId, Connection conn) throws SQLException;
}