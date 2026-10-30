# weavroafront-voice

weavroa 语音能力 Web 前端，提供**声音设计**与**声音克隆**两大可视化操作页面，内置语音合成（TTS）试听播放器。

## 功能特性

| 页面 | 说明 |
|------|------|
| 声音设计 | 输入自然语言描述生成专属音色，支持音色列表、试听播放与删除 |
| 声音克隆 | 上传样本音频克隆目标声音，支持克隆列表、试听播放与删除 |
| 语音合成 | 文本转语音（TTS），内置音色选择，播放器支持进度条、音量调节与下载 |

## 技术栈

- Vue 3 + Vite
- vue-router 4

## 快速开始

### 环境要求

- Node.js 20+
- 后端服务 [weavroa-voice](../)（默认 `http://127.0.0.1:8080`）

### 安装与运行

```bash
# 安装依赖
npm install

# 开发模式（默认端口 5173，/api 请求代理到 127.0.0.1:8080）
npm run dev

# 生产构建（产物输出到 dist/，路由 base 为 /tool/）
npm run build
```

## 环境变量

`.env.development` 中均为可选配置：

| 变量 | 说明 |
|------|------|
| `VITE_API_BASE_URL` | 后端根地址，所有接口走同一域名时设置（需后端配置 CORS） |
| `VITE_VOICE_API_KEY` | 请求头 `X-API-Key` 的值，后端需要鉴权时设置 |
| `VITE_TTS_STREAM_METHOD` | 流式 TTS 请求方式，可选 `GET` / `POST`（默认 POST） |
| `VITE_VOICE_CLONE_DEBUG` | 设为 `true` 打开声音克隆页控制台调试日志 |

## 目录结构

```
src/
├── components/       # 公共组件
├── lib/              # 流式合成、音色列表、音色名校验等工具
├── pages/
│   ├── voiceclone/   # 声音克隆页
│   └── voicedesign/  # 声音设计页
└── router/           # 路由配置
```

## License

本项目基于 [Apache License 2.0](./LICENSE) 协议开源。
