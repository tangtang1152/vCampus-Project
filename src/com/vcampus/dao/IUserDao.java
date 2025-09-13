package com.vcampus.dao;

import java.sql.SQLException;
import java.util.List;
import com.vcampus.entity.User;

public interface IUserDao {

	boolean addUser(User user) throws SQLException;
	boolean deleteUser(Integer userId) throws SQLException;
	boolean updateUser(User user) throws SQLException;
	User getUserById(Integer userId) throws SQLException;
	User getUserByUsername(String username) throws SQLException;
	List<User> getAllUsers() throws SQLException;
	
	User validateUser(String username, String password) throws SQLException;
	
	 // 新增的方法
    boolean updateUserPassword(Integer userId, String newPassword) throws SQLException;
    boolean updateUserRole(Integer userId, String newRole) throws SQLException;
    List<User> getUsersByRole(String role) throws SQLException;
    boolean usernameExists(String username) throws SQLException;
    int getTotalUserCount() throws SQLException;
    int getUserCountByRole(String role) throws SQLException;
    List<User> searchUsers(String keyword) throws SQLException;
}
