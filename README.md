# MCP MongoDB Tools

Spring Boot starter providing MCP tools for MongoDB operations. Supports multi-instance environments with find, aggregate, and collection management.

## Installation

```xml
<dependency>
    <groupId>io.github.massimilianopili</groupId>
    <artifactId>mcp-mongo-tools</artifactId>
    <version>0.0.1</version>
</dependency>
```

Requires Java 17+ and Spring AI 1.0.0+.

## Tools

| Tool | Description |
|------|-------------|
| `mongo_find` | Query documents with JSON filter, projection, sort, and limit |
| `mongo_count` | Count documents matching a filter |
| `mongo_list_collections` | List collections in a database |
| `mongo_aggregate` | Execute aggregation pipeline |
| `mongo_list_databases` | List all configured MongoDB instances |

## Configuration

```properties
# Enable MongoDB tools (required)
MCP_MONGO_ENABLED=true

# Single instance
MCP_MONGO_URI=mongodb://localhost:27017/mydb

# Multi-instance (pattern: MCP_MONGO_{NAME}_URI)
MCP_MONGO_NAMES=ANALYTICS,LOGS
MCP_MONGO_ANALYTICS_URI=mongodb://host1:27017/analytics
MCP_MONGO_LOGS_URI=mongodb://host2:27017/logs
```

## How It Works

- Uses `@Tool` (Spring AI) for synchronous MCP tool methods
- Auto-configured via `MongoToolsAutoConfiguration` with `@ConditionalOnProperty(name = "mcp.mongo.enabled", havingValue = "true")`
- Multi-instance registry creates `MongoTemplate` instances from `MCP_MONGO_NAMES`

## Requirements

- Java 17+
- Spring Boot 3.4+
- Spring AI 1.0.0+
- Spring Data MongoDB

## License

[MIT License](LICENSE)
