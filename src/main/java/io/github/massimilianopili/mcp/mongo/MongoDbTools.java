package io.github.massimilianopili.mcp.mongo;

import org.bson.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "mcp.mongo.enabled", havingValue = "true")
public class MongoDbTools {

    private final Map<String, MongoTemplate> registry;

    public MongoDbTools(Map<String, MongoTemplate> mongoTemplateRegistry) {
        this.registry = mongoTemplateRegistry;
    }

    @Tool(name = "mongo_find",
          description = "Finds documents in a MongoDB collection. Filter and projection use MongoDB JSON syntax. Default: max 50 documents.")
    public List<Map<String, Object>> find(
            @ToolParam(description = "Collection name") String collection,
            @ToolParam(description = "JSON filter, e.g. {\"status\": \"active\"}", required = false) String filter,
            @ToolParam(description = "JSON projection, e.g. {\"name\": 1, \"_id\": 0}", required = false) String projection,
            @ToolParam(description = "Max number of documents (default 50, max 200)", required = false) Integer limit,
            @ToolParam(description = "MongoDB instance name (from mongo_list_databases). If omitted, uses the first available.", required = false) String database) {
        try {
            MongoTemplate mongo = getMongo(database);
            Document filterDoc = (filter != null && !filter.isBlank())
                    ? Document.parse(filter) : new Document();
            Document projDoc = (projection != null && !projection.isBlank())
                    ? Document.parse(projection) : null;

            Query query;
            if (projDoc != null) {
                query = new BasicQuery(filterDoc, projDoc);
            } else {
                query = new BasicQuery(filterDoc);
            }

            int effectiveLimit = (limit != null && limit > 0) ? Math.min(limit, 200) : 50;
            query.limit(effectiveLimit);

            List<Document> results = mongo.find(query, Document.class, collection);
            return results.stream()
                    .map(doc -> new LinkedHashMap<String, Object>(doc))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of(Map.of("error", "Errore query MongoDB: " + e.getMessage()));
        }
    }

    @Tool(name = "mongo_count",
          description = "Counts documents in a MongoDB collection, with optional filter")
    public Map<String, Object> count(
            @ToolParam(description = "Collection name") String collection,
            @ToolParam(description = "JSON filter, e.g. {\"status\": \"active\"}", required = false) String filter,
            @ToolParam(description = "MongoDB instance name (from mongo_list_databases). If omitted, uses the first available.", required = false) String database) {
        try {
            MongoTemplate mongo = getMongo(database);
            Query query;
            if (filter != null && !filter.isBlank()) {
                query = new BasicQuery(Document.parse(filter));
            } else {
                query = new Query();
            }
            long total = mongo.count(query, collection);
            return Map.of("collection", collection, "count", total);
        } catch (Exception e) {
            return Map.of("error", "Errore conteggio MongoDB: " + e.getMessage());
        }
    }

    @Tool(name = "mongo_list_collections",
          description = "Lists all collections in the configured MongoDB database")
    public List<String> listCollections(
            @ToolParam(description = "MongoDB instance name (from mongo_list_databases). If omitted, uses the first available.", required = false) String database) {
        try {
            return getMongo(database).getCollectionNames().stream()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of("Errore: " + e.getMessage());
        }
    }

    @Tool(name = "mongo_aggregate",
          description = "Executes a MongoDB aggregation pipeline on a collection. The pipeline is a JSON array of stages.")
    public List<Map<String, Object>> aggregate(
            @ToolParam(description = "Collection name") String collection,
            @ToolParam(description = "Pipeline JSON array, e.g. [{\"$match\":{\"status\":\"active\"}},{\"$group\":{\"_id\":\"$category\",\"total\":{\"$sum\":1}}}]")
            String pipelineJson,
            @ToolParam(description = "MongoDB instance name (from mongo_list_databases). If omitted, uses the first available.", required = false) String database) {
        try {
            MongoTemplate mongo = getMongo(database);
            List<Document> pipeline = Document.parse("{\"p\":" + pipelineJson + "}")
                    .getList("p", Document.class);

            List<Document> results = mongo.getCollection(collection)
                    .aggregate(pipeline)
                    .into(new ArrayList<>());

            return results.stream()
                    .map(doc -> new LinkedHashMap<String, Object>(doc))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of(Map.of("error", "Errore aggregazione MongoDB: " + e.getMessage()));
        }
    }

    @Tool(name = "mongo_list_databases",
          description = "Lists the MongoDB instances configured in the MCP server. Each name can be used as the 'database' parameter in other MongoDB tools.")
    public List<String> listDatabases() {
        return new ArrayList<>(registry.keySet());
    }

    // --- Metodi privati ---

    private MongoTemplate getMongo(String database) {
        if (database == null || database.isBlank()) {
            return registry.values().iterator().next();
        }
        MongoTemplate template = registry.get(database);
        if (template == null) {
            throw new IllegalArgumentException(
                    "Istanza MongoDB '" + database + "' non trovata. Disponibili: " + registry.keySet());
        }
        return template;
    }
}
