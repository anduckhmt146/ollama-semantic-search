# 🧠 Semantic Search with Spring AI, Ollama, and Milvus

This project implements a semantic search API for product-related data using:
- [Spring AI](https://docs.spring.io/spring-ai/docs/current/reference/html/)
- [Ollama](https://ollama.com) as the local LLM/embedding provider
- [Milvus](https://milvus.io/) as the vector store backend
- Structured product data (e.g. title, description, features, price, category)

## ✨ Features

- 🔍 Semantic product search over embedded documents
- 💬 Chat-style querying with structured JSON responses
- ⚙️ Embedding generation endpoint
- 📦 Integration with Milvus for vector similarity search
- 🧠 Uses Ollama models locally (e.g., `mxbai-embed-large`, `llama3.2`)

---

## 🧰 Tech Stack

| Component        | Tool / Library         |
|------------------|------------------------|
| LLM & Embeddings | [Ollama](https://ollama.com) (`llama3.2`, `mxbai-embed-large`) |
| Embedding store  | [Milvus vector DB](https://milvus.io/) |
| Framework        | [Spring Boot](https://spring.io/projects/spring-boot) + [Spring AI](https://docs.spring.io/spring-ai/docs/current/reference/html/) |
| REST API         | Spring MVC (`@RestController`) |
| Data Format      | JSON |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Docker (for running Milvus)
- [Ollama](https://ollama.com/download) installed and running locally

### Run Ollama Models

```bash
brew install ollama
```

```bash
# Pull models
ollama pull mxbai-embed-large
ollama pull llama3.2

# Check available models
ollama ls
```

### Run Milvus

```bash
docker-compose up -d
```

### Run Source

```bash
mvn spring-boot:run
```

## 🧪 API Endpoints

### Chat with model

**Request:**

```bash
curl --location 'http://localhost:8080/api/ollama/chat?query=Captial%20of%20Vietnam'
```

**Response:**

```
{
    "answer": "Hanoi"
}
```

### Embedding token

**Request:**

```bash
curl --location 'http://localhost:8080/api/ollama/embedding?query=smartphone'
```

**Response:**

```
{
    "embedding": [
        -0.018574331,
        -0.05374644,
        -0.05071582,
        ...]
}
```

### Semantic search from dataset

**Request:**

```bash
curl --location 'http://localhost:8080/api/ollama/search?query=headphone'
```

**Response:**

```
{
    "title": "Bluetooth Wireless Earbuds",
    "description": "Noise-canceling earbuds with up to 30 hours battery life and fast charging support. Sweat and water-resistant.",
    "price": "$49.99",
    "category": "Electronics",
    "features": "Touch controls, Charging case, Bluetooth 5.3, Built-in microphone"
}
```

## Contact

- If you have any questions, please open an issue on GitHub or contacting me via email: ducan1406@gmail.com.