## 实现计划

### 1. 问题分析

当前头像上传功能存在以下问题：
- 前端使用 Base64 存储头像，没有上传到服务器
- 后端没有提供上传头像的接口
- 没有使用 COS 存储头像

### 2. 实现方案

1. **后端**：在 UserController 中添加上传头像的接口，使用 COS 存储
2. **前端**：修改 UserProfilePage.vue 中的头像上传逻辑，调用后端接口
3. **参考**：参考 ArticleController 中的 uploadCoverImage 方法实现

### 3. 实现内容

#### 3.1 后端修改

**文件**：`d:\myspace\上课代码\clothingPattern\clothingPattern-backend\src\main\java\com\xhs\clothingpatternbackend\controller\UserController.java`

- 添加 `uploadAvatar` 方法，用于上传头像到 COS
- 使用 MultipartFile 接收文件
- 调用 CosUtils 上传文件到 COS
- 返回 COS URL

#### 3.2 前端修改

**文件**：`d:\myspace\上课代码\clothingPattern\clothingPattern-front\src\pages\user\UserProfilePage.vue`

- 修改 `handleAvatarUpload` 方法，调用后端上传头像接口
- 使用 FormData 发送文件
- 接收并保存 COS URL

### 4. 预期结果

- 头像上传到 COS，返回 COS URL
- 前端保存 COS URL 到 userForm.userAvatar
- 更新用户信息时，将 COS URL 保存到数据库
- 头像显示正常

### 5. 技术实现

- 后端使用 CosUtils 上传文件到 COS
- 前端使用 FormData 发送文件
- 后端返回 COS URL
- 前端保存 COS URL 并显示

### 6. 实现步骤

1. 后端添加 uploadAvatar 接口
2. 前端修改 handleAvatarUpload 方法
3. 测试头像上传功能
4. 确保头像显示正常

### 7. 代码参考

- 参考 ArticleController 中的 uploadCoverImage 方法
- 参考前端 ArticleEditPage.vue 中的封面上传逻辑