package com.example.easyrecipes.client;

import com.example.easyrecipes.Category;
import com.example.easyrecipes.net.Network;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The very first screen (opened by /easyrecipes): choose Vanilla or Create. */
public class CategorySelectScreen extends Screen {

    private static final int W = 200;
    private static final int H = 112;

    private int left;
    private int top;

    public CategorySelectScreen() {
        super(Component.translatable("gui.easyrecipes.picker.title"));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;

        addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.category.vanilla"),
                        b -> minecraft.setScreen(new StationSelectScreen(Category.VANILLA)))
                .bounds(left + 40, top + 34, 120, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.category.create"),
                        b -> minecraft.setScreen(new StationSelectScreen(Category.CREATE)))
                .bounds(left + 40, top + 62, 120, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        GuiHelper.panel(g, left, top, W, H);
        g.drawCenteredString(font, title, left + W / 2, top + 12, GuiHelper.TITLE);
        super.render(g, mouseX, mouseY, partialTick);
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
