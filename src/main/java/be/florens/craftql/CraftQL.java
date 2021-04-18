package be.florens.craftql;

import be.florens.craftql.servlet.GraphQLServlet;
import be.florens.craftql.servlet.SubscriptionEndpoint;
import be.florens.craftql.servlet.SubscriptionEndpointConfigurer;
import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.schema.GraphQLSchema;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.server.ServerEndpointConfig;
import java.util.Optional;

public class CraftQL implements DedicatedServerModInitializer {

    public static final String MOD_ID = "craftql";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final int PORT = 8080;
    public static GraphQLConfiguration graphQLConfig;
    private static Server graphQLServer;

    @Override
    public void onInitializeServer() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SchemaResourceReloadListener());

        // Make sure we don't hang Minecraft when the server is stopped
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (graphQLServer != null) {
                try {
                    graphQLServer.stop();
                } catch (Exception e) {
                    LOGGER.error(String.format("[%s] Exception occured while stopping GraphQL server thread", MOD_ID), e);
                }
            }
        });
    }

    public static Optional<MinecraftServer> getMinecraftServer() {
        Object gameInstance = FabricLoader.getInstance().getGameInstance();
        if (gameInstance instanceof MinecraftServer) {
            return Optional.of((MinecraftServer) gameInstance);
        }

        return Optional.empty();
    }

    private static void startGraphQLServer() {
        // Basic server init
        graphQLServer = new Server();
        ServerConnector connector = new ServerConnector(graphQLServer);
        connector.setPort(PORT);
        graphQLServer.addConnector(connector);

        // Sets the prefix to use for the names of the threads
        if (graphQLServer.getThreadPool() instanceof QueuedThreadPool) {
            ((QueuedThreadPool) graphQLServer.getThreadPool()).setName(String.format("%s-server", MOD_ID));
        }

        // GraphQL servlet
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // HTTP endpoint
        context.addServlet(GraphQLServlet.class, "/graphql");
        graphQLServer.setHandler(context);

        // Websocket endpoint (for subscriptions)
        WebSocketServerContainerInitializer.configure(context, (servletContext, serverContainer) -> {
            serverContainer.addEndpoint(ServerEndpointConfig.Builder
                    .create(SubscriptionEndpoint.class, "/subscriptions")
                    .configurator(new SubscriptionEndpointConfigurer())
                    .build());
        });
        graphQLServer.setHandler(context);

        // Start the server threads
        try {
            graphQLServer.setStopAtShutdown(true); // This doesn't appear do anything but it's safe to leave it
            graphQLServer.start();
        } catch (Exception e) {
            LOGGER.error(String.format("[%s] Exception in GraphQL server thread", MOD_ID), e);
        }
    }

    public static void updateGraphQLSchema(GraphQLSchema schema) {
        graphQLConfig = GraphQLConfiguration.with(schema).build();

        if (graphQLServer == null) {
            startGraphQLServer();
        }
    }
}
