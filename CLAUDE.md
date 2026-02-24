# MCP MongoDB Tools

Spring Boot starter that provides MCP tools for MongoDB (multi-instance, find, aggregate, count). Published on Maven Central as `io.github.massimilianopili:mcp-mongo-tools`.

## Build Commands

```bash
# Build
/opt/maven/bin/mvn clean compile

# Install to local Maven repo (skip GPG for local)
/opt/maven/bin/mvn clean install -Dgpg.skip=true

# Deploy to Maven Central (requires GPG key + Central Portal credentials in ~/.m2/settings.xml)
/opt/maven/bin/mvn clean deploy
```

Java 17+ required. Maven is at `/opt/maven/bin/mvn` (not in PATH).

## Project Structure

```
src/main/java/io/github/massimilianopili/mcp/mongo/
├── MongoConfig.java                  # Multi-instance MongoTemplate registry
├── MongoDbTools.java                 # @Tool: mongo_find, mongo_count, mongo_list_collections, mongo_aggregate, mongo_list_databases
└── MongoToolsAutoConfiguration.java  # Spring Boot auto-config

src/main/resources/META-INF/spring/
└── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

## Key Patterns

- **@Tool** (Spring AI): Marks synchronous tool methods.
- **Auto-configuration**: `MongoToolsAutoConfiguration` activates with `@ConditionalOnClass(MongoTemplate.class)` AND `@ConditionalOnProperty(name = "mcp.mongo.enabled", havingValue = "true")`. Both conditions must be met.
- **Multi-instance**: `MongoConfig` creates multiple `MongoTemplate` instances from `MCP_MONGO_NAMES` pattern.

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

## Dependencies

- Spring Boot 3.4.1 (spring-boot-autoconfigure, spring-boot-starter-data-mongodb)
- Spring AI 1.0.0 (spring-ai-model)

## Maven Central Publication

- GroupId: `io.github.massimilianopili`
- Plugin: `central-publishing-maven-plugin` v0.7.0
- Credentials: Central Portal token in `~/.m2/settings.xml` (server id: `central`)
