package com.example.qwerty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("qwerty.json");

    public static Config INSTANCE = load();

    public boolean enabled = false;
    public double minHeight = 0.0;   // мин. высота падения (блоки)
    public double minCps = 8.0;
    public double maxCps = 12.0;
    public boolean waitCooldown = true;
    public boolean debug = true; // подсказки над хотбаром
    public boolean blockAfterAxe = true; // не бить булавой, если предыдущий слот был с топором

    public void fix() {
        minHeight = Math.max(0, Math.min(30, minHeight));
        minCps = Math.max(1, Math.min(20, minCps));
        maxCps = Math.max(1, Math.min(20, maxCps));
        if (minCps > maxCps) minCps = maxCps;
    }

    public static Config load() {
        try {
            if (Files.exists(FILE)) {
                Config c = GSON.fromJson(Files.readString(FILE), Config.class);
                if (c != null) { c.fix(); return c; }
            }
        } catch (Exception ignored) {}
        return new Config();
    }

    public void save() {
        try {
            fix();
            Files.writeString(FILE, GSON.toJson(this));
        } catch (Exception ignored) {}
    }
}
