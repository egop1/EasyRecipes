package com.example.easyrecipes.client;

import com.example.easyrecipes.Category;
import com.example.easyrecipes.Station;
import com.example.easyrecipes.net.Network;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Scrollable station list for one category. Picking an available station opens its recipe list. */
public class StationSelectScreen extends Screen {

    private static final int W = 230;
    private static final int VISIBLE_ROWS = 5;

    private final Category category;
    private int left;
    private int top;
    private int panelH;
    private int totalRows;
    private int scroll = 0;
    private Component status = Component.empty();

    public StationSelectScreen(Category category) {
        super(Component.translatable(category.translationKey()));
        this.category = category;
    }

    private List<Station> stations() {
        List<Station> list = new ArrayList<>();
        for (Station station : Station.values()) {
            if (station.category() == category) {
                list.add(station);
            }
        }
        return list;
    }

    private int maxScroll() {
        return Math.max(0, totalRows - VISIBLE_ROWS);
    }

    @Override
    protected void init() {
        List<Station> stations = stations();
        totalRows = (stations.size() + 1) / 2;
        int visibleRows = Math.min(totalRows, VISIBLE_ROWS);
        panelH = 30 + visibleRows * 26 + 30;
        left = (width - W) / 2;
        top = (height - panelH) / 2;
        scroll = clamp(scroll, 0, maxScroll());

        for (int r = 0; r < visibleRows; r++) {
            int rowIndex = scroll + r;
            for (int c = 0; c < 2; c++) {
                int i = rowIndex * 2 + c;
                if (i >= stations.size()) {
                    continue;
                }
                Station station = stations.get(i);
                int x = left + 12 + c * 106;
                int y = top + 30 + r * 26;
                addRenderableWidget(Button.builder(label(station), b -> onPick(station))
                        .bounds(x, y, 100, 20).build());
            }
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.list.back"),
                        b -> minecraft.setScreen(new CategorySelectScreen()))
                .bounds(left + (W - 70) / 2, top + panelH - 24, 70, 20).build());
    }

    private Component label(Station station) {
        Component name = Component.translatable(station.translationKey());
        return station.available() ? name : name.copy().append(" …");
    }

    private void onPick(Station station) {
        // Every listed station has an editor now, so the only way to be unavailable is a missing Create.
        if (!station.available()) {
            status = Component.translatable("gui.easyrecipes.station.needs_create");
            return;
        }
        minecraft.setScreen(new RecipeListScreen(station));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll() > 0) {
            int old = scroll;
            scroll = clamp(scroll - (int) Math.signum(delta), 0, maxScroll());
            if (scroll != old) {
                rebuildWidgets();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        GuiHelper.panel(g, left, top, W, panelH);
        g.drawCenteredString(font, title, left + W / 2, top + 10, GuiHelper.TITLE);
        super.render(g, mouseX, mouseY, partialTick);

        int maxScroll = maxScroll();
        if (maxScroll > 0) {
            int trackX = left + W - 7;
            int trackY = top + 28;
            int trackH = panelH - 56;
            g.fill(trackX, trackY, trackX + 4, trackY + trackH, 0xFF555555);
            int thumbH = Math.max(12, trackH * VISIBLE_ROWS / totalRows);
            int thumbY = trackY + (trackH - thumbH) * scroll / maxScroll;
            g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }

        if (!status.getString().isEmpty()) {
            g.drawCenteredString(font, status, left + W / 2, top + panelH - 36, GuiHelper.ERROR);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
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
