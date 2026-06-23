# Render 长期公网部署步骤

本项目已经准备好以下生产部署文件：

- [render.yaml](/E:/WORK_2/AI/Final/render.yaml:1)
- [Dockerfile](/E:/WORK_2/AI/Final/Dockerfile:1)
- [backend/src/main/resources/application-prod.yml](/E:/WORK_2/AI/Final/backend/src/main/resources/application-prod.yml:1)

## 部署前确认

你需要准备：

- GitHub 仓库
- Render 账号
- `DEEPSEEK_API_KEY`

当前仓库远程地址：

- `https://github.com/Xisu1231/lightbalance-health.git`

## 推荐部署方式：Render Blueprint

### 1. 把最新代码推送到 GitHub

确保 Render 读取到的是最新版本代码。

### 2. 登录 Render

进入：

- `Dashboard`
- `New +`
- `Blueprint`

### 3. 选择 GitHub 仓库

选择：

- `Xisu1231/lightbalance-health`

Render 会自动识别根目录下的 `render.yaml`。

### 4. 检查服务配置

当前配置要点如下：

- 服务类型：`Web Service`
- 运行方式：`Docker`
- 健康检查：`/api/health`
- Spring Profile：`prod`

### 5. 填写环境变量

至少需要补这一个：

- `DEEPSEEK_API_KEY`

已在 `render.yaml` 中预置的变量：

- `SPRING_PROFILES_ACTIVE=prod`
- `DEEPSEEK_ENABLED=true`
- `DEEPSEEK_MODEL=deepseek-v4-flash`

### 6. 点击 Deploy

首次部署一般会经历：

- 前端构建
- 后端打包
- Docker 镜像部署
- Render 健康检查

### 7. 部署成功后验证

部署完成后，Render 会给你一个正式公网地址，例如：

- `https://lightbalance-health.onrender.com`

建议验证这两个地址：

- 首页：`https://你的域名`
- 健康检查：`https://你的域名/api/health`

正常时健康检查应返回：

```json
{"status":"ok","service":"lightbalance-health","time":"..."}
```

## 部署后的使用建议

- 如果只是课堂演示，免费实例已经够用。
- 如果你希望唤醒更快、体验更稳定，可以升级 Render 计划。
- 如果以后要换模型或更新页面，只需要继续 push 到 GitHub，Render 会自动重新部署。

## 常见问题

### 1. 打开页面正常，但智能建议不可用

通常说明：

- `DEEPSEEK_API_KEY` 没填
- 或 DeepSeek 配额/网络问题

### 2. Render 显示健康检查失败

先访问：

- `/api/health`

如果这个接口不通，优先检查：

- 构建日志
- 启动日志
- 环境变量是否正确

### 3. 首次打开较慢

如果你使用的是免费计划，实例休眠后首次冷启动会偏慢，这是正常现象。
