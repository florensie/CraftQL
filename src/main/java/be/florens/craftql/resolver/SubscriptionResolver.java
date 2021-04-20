package be.florens.craftql.resolver;

import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class SubscriptionResolver implements GraphQLSubscriptionResolver {

    public Publisher<ServerPlayerEntity> onBreakBlock() {
        //noinspection ReactiveStreamsPublisherImplementation
        return new Publisher<ServerPlayerEntity>() {
            private int count;

            @Override
            public void subscribe(Subscriber s) {
                PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
                    count++;
                    if (count == 10) {
                        s.onComplete();
                    } else {
                        //noinspection unchecked
                        s.onNext(player);
                    }
                });
            }
        };
    }
}
