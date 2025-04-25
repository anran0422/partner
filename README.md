## 项目介绍
> 个人比赛项目的参照版本

旨在找到搭子的移动端 H5 网站（APP 风格），是基于基于 Spring Boot + Vue3 的 全栈项目，包括用户登录、更新个人信息、按标签搜索用户、建房组队、推荐相似用户等功能。

### 项目亮点
- Redis 缓存预热，避免访问主页请求数据库，减轻数据库压力
- 分布式锁 Redisson，后台只有一个线程通过定时任务去构建 Redis 缓存
- 使用编辑距离算法，优化用户匹配方式
- 大批量数据导入，测试功能提升幅度


## 功能概览

### 匹配功能
- 主页


![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E4%B8%BB%E9%A1%B5.jpg)
- 心动模式


![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E5%BF%83%E5%8A%A8%E6%A8%A1%E5%BC%8F.jpg)

### 队伍
- 创建队伍


![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E5%88%9B%E5%BB%BA%E9%98%9F%E4%BC%8D.jpg)
- 已加入的队伍

  
![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E5%B7%B2%E5%8A%A0%E5%85%A5%E9%98%9F%E4%BC%8D.jpg)
- 已创建的队伍


![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E5%B7%B2%E5%88%9B%E5%BB%BA%E9%98%9F%E4%BC%8D.jpg)
- 搜索队伍


![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E6%90%9C%E7%B4%A2%E9%98%9F%E4%BC%8D.jpg)
- 更新队伍


![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E6%9B%B4%E6%96%B0%E9%98%9F%E4%BC%8D.jpg)
### 用户
- 按照标签搜索用户


![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E6%A0%87%E7%AD%BE%E6%90%9C%E7%B4%A2%E7%94%A8%E6%88%B7.jpg)
- 个人信息


![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E4%B8%AA%E4%BA%BA%E4%BF%A1%E6%81%AF.jpg)
- 修改个人信息


![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E4%BF%AE%E6%94%B9%E4%B8%AA%E4%BA%BA%E4%BF%A1%E6%81%AF.jpg)
- 编辑信息


![](https://cloud-1311088844.cos.ap-beijing.myqcloud.com/public_share/project/partner/%E7%BC%96%E8%BE%91%E4%BF%A1%E6%81%AF.jpg)


### 技术选型

前端
- Vue 3
- Vant UI 组件库
- Vite 脚手架
- Axios 请求库

后端
- Java SpringBoot 框架
- MyBatis-Plus
- MyBatis X 自动生成代码
- Redisson 分布式锁
- Easy Excel 数据导入
- Spring Scheduler 定时任务
- Swagger + Knife4j 接口文档
- Gson：JSON 序列化库
- 相似度匹配算法

数据库
- MySQL 数据库
- Redis 缓存（多种 Java 实现方式）
- Redis 分布式登录

部署
- Linux 单机部署
- Nginx Web 服务器
- Docker
- 容器托管平台
