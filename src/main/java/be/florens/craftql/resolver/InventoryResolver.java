package be.florens.craftql.resolver;

import graphql.kickstart.tools.GraphQLResolver;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// TODO: Generalize for all inventories
public class InventoryResolver implements GraphQLResolver<PlayerInventory> {

    public List<ItemStack> getStacks(PlayerInventory inventory) {
        ArrayList<ItemStack> stacks = new ArrayList<>();

        for (int i = 0; i < inventory.size(); i++) {
            stacks.add(inventory.getStack(i));
        }

        return stacks;
    }
}
