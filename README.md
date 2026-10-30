# weavroa-voice

语音能力服务，提供**语音合成（TTS）**、**声音克隆**、**声音设计**三大能力，可独立运行、打包、部署。

## 功能特性

| 能力 | 说明 |
|------|------|
| 声音设计 | 输入自然语言描述，内置对话模型自动润色为高质量音色设计提示词，生成专属音色 |
| 声音克隆 | 通过样本音频克隆目标声音，支持文件上传与 Base64 两种方式 |
| 语音合成 | 文本转语音（TTS），支持流式输出 |

内置对话模型接入层（`com.eniac.weavroa.llm`），基于 OpenAI 兼容协议，可通过配置注册任意兼容模型，用于提示词润色等对话场景。

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+（或直接使用项目自带的 Maven Wrapper）

### 启动

```bash
export DASHSCOPE_API_KEY=你的百炼APIKey

# 开发模式
./mvnw spring-boot:run

# 打包
./mvnw -DskipTests package

# 部署运行
java -jar target/weavroa-voice-standalone-0.0.1-SNAPSHOT.jar
```

默认端口 `8080`（可通过 `server.port` 修改）。

## 配置

```yaml
weavroa:
  voice:
    api-key: ${DASHSCOPE_API_KEY:}      # 声音设计/克隆/TTS 调用的 API Key
    base-url: https://dashscope.aliyuncs.com/api/v1
  llm:
    model:
      register:
        TEXT:
          - name: qwen-plus             # 提示词润色用的对话模型，可换成任意 OpenAI 兼容模型
            base-url: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
            api-key: ${DASHSCOPE_API_KEY:}
```

密钥一律通过环境变量注入，代码与配置中不含任何硬编码密钥。

## REST 接口

### 语音合成（TTS）

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/v1/tts/synthesize` | 文本转语音 |
| GET | `/api/v1/tts/voices` | 内置音色列表 |
| GET | `/api/v1/tts/synthesize/stream` | 流式语音合成（SSE） |
| POST | `/api/v1/tts/synthesize/stream` | 流式语音合成（SSE） |

### 声音克隆

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/v1/voices/clone` | 上传样本音频克隆声音（multipart） |
| GET | `/api/v1/voices/clone` | 克隆声音列表（分页） |
| POST | `/api/v1/voices/clone/base64` | Base64 音频克隆声音 |
| DELETE | `/api/v1/voices/clone/{voice}` | 删除克隆声音 |

### 声音设计

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/v1/voices/design` | 创建设计音色（含 LLM 提示词润色） |
| GET | `/api/v1/voices/design` | 设计音色列表 |
| GET | `/api/v1/voices/design/{voice}` | 查询单个设计音色 |
| DELETE | `/api/v1/voices/design/{voice}` | 删除设计音色 |

## 技术栈

- Spring Boot 3.5.4（Web Starter + WebFlux，Java 21）
- dashscope-sdk-java 2.22.12、gson 2.13.1、pinyin4j 2.5.1、okhttp 4.12.0、lombok

## License

本项目基于 [Apache License 2.0](./LICENSE) 协议开源。
