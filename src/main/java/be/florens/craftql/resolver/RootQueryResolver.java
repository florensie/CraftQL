package be.florens.craftql.resolver;

import be.florens.craftql.CraftQL;
import graphql.kickstart.tools.GraphQLQueryResolver;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;

public class RootQueryResolver implements GraphQLQueryResolver {

    public List<ServerPlayerEntity> getPlayers() {
        return CraftQL.getMinecraftServer().map(minecraftServer -> minecraftServer.getPlayerManager().getPlayerList())
                .orElse(Collections.emptyList());
    }
}
