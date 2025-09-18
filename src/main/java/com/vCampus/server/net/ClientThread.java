package com.vCampus.server.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.vCampus.service.IUserService;
import com.vCampus.service.ServiceFactory;

import com.vCampus.entity.User;
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
