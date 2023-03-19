package net.punchtree.freebuild.datahandling;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface DatabaseConnection {
    CompletableFuture<? extends DatabaseConnection> connect(boolean forced);

    void disconnect();
    Map<String, Object> create(String table, Map<String, Object> values) throws Exception;
    Optional<Map<String, Object>> read(String table);
    Map<String, Object> update(String table, Map<String, Object> values) throws Exception;
    Map<String, Object> delete(String table) throws Exception;
    Map<String, Object> upsert(String table, Map<String, Object> values) throws Exception;

}
