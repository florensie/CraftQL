package be.florens.craftql;

import be.florens.craftql.resolver.PlayerInventoryResolver;
import be.florens.craftql.resolver.RootQueryResolver;
import be.florens.craftql.scalar.Scalars;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.kickstart.tools.ObjectMapperConfigurer;
import graphql.kickstart.tools.ObjectMapperConfigurerContext;
import graphql.kickstart.tools.PerFieldObjectMapperProvider;
import graphql.kickstart.tools.SchemaParser;
import graphql.kickstart.tools.SchemaParserBuilder;
import graphql.kickstart.tools.SchemaParserOptions;
import graphql.language.FieldDefinition;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class SchemaResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    @Override
    public Identifier getFabricId() {
        return new Identifier(CraftQL.MOD_ID, "schemas");
    }

    @Override
    public void apply(ResourceManager manager) {
        // Base schema builder with resolvers and scalars
        SchemaParserBuilder schemaParserBuilder = SchemaParser.newParser()
                .resolvers(new RootQueryResolver(), new PlayerInventoryResolver())
                .scalars(Scalars.TEXT);

        // Read from schema files
        manager.findResources("schemas", path -> path.endsWith(".graphqls")).stream()
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
                .forEach(schemaParserBuilder::schemaString);

        // Update the server configuration
        long start = System.currentTimeMillis();
        CraftQL.updateGraphQLSchema(schemaParserBuilder.build().makeExecutableSchema());
        CraftQL.LOGGER.info(String.format("[%s] Schema was parsed in %s milliseconds", CraftQL.MOD_ID, System.currentTimeMillis() - start));
    }
}
