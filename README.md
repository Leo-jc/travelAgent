# TravelAgent - 智能旅行助手

基于 Spring Boot 和 Spring AI 构建的智能旅行助手应用，提供 AI 驱动的旅行咨询服务。

## 功能特性

- 🤖 **AI 对话**: 集成 Spring AI 和阿里云 DashScope，实现智能旅行咨询
- 📝 **RAG 支持**: 支持加载 Markdown 文档，增强 AI 回答的准确性
- 💾 **聊天记忆**: 基于 Kryo 序列化的文件存储，支持多会话管理
- 🔍 **智能增强**: 支持 Re-Reading Advisor，提升回答质量
- 📊 **API 文档**: 集成 Knife4j，提供友好的 API 调试界面

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.5.14 | 应用框架 |
| Spring AI | 1.0.0 | AI 集成框架 |
| Spring AI Alibaba | 1.0.0-M6.1 | 阿里云 AI 集成 |
| Knife4j | 4.4.0 | API 文档工具 |
| Kryo | 5.6.2 | 高性能序列化 |
| Lombok | 1.18.x | 代码简化工具 |

## 项目结构

```
travelAgent/
├── src/main/java/com/serain/travelagent/
│   ├── TravelAgentApplication.java    # 启动类
│   ├── app/                           # 核心业务逻辑
│   │   ├── TravelAgent.java           # 旅行助手核心服务
│   │   └── advisor/                   # AI 增强器
│   │       ├── SimpleLoggerAdvisor.java    # 日志增强器
│   │       └── ReReadingAdvisor.java       # 重读增强器
│   ├── config/                        # 配置类
│   │   └── AiConfig.java              # AI 配置
│   ├── chatmemory/                    # 聊天记忆
│   │   └── FileBasedChatMemory.java   # 文件存储记忆实现
│   ├── rag/                           # RAG 相关
│   │   └── TravelDocumentLoader.java  # 文档加载器
│   └── model/controller/              # REST API
│       └── HealthController.java      # 健康检查接口
├── src/main/resources/
│   ├── application.yaml               # 应用配置
│   └── document/                      # RAG 文档目录
│       └── *.md                       # Markdown 文档
└── pom.xml                            # Maven 配置
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- 阿里云 DashScope API Key

### 配置说明

1. **配置 API Key**

创建 `src/main/resources/application-local.yaml` 文件：

```yaml
spring:
  ai:
    dashscope:
      api-key: your-api-key-here
      chat:
        options:
          model: qwen-plus
```

或通过环境变量设置：

```bash
export DASHSCOPE_API_KEY=your-api-key-here
```

### 运行项目

```bash
# 开发模式运行
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/travelAgent-0.0.1-SNAPSHOT.jar
```

### 访问服务

- **API 文档**: http://localhost:8123/api/swagger-ui.html
- **健康检查**: http://localhost:8123/api/health

## API 接口

### 健康检查

```http
GET /api/health
```

**响应**:
```json
"ok"
```

## 核心组件

### TravelAgent

旅行助手核心服务，提供 AI 对话能力：

```java
@Resource
private TravelAgent travelAgent;

// 调用示例
travelAgentResponse response = travelAgent.ask("推荐一个旅游目的地", "conversation-1");
```

### FileBasedChatMemory

基于文件的聊天记忆实现，使用 Kryo 序列化存储会话记录。

### ReReadingAdvisor

通过重新阅读问题提升 AI 回答准确性的增强器。

### TravelDocumentLoader

Markdown 文档加载器，支持批量加载文档目录下的 `.md` 文件。

## RAG 文档

项目包含以下旅行相关文档：

- `世界著名旅游目的地深度指南.md`
- `文化遗产与历史古迹探索之旅.md`
- `旅游攻略与实用旅行技巧大全.md`
- `美食与特色体验之旅.md`
- `自然景观与户外探险指南.md`

## 测试

```bash
# 运行所有测试
mvn test

# 运行指定测试
mvn test -Dtest=TravelAgentApplicationTests
```

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License