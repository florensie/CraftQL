package be.florens.craftql;

import be.florens.craftql.resolver.Resolvers;
import be.florens.craftql.scalar.Scalars;
import be.florens.craftql.servlet.GraphQLContext;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class CraftQL implements DedicatedServerModInitializer {

    public static final String MOD_ID = "craftql";
    public static final Logger LOGGER = LogManager.getLogger();
    private static GraphQLContext graphQLContext;

    @Override
    public void onInitializeServer() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SchemaResourceReloadListener());

        // Register default GraphQL types
        Resolvers.registerAll();
        Scalars.registerAll();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static Optional<MinecraftServer> getMinecraftServer() {
        Object gameInstance = FabricLoader.getInstance().getGameInstance();
        if (gameInstance instanceof MinecraftServer) {
            return Optional.of((MinecraftServer) gameInstance);
        }

        return Optional.empty();
    }

    public static GraphQLContext getOrCreateGraphQLContext() {
        if (graphQLContext == null) {
            graphQLContext = new GraphQLContext(8080);
        }

        return graphQLContext;
    }
}
