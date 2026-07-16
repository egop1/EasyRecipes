package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.net.Network;
import com.example.easyrecipes.net.OpenEditorPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Asks for the mechanical crafter's grid size before opening the editor. The size travels in the
 * open packet so the menu can lay out exactly that many ghost slots (menu slots are fixed once
 * built, so the size must be known up front).
 */
public class GridSizeScreen extends Screen {

    private static final int W = 200;
    private static final int H = 124;

    private final Station station;
    private final String editingId;
    private int left;
    private int top;
    private int gridW = 3;
    private int gridH = 3;

    public GridSizeScreen(Station station, String editingId) {
        super(Component.translatable("gui.easyrecipes.size.title"));
        this.station = station;
        this.editingId = editingId == null ? "" : editingId;
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;

        addRenderableWidget(Button.builder(Component.literal("-"), b -> gridW = clamp(gridW - 1))
                .bounds(left + 80, top + 30, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> gridW = clamp(gridW + 1))
                .bounds(left + 130, top + 30, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-"), b -> gridH = clamp(gridH - 1))
                .bounds(left + 80, top + 56, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> gridH = clamp(gridH + 1))
                .bounds(left + 130, top + 56, 20, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.size.open"), b -> open())
                .bounds(left + 16, top + 88, 80, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.list.back"),
                        b -> minecraft.setScreen(new RecipeListScreen(station)))
                .bounds(left + 104, top + 88, 80, 20).build());
    }

    private void open() {
        Network.CHANNEL.sendToServer(new OpenEditorPacket(station.ordinal(), editingId, gridW, gridH));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        GuiHelper.panel(g, left, top, W, H);
        g.drawCenteredString(font, title, left + W / 2, top + 10, GuiHelper.TITLE);
        g.drawString(font, Component.translatable("gui.easyrecipes.size.width"), left + 16, top + 36, GuiHelper.TITLE, false);
        g.drawString(font, Component.translatable("gui.easyrecipes.size.height"), left + 16, top + 62, GuiHelper.TITLE, false);
        g.drawCenteredString(font, String.valueOf(gridW), left + 115, top + 36, GuiHelper.TITLE);
        g.drawCenteredString(font, String.valueOf(gridH), left + 115, top + 62, GuiHelper.TITLE);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private static int clamp(int value) {
        return Math.max(1, Math.min(9, value));
    }

    @Override
    public void onClose() {
        Network.finishEditing();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
