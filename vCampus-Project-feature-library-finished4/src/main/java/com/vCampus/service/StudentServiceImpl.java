package com.vCampus.service;

import com.vCampus.dao.IStudentDao;
import com.vCampus.dao.IUserDao;
import com.vCampus.dao.StudentDaoImpl;
import com.vCampus.dao.UserDaoImpl;
import com.vCampus.entity.Student;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生服务实现类
 */
public class StudentServiceImpl 
	extends AbstractBaseServiceImpl<Student, String> implements IStudentService {

    private final IStudentDao studentDao = new StudentDaoImpl();
    private final IUserDao userDao = new UserDaoImpl();
    
    // 实现抽象方法（现在有Connection参数）
    @Override
    protected Student doGetBySelfId(String studentId, Connection conn) throws Exception {
        return studentDao.findByStudentId(studentId, conn);
    }

    @Override
    protected List<Student> doGetAll(Connection conn) throws Exception {
        return studentDao.findAll(conn);
    }

    @Override
    protected boolean doAdd(Student student, Connection conn) throws Exception {
        return studentDao.insert(student, conn);
    }

    @Override
    protected boolean doUpdate(Student student, Connection conn) throws Exception {
        return studentDao.update(student, conn);
    }

    @Override
    protected boolean doDelete(String studentId, Connection conn) throws Exception {
        Student student = studentDao.findByStudentId(studentId, conn);
        if (student == null) return false;
        
        // 先删除相关的借阅记录
        if (!deleteRelatedBorrowRecords(student.getUserId(), conn)) {
            return false;
        }
        
        // 删除相关的预约记录
        if (!deleteRelatedReservations(student.getUserId(), conn)) {
            return false;
        }
        
        // 删除相关的选课记录
        if (!deleteRelatedChooses(studentId, conn)) {
            return false;
        }
        
        // 删除相关的订单记录
        if (!deleteRelatedOrders(studentId, conn)) {
            return false;
        }
        
        // 再删除学生记录
        boolean studentDeleted = studentDao.delete(studentId, conn);
        if (!studentDeleted) return false;
        
        // 最后删除用户记录
        return userDao.delete(student.getUserId(), conn);
    }
    
    /**
     * 删除用户相关的借阅记录
     */
    private boolean deleteRelatedBorrowRecords(Integer userId, Connection conn) throws Exception {
        try {
            // 导入借阅记录DAO
            com.vCampus.dao.IBorrowRecordDao borrowRecordDao = new com.vCampus.dao.BorrowRecordDao();
            
            // 获取用户的所有借阅记录
            java.util.List<com.vCampus.entity.BorrowRecord> borrowRecords = 
                borrowRecordDao.listByUser(String.valueOf(userId), conn);
            
            // 删除所有借阅记录
            for (com.vCampus.entity.BorrowRecord record : borrowRecords) {
                if (!borrowRecordDao.delete(record.getRecordId(), conn)) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("删除借阅记录失败: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * 删除用户相关的预约记录
     */
    private boolean deleteRelatedReservations(Integer userId, Connection conn) throws Exception {
        try {
            // 导入预约记录DAO
            com.vCampus.dao.IReservationDao reservationDao = new com.vCampus.dao.ReservationDao();
            
            // 获取用户的所有预约记录
            java.util.List<com.vCampus.entity.Reservation> reservations = 
                reservationDao.listActiveByUser(String.valueOf(userId), conn);
            
            // 删除所有预约记录
            for (com.vCampus.entity.Reservation reservation : reservations) {
                if (!reservationDao.delete(reservation.getReservationId(), conn)) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("删除预约记录失败: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * 删除学生相关的选课记录
     */
    private boolean deleteRelatedChooses(String studentId, Connection conn) throws Exception {
        try {
            // 导入选课记录DAO
            com.vCampus.dao.IChooseDao chooseDao = new com.vCampus.dao.ChooseDaoImpl();
            
            // 获取学生的所有选课记录
            java.util.List<com.vCampus.entity.Choose> chooses = 
                chooseDao.findByStudentId(studentId, conn);
            
            // 删除所有选课记录
            for (com.vCampus.entity.Choose choose : chooses) {
                if (!chooseDao.delete(choose.getSelectid(), conn)) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("删除选课记录失败: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * 删除学生相关的订单记录
     */
    private boolean deleteRelatedOrders(String studentId, Connection conn) throws Exception {
        try {
            // 导入订单DAO
            com.vCampus.dao.IOrderDao orderDao = new com.vCampus.dao.OrderDaoImpl();
            
            // 获取学生的所有订单记录
            java.util.List<com.vCampus.entity.Order> orders = 
                orderDao.getOrdersByStudentId(studentId);
            
            // 删除所有订单记录
            for (com.vCampus.entity.Order order : orders) {
                if (!orderDao.deleteOrder(order.getOrderId())) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("删除订单记录失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected boolean doExists(String studentId, Connection conn) throws Exception {
        return studentDao.findByStudentId(studentId, conn) != null;
    }

    // 实现特定方法
    @Override
    public Student getByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.findByUserId(userId, conn)
            );
        } catch (Exception e) {
            handleException("根据用户ID获取学生失败", e);
            return null;
        }
    }

    @Override
    public Student getStudentFull(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                Student student = studentDao.findByStudentId(studentId, conn);
                if (student != null) {
                    var user = userDao.findById(student.getUserId(), conn);
                    if (user != null) {
                        student.setUsername(user.getUsername());
                        student.setPassword(user.getPassword());
                        student.setRole(user.getRole());
                    }
                }
                return student;
            });
        } catch (Exception e) {
            handleException("获取完整学生信息失败", e);
            return null;
        }
    }

    @Override
    public boolean updateStudentOnly(Student student) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.update(student, conn)
            );
        } catch (Exception e) {
            handleException("更新学生信息失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteStudentOnly(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn ->
            	studentDao.delete(studentId, conn)
            );
        } catch (Exception e) {
            handleException("删除学生信息失败", e);
            return false;
        }
    }

    @Override
    public List<Student> getStudentsByClass(String className) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                List<Student> allStudents = studentDao.findAll(conn);
                return allStudents.stream()
                    .filter(student -> className.equals(student.getClassName()))
                    .collect(Collectors.toList());
            });
        } catch (Exception e) {
            handleException("根据班级获取学生失败", e);
            return List.of();
        }
    }
}