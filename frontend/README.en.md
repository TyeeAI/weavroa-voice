# weavroafront-voice

Web frontend for the weavroa voice capability, providing two visual pages for **Voice Design** and **Voice Cloning**, with a built-in Text-to-Speech (TTS) preview player.

## Features

| Page | Description |
|------|-------------|
| Voice Design | Generate custom voices from natural language descriptions, with voice list, preview playback and deletion |
| Voice Cloning | Clone target voices by uploading sample audio, with clone list, preview playback and deletion |
| Text-to-Speech | Text-to-speech synthesis with built-in voice selection; the player supports progress bar, volume control and download |

## Tech Stack

- Vue 3 + Vite
- vue-router 4

## Getting Started

### Prerequisites

- Node.js 20+
- Backend service [weavroa-voice](../) (default `http://127.0.0.1:8080`)

### Install and Run

```bash
# Install dependencies
npm install

# Development mode (default port 5173, /api requests are proxied to 127.0.0.1:8080)
npm run dev

# Production build (output to dist/, router base is /tool/)
npm run build
```

## Environment Variables

All entries in `.env.development` are optional:

| Variable | Description |
|----------|-------------|
| `VITE_API_BASE_URL` | Backend base URL; set it when all requests (including large file uploads) go through the same domain (CORS must be configured on the backend) |
| `VITE_VOICE_API_KEY` | Value of the `X-API-Key` header; set it when the backend requires authentication |
| `VITE_TTS_STREAM_METHOD` | HTTP method for streaming TTS requests, `GET` or `POST` (default POST) |
| `VITE_VOICE_CLONE_DEBUG` | Set to `true` to enable debug logs on the voice cloning page |

## Project Structure

```
src/
├── components/       # Shared components
├── lib/              # Utilities for streaming synthesis, voice list, voice name validation, etc.
├── pages/
│   ├── voiceclone/   # Voice cloning page
│   └── voicedesign/  # Voice design page
└── router/           # Router configuration
```

## License

This project is open-sourced under the [Apache License 2.0](./LICENSE).
