package be.florens.craftql.servlet;

import be.florens.craftql.CraftQL;
import be.florens.craftql.api.Registries;
import com.google.common.collect.Lists;
import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.tools.SchemaParser;
import graphql.kickstart.tools.SchemaParserBuilder;
import graphql.schema.GraphQLSchema;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.function.Supplier;

public class GraphQLContext {

    public final int port;
    private final Server server;
    public GraphQLConfiguration graphQLConfig;

    public GraphQLContext(int port) {
        this.port = port;

        // Basic server init
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        // Sets the prefix to use for the names of the threads
        if (server.getThreadPool() instanceof QueuedThreadPool) {
            ((QueuedThreadPool) server.getThreadPool()).setName(String.format("%s-server", CraftQL.MOD_ID));
        }

        // GraphQL servlet
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // HTTP endpoints
        context.addServlet(GraphQLServlet.class, "/graphql");
        context.setAttribute("graphql-config-supplier", (Supplier<GraphQLConfiguration>) this::getGraphQLConfig);
        context.addServlet(GraphiQLServlet.class, "/graphiql"); // TODO: make configurable
        server.setHandler(context);

        // Websocket endpoint (for subscriptions)
        WebSocketServerContainerInitializer.configure(context, (servletContext, serverContainer) -> {
            serverContainer.addEndpoint(ServerEndpointConfig.Builder
                    .create(SubscriptionEndpoint.class, "/subscriptions")
                    // TODO: this probably doesn't support reloading config
                    .configurator(new SubscriptionEndpointConfigurer(graphQLConfig))
                    .build());
        });
        server.setHandler(context);
    }

    public void parseAndApplySchemas(List<String> rawSchemas) {
        GraphQLSchema graphQLSchema = buildSchema(rawSchemas);
        updateServerConfig(graphQLSchema);
        startIfNotRunning();
    }

    public void startIfNotRunning() {
        if (!server.isRunning()) {
            try {
                server.setStopAtShutdown(true); // This doesn't appear do anything but it's safe to leave it
                server.start();
            } catch (Exception e) {
                CraftQL.LOGGER.error(String.format("[%s] Exception in GraphQL server thread", CraftQL.MOD_ID), e);
            }

            // Make sure we don't hang the game when it's closed
            // TODO: this won't work for the client
            ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
                try {
                    this.server.stop();
                } catch (Exception e) {
                    CraftQL.LOGGER.error(String.format("[%s] Exception occured while stopping GraphQL server thread", CraftQL.MOD_ID), e);
                }
            });
        }
    }

    private GraphQLSchema buildSchema(List<String> rawSchemas) {
        // Base schema with resolvers and scalars from registry
        SchemaParserBuilder schemaParserBuilder = SchemaParser.newParser()
                .resolvers(Lists.newArrayList(Registries.RESOLVERS))
                .scalars(Lists.newArrayList(Registries.SCALARS));

        rawSchemas.forEach(schemaParserBuilder::schemaString);
        return schemaParserBuilder.build().makeExecutableSchema();
    }

    public GraphQLConfiguration getGraphQLConfig() {
        return graphQLConfig;
    }

    private void updateServerConfig(GraphQLSchema schema) {
        graphQLConfig = GraphQLConfiguration.with(schema).build();
    }
}
