# weavroa-voice

Voice capability service providing **Text-to-Speech (TTS)**, **Voice Cloning**, and **Voice Design**. It can be run, packaged, and deployed independently.

## Features

| Capability | Description |
|------------|-------------|
| Voice Design | Generate custom voices from natural language descriptions; a built-in chat model automatically polishes them into high-quality voice design prompts |
| Voice Cloning | Clone target voices from sample audio, supporting both file upload and Base64 |
| Text-to-Speech | Text-to-speech (TTS) with streaming output |

A built-in chat model integration layer (`com.eniac.weavroa.llm`) based on the OpenAI-compatible protocol. Any compatible model can be registered through configuration for chat scenarios such as prompt polishing.

## Web Frontend

The companion web frontend is located in [`frontend/`](./frontend), providing visual pages for voice design and voice cloning with a built-in TTS preview player. See [frontend/README.md](./frontend/README.md) for details.

## Getting Started

### Prerequisites

- JDK 21+
- Maven 3.6+ (or use the bundled Maven Wrapper)

### Run

```bash
export DASHSCOPE_API_KEY=your-bailian-api-key

# Development mode
./mvnw spring-boot:run

# Package
./mvnw -DskipTests package

# Deploy and run
java -jar target/weavroa-voice-standalone-0.0.1-SNAPSHOT.jar
```

The default port is `8080` (configurable via `server.port`).

## Configuration

```yaml
weavroa:
  voice:
    api-key: ${DASHSCOPE_API_KEY:}      # API key for voice design/cloning/TTS
    base-url: https://dashscope.aliyuncs.com/api/v1
  llm:
    model:
      register:
        TEXT:
          - name: qwen-plus             # Chat model for prompt polishing; can be replaced with any OpenAI-compatible model
            base-url: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
            api-key: ${DASHSCOPE_API_KEY:}
```

All keys are injected via environment variables. The code and configuration contain no hard-coded keys.

## REST API

### Text-to-Speech (TTS)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/tts/synthesize` | Text-to-speech synthesis |
| GET | `/api/v1/tts/voices` | Built-in voice list |
| GET | `/api/v1/tts/synthesize/stream` | Streaming speech synthesis (SSE) |
| POST | `/api/v1/tts/synthesize/stream` | Streaming speech synthesis (SSE) |

### Voice Cloning

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/voices/clone` | Clone a voice by uploading sample audio (multipart) |
| GET | `/api/v1/voices/clone` | Cloned voice list (paginated) |
| POST | `/api/v1/voices/clone/base64` | Clone a voice from Base64 audio |
| DELETE | `/api/v1/voices/clone/{voice}` | Delete a cloned voice |

### Voice Design

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/voices/design` | Create a designed voice (with LLM prompt polishing) |
| GET | `/api/v1/voices/design` | Designed voice list |
| GET | `/api/v1/voices/design/{voice}` | Get a single designed voice |
| DELETE | `/api/v1/voices/design/{voice}` | Delete a designed voice |

## Tech Stack

- Spring Boot 3.5.4 (Web Starter + WebFlux, Java 21)
- dashscope-sdk-java 2.22.12, gson 2.13.1, pinyin4j 2.5.1, okhttp 4.12.0, lombok

## License

This project is open-sourced under the [Apache License 2.0](./LICENSE).
