package com.vcampus.ui;

import com.vcampus.controller.LibraryController;
import com.vcampus.entity.BorrowRecord;

import java.util.List;
import java.util.Scanner;

/**
 * 图书馆控制台界面 - 完善版本
 */
public class LibraryConsoleUI {
    private LibraryController controller;
    private Scanner scanner;
    private String currentUser; // 当前登录用户
    
    public LibraryConsoleUI() {
        this.controller = new LibraryController();
        this.scanner = new Scanner(System.in);
        this.currentUser = null;
    }
    
    /**
     * 启动系统
     */
    public void start() {
        showWelcome();
        
        while (true) {
            if (currentUser == null) {
                // 未登录状态
                showLoginMenu();
            } else {
                // 已登录状态
                showMainMenu();
            }
        }
    }
    
    /**
     * 显示欢迎信息
     */
    private void showWelcome() {
        System.out.println("✨ " + "=".repeat(50));
        System.out.println("✨            🏫 虚拟校园图书馆管理系统           ");
        System.out.println("✨ " + "=".repeat(50));
        System.out.println();
    }
    
    /**
     * 登录菜单
     */
    private void showLoginMenu() {
        System.out.println("\n🔐 用户登录");
        System.out.println("1. 登录系统");
        System.out.println("2. 退出系统");
        System.out.println("0. 清理测试数据");
        
        int choice = getIntInput("请选择操作: ");
        
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                exitSystem();
                break;
            case 0:
                cleanupData();
                break;
            default:
                System.out.println("❌ 无效选择，请重新输入");
        }
    }
    
    /**
     * 主菜单
     */
    private void showMainMenu() {
        System.out.println("\n📚 主菜单 - 欢迎 " + currentUser + "!");
        System.out.println("1. 🤝 借书");
        System.out.println("2. ↩️ 还书");
        System.out.println("3. 🔄 续借");
        System.out.println("4. 👤 我的借阅记录");
        System.out.println("5. ⚠️ 我的超期图书");
        System.out.println("6. 📊 借阅统计");
        System.out.println("7. 🔍 图书查询");
        System.out.println("8. ⚙️ 系统管理");
        System.out.println("9. 👋 退出登录");
        System.out.println("0. 🚪 退出系统");
        
        int choice = getIntInput("请选择操作: ");
        
        switch (choice) {
            case 1: borrowBook(); break;
            case 2: returnBook(); break;
            case 3: renewBook(); break;
            case 4: showMyRecords(); break;
            case 5: showMyOverdue(); break;
            case 6: showStatistics(); break;
            case 7: searchBooks(); break;
            case 8: systemManagement(); break;
            case 9: logout(); break;
            case 0: exitSystem(); break;
            default: System.out.println("❌ 无效选择");
        }
    }
    
    /**
     * 用户登录
     */
    private void login() {
        System.out.println("\n🔐 用户登录");
        String studentId = getStringInput("请输入学号: ");
        
        // 简单的登录验证（实际项目中应该有密码验证）
        if (studentId != null && !studentId.trim().isEmpty()) {
            currentUser = studentId;
            System.out.println("✅ 登录成功！欢迎 " + studentId);
        } else {
            System.out.println("❌ 学号不能为空");
        }
    }
    
    /**
     * 退出登录
     */
    private void logout() {
        System.out.println("👋 再见，" + currentUser + "！");
        currentUser = null;
    }
    
    /**
     * 借书功能
     */
    private void borrowBook() {
        System.out.println("\n🤝 借书功能");
        
        int bookId = getIntInput("请输入图书ID: ");
        int days = getIntInput("请输入借阅天数: ");
        
        String error = controller.validateBorrowParameters(bookId, currentUser, days);
        if (error != null) {
            System.out.println("❌ " + error);
            return;
        }
        
        System.out.println("⏳ 正在处理借书请求...");
        boolean success = controller.borrowBook(bookId, currentUser, days);
        
        if (success) {
            System.out.println("✅ 借书成功！");
            // 显示借阅详情
            List<BorrowRecord> records = controller.getUserBorrowRecords(currentUser);
            if (!records.isEmpty()) {
                BorrowRecord latest = records.get(0);
                System.out.println("   📖 图书ID: " + latest.getBookId());
                System.out.println("   📅 应还日期: " + latest.getDueDate());
            }
        } else {
            System.out.println("❌ 借书失败，请重试");
        }
    }
    
    /**
     * 还书功能
     */
    private void returnBook() {
        System.out.println("\n↩️ 还书功能");
        
        // 先显示用户当前的借阅记录
        showMyRecords();
        
        int bookId = getIntInput("请输入要归还的图书ID: ");
        
        String error = controller.validateReturnParameters(currentUser, bookId);
        if (error != null) {
            System.out.println("❌ " + error);
            return;
        }
        
        System.out.println("⏳ 正在处理还书请求...");
        boolean success = controller.returnBook(currentUser, bookId);
        System.out.println(success ? "✅ 还书成功！" : "❌ 还书失败");
    }
    
    /**
     * 续借功能
     */
    private void renewBook() {
        System.out.println("\n🔄 续借功能");
        
        // 先显示用户当前的借阅记录
        showMyRecords();
        
        // 改为获取记录ID而不是图书ID
        int recordId = getIntInput("请输入要续借的📝记录ID: ");
        int additionalDays = getIntInput("请输入续借天数: ");
        
        String error = controller.validateRenewParameters(recordId, additionalDays);
        if (error != null) {
            System.out.println("❌ " + error);
            return;
        }
        
        System.out.println("⏳ 正在处理续借请求...");
        boolean success = controller.renewBook(recordId, additionalDays);
        
        if (success) {
            System.out.println("✅ 续借成功！");
            // 显示更新后的信息
            List<BorrowRecord> records = controller.getUserBorrowRecords(currentUser);
            for (BorrowRecord record : records) {
                if (record.getRecordId() == recordId) {
                    System.out.println("   新的应还日期: " + record.getDueDate());
                    break;
                }
            }
        } else {
            System.out.println("❌ 续借失败，请检查记录ID是否正确");
        }
    }

    
    /**
     * 显示我的借阅记录
     */
    private void showMyRecords() {
        System.out.println("\n👤 我的借阅记录");
        controller.displayUserRecords(currentUser);
    }
    
    /**
     * 显示我的超期图书
     */
    private void showMyOverdue() {
        System.out.println("\n⚠️ 我的超期图书");
        controller.displayStudentOverdueBooks(currentUser);
    }
    
    /**
     * 显示统计信息
     */
    private void showStatistics() {
        System.out.println("\n📊 统计信息");
        System.out.println(controller.getLibraryStats());
        System.out.println(controller.getUserBorrowStats(currentUser));
    }
    
    /**
     * 图书查询（待实现）
     */
    private void searchBooks() {
        System.out.println("\n🔍 图书查询功能");
        System.out.println("该功能正在开发中...");
        // 这里可以添加图书搜索逻辑
    }
    
    /**
     * 系统管理
     */
    private void systemManagement() {
        System.out.println("\n⚙️ 系统管理");
        System.out.println("1. 清理所有数据");
        System.out.println("2. 重置图书库存");
        System.out.println("3. 返回主菜单");
        
        int choice = getIntInput("请选择操作: ");
        
        switch (choice) {
            case 1:
                cleanupData();
                break;
            case 2:
                resetInventory();
                break;
            case 3:
                return;
            default:
                System.out.println("❌ 无效选择");
        }
    }
    
    /**
     * 清理数据
     */
    private void cleanupData() {
        System.out.println("\n🧹 数据清理");
        System.out.println("⚠️  警告：这将删除所有测试数据！");
        String confirm = getStringInput("确认清理？(y/N): ");
        
        if ("y".equalsIgnoreCase(confirm)) {
            System.out.println("⏳ 正在清理数据...");
            boolean success = controller.cleanupAllTestData();
            System.out.println(success ? "✅ 数据清理成功" : "❌ 数据清理失败");
        } else {
            System.out.println("操作已取消");
        }
    }
    
    /**
     * 重置库存
     */
    private void resetInventory() {
        System.out.println("\n🔄 重置库存");
        int bookId = getIntInput("请输入图书ID: ");
        int count = getIntInput("请输入库存数量: ");
        
        boolean success = controller.resetBookInventory(bookId, count);
        System.out.println(success ? "✅ 库存重置成功" : "❌ 库存重置失败");
    }
    
    /**
     * 退出系统
     */
    private void exitSystem() {
        System.out.println("\n感谢使用虚拟校园图书馆系统！");
        System.out.println("再见！👋");
        System.exit(0);
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 获取整数输入
     */
    private int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("❌ 请输入有效的数字");
            scanner.next(); // 清除无效输入
            System.out.print(prompt);
        }
        int input = scanner.nextInt();
        scanner.nextLine(); // 清除换行符
        return input;
    }
    
    /**
     * 获取字符串输入
     */
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    /**
     * 显示分隔线
     */
    private void showSeparator() {
        System.out.println("\n" + "─".repeat(50));
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        LibraryConsoleUI ui = new LibraryConsoleUI();
        ui.start();
    }
}