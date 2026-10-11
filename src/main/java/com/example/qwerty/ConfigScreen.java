package com.example.qwerty;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ConfigScreen extends Screen {
    public ConfigScreen() {
        super(Component.literal("Qwerty"));
    }

    @Override
    protected void init() {
        Config c = Config.INSTANCE;
        int w = 200, x = this.width / 2 - w / 2, y = this.height / 2 - 95;

        addRenderableWidget(Button.builder(label("Включено", c.enabled), b -> {
            c.enabled = !c.enabled;
            b.setMessage(label("Включено", c.enabled));
        }).bounds(x, y, w, 20).build());

        addRenderableWidget(new ValueSlider(x, y + 26, w, "Мин. высота удара", 0, 30, 0.5, c.minHeight,
                v -> c.minHeight = v));
        addRenderableWidget(new ValueSlider(x, y + 52, w, "Мин. КПС", 1, 20, 0.5, c.minCps,
                v -> c.minCps = v));
        addRenderableWidget(new ValueSlider(x, y + 78, w, "Макс. КПС", 1, 20, 0.5, c.maxCps,
                v -> c.maxCps = v));

        addRenderableWidget(Button.builder(label("Ждать перезарядку", c.waitCooldown), b -> {
            c.waitCooldown = !c.waitCooldown;
            b.setMessage(label("Ждать перезарядку", c.waitCooldown));
        }).bounds(x, y + 104, w, 20).build());

        addRenderableWidget(Button.builder(label("Не бить после топора", c.blockAfterAxe), b -> {
            c.blockAfterAxe = !c.blockAfterAxe;
            b.setMessage(label("Не бить после топора", c.blockAfterAxe));
        }).bounds(x, y + 130, w, 20).build());

        addRenderableWidget(Button.builder(label("Подсказки", c.debug), b -> {
            c.debug = !c.debug;
            b.setMessage(label("Подсказки", c.debug));
        }).bounds(x, y + 156, w, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Готово"), b -> onClose())
                .bounds(x, y + 186, w, 20).build());
    }

    private static Component label(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "да" : "нет"));
    }

    @Override
    public void onClose() {
        Config.INSTANCE.save();
        super.onClose();
    }

    private static class ValueSlider extends AbstractSliderButton {
        private final String label;
        private final double min, max, step;
        private final Consumer<Double> setter;

        ValueSlider(int x, int y, int w, String label, double min, double max, double step,
                    double current, Consumer<Double> setter) {
            super(x, y, w, 20, Component.empty(), (current - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.setter = setter;
            updateMessage();
        }

        private double real() {
            double v = min + this.value * (max - min);
            return Math.round(v / step) * step;
        }

        @Override
        protected void updateMessage() {
            if (label == null) return;
            setMessage(Component.literal(label + ": " + real()));
        }

        @Override
        protected void applyValue() {
            setter.accept(real());
        }
    }
}
