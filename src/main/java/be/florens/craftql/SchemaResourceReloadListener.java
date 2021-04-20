package be.florens.craftql;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    @Override
    public Identifier getFabricId() {
        return CraftQL.id("schemas");
    }

    @Override
    public void apply(ResourceManager manager) {
        // Read from schema files
        List<String> rawSchemas = manager.findResources("schemas", path -> path.endsWith(".graphqls")).stream()
                .flatMap(id -> {
                    try (InputStream stream = manager.getResource(id).getInputStream()) {
                        String schemaString = IOUtils.toString(stream, StandardCharsets.UTF_8);
                        stream.close();
                        return Stream.of(schemaString);
                    } catch (IOException exception) {
                        CraftQL.LOGGER.error(String.format("[%s] Failed to load graphql schema: %s",
                                CraftQL.MOD_ID, id.toString()), exception);
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());

        // Update the server configuration
        long start = System.currentTimeMillis();
        CraftQL.getOrCreateGraphQLContext().parseAndApplySchemas(rawSchemas);
        CraftQL.LOGGER.info(String.format("[%s] Schema was parsed in %s milliseconds", CraftQL.MOD_ID, System.currentTimeMillis() - start));
    }
}
