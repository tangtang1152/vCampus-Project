package server.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.vCampus.service.IUserService;
import com.vCampus.service.ServiceFactory;

import com.vCampus.entity.Student;
import com.vCampus.entity.Teacher;
import com.vCampus.entity.Admin;

public class ClientThread extends Thread {
    private Socket socket;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true)) {

            // 1. 读取客户端请求
            String request = in.readLine();
            System.out.println("[SERVER] 收到请求: " + request);

            // 2. 处理请求
            String response = processRequest(request);//下面有这个函数

            // 3. 返回响应
            out.println(response);
            System.out.println("[SERVER] 返回响应: " + response);

        } catch (IOException e) {
            System.err.println("[SERVER] 客户端连接异常: " + e.getMessage());
        }
    }

    private String processRequest(String request) {//根据string: request返回string:response的函数
       

        // 分割请求：格式为 "命令|参数1|参数2..."
        String[] parts = request.split("\\|");
        String command = parts[0].toUpperCase(); // 统一转为大写

        switch (command) {
        case "LOGIN":
        {
        	/*
            if (parts.length < 3) {
                return "ERROR|登录参数不足";
            }
            */
            IUserService userService = ServiceFactory.getUserService();
            String username = parts[1];
            String password = parts[2];
            var user = userService.login(username, password);
            if (user != null) {
                return "LOGIN_SUCCESS|" + username + "|" + user.getRole(); // 返回完整信息
            } else {
                return "LOGIN_FAILED|用户名或密码错误"; // 明确失败原因
            }
        }
                

         case "REGISTER":
         {
        	 switch(parts[1]) {
        	 case "学生":{
        		 if (parts.length < 8) {
                     return "ERROR|登录参数不足";
                 }
        		 
        		 String StudentId = parts[2] ;
        		 String StudentName = parts[3] ;
        		 String Username = parts[4] ;
        		 String Password = parts[5] ;
        		 String Role = parts[6] ;
        		 String ClassName = parts[7];
        		 
        		 Student st = new Student();
        		 st.setStudentId(StudentId);
        		 st.setUsername(Username);
        		 st.setPassword(Password);
        		 st.setRole(Role);
        		 st.setStudentName(StudentName);
        		 st.setClassName(ClassName);
        		 
        		 IUserService userService = ServiceFactory.getUserService();
                 IUserService.RegisterResult result = userService.register(st);
                 return ReturnResult(result,"学生");
        	 }
             case "教师":{
            	 if (parts.length < 10) {
                     return "ERROR|登录参数不足";
                 }
            	 String TeacherId = parts[2] ;
        		 String TeacherName = parts[3] ;
        		 String Username = parts[4] ;
        		 String Password = parts[5] ;
        		 String Role = parts[6] ;
        		 String Technical = parts[7];
        		 String DepartmentId = parts[8];
        		 String Sex = parts[9];
        		 
        		 Teacher tc = new Teacher();
        		 tc.setTeacherId(TeacherId);
        		 tc.setTeacherName(TeacherName);
        		 tc.setUsername(Username);
        		 tc.setPassword(Password);
        		 tc.setRole(Role);
        		 tc.setTechnical(Technical);
        		 tc.setDepartmentId(DepartmentId);
        		 tc.setSex(Sex);
        		 IUserService userService = ServiceFactory.getUserService();
                 IUserService.RegisterResult result = userService.register(tc);
                 return ReturnResult(result,"教师");
            	
        	 }
            case "管理员":{
            	if (parts.length < 7) {
                    return "ERROR|登录参数不足";
                }
            	 String AdminId = parts[2] ;
        		 String AdminName = parts[3] ;
        		 String Username = parts[4] ;
        		 String Password = parts[5] ;
        		 String Role = parts[6] ;
        		 
        		 Admin admin = new Admin();
        		 admin.setAdminId(AdminId);
        		 admin.setAdminName(AdminName);
        		 admin.setUsername(Username);
        		 admin.setPassword(Password);
        		 admin.setRole(Role);
        		 IUserService userService = ServiceFactory.getUserService();
                 IUserService.RegisterResult result = userService.register(admin);
                 return ReturnResult(result,"管理员");
            }
            default:{
            	return "REGISTER_FAILED|服务器不可能执行到这里";
            }//end fault
        	 }
        	 
         }
         default:{
    		 return "REGISTER_FAILED|服务器不可能执行到这里";
    	 }
         }
        
             
         
        }
        private String ReturnResult(IUserService.RegisterResult result, String userType) {
            System.out.println("处理注册结果: " + result + ", 用户类型: " + userType);

            switch (result) {
                case SUCCESS:
                    return "SUCCESS";
			case USERNAME_EXISTS:
                	return "用户名已存在";
			case STUDENT_ID_EXISTS:
                	return "学号已存在";
			case TEACHER_ID_EXISTS:
                	return "教师编号已存在";
			case ADMIN_ID_EXISTS:
                	return "管理员工号已存在";
			case VALIDATION_FAILED:
                	return "注册失败，数据验证失败，请检查输入";
			case DATABASE_ERROR:
                	return "注册失败，数据库错误，请检查数据格式或联系管理员";
			default:
                	return "注册失败，未知错误";
            }
        
    }
   
    
}
