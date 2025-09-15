# vCampus 项目开发指南

## 项目结构规范
- 所有实体类放在 `entity` 包
- 所有DAO类放在 `dao` 包  
- 所有Service类放在 `service` 包
- 所有视图控制器放在 `view` 包
- 所有工具类放在 `util` 包

## 命名规范
- 实体类：`Xxx` (如 Student)
- DAO类：`XxxDao` (如 StudentDao)
- Service类：`XxxService` (如 StudentService)
- 控制器：`XxxController` (如 LoginController)
- FXML文件：`xxx-view.fxml` (如 login-view.fxml)

- 数据成员：驼峰命名`xxxYyyZzz` (如 studemtName)
- 保留字：全小写`xxx` (如 username 改不了大写)

- 函数：驼峰命名`xxxYyyZzz` (如 getConnection)

## 字段大小 见DBConstants.java

- User类
- username：50
- password：255
- role: 20

- Student类
- studentName：50
- className：50

## 开发流程
1. 首先在entity中定义数据模型
2. 在dao中实现数据访问方法
3. 在service中实现业务逻辑
4. 在view中创建界面和控制器

## 代码提交规范
- 每次提交只完成一个功能
- 提交信息格式: `[模块] 简短描述`
- 示例: `[用户管理] 添加用户登录功能`