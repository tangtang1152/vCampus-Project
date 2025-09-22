package edu.seu.campus.server;

import com.vCampus.net.CourseGrabServer;
import com.vCampus.common.ConfigManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan; // <-- 添加这个导入

@SpringBootApplication
@ComponentScan(basePackages = {"edu.seu.campus.server", "com.vCampus"}) // <-- 添加这个注解
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner startCourseGrabServer(CourseGrabServer courseGrabServer) {
        return args -> {
            if (!ConfigManager.isSocketEnabled()) {
                System.out.println("[ServerApplication] socket.enabled=false，跳过启动 CourseGrabServer");
                return;
            }
            new Thread(() -> {
            	try {
    				courseGrabServer.start();
    				} catch (Exception e) {
    					System.err.println("Failed to start CourseGrabServer: " + e.getMessage());
    					e.printStackTrace();
    				}
    			}, "CourseGrabServer-Starter").start();
        };
    }
}
            