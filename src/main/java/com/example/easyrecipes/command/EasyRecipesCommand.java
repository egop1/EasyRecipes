package com.example.easyrecipes.command;

import com.example.easyrecipes.EasyRecipes;
import com.example.easyrecipes.net.Network;
import com.example.easyrecipes.net.OpenPickerPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** Registers {@code /easyrecipes}, which tells the client to open the station picker. */
@Mod.EventBusSubscriber(modid = EasyRecipes.MOD_ID)
public final class EasyRecipesCommand {

    private EasyRecipesCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("easyrecipes")
                        // Editing recipes changes server-wide data — operators only.
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> openPicker(ctx.getSource())));
    }

    private static int openPicker(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenPickerPacket());
            return 1;
        }
        source.sendFailure(Component.translatable("gui.easyrecipes.command.players_only"));
        return 0;
    }
}
