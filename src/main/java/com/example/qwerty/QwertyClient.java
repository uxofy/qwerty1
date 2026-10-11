package com.example.qwerty;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

public class QwertyClient implements ClientModInitializer {
    private static KeyMapping toggleKey;
    private static KeyMapping menuKey;
    private static long nextAttack = 0;
    private static int lastSlot = -1;
    private static boolean axeBlock = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.qwerty.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KeyMapping.Category.MISC));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.qwerty.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(QwertyClient::tick);
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        Config cfg = Config.INSTANCE;

        while (toggleKey.consumeClick()) {
            cfg.enabled = !cfg.enabled;
            cfg.save();
            mc.player.displayClientMessage(
                    Component.literal("Qwerty: " + (cfg.enabled ? "ВКЛ" : "ВЫКЛ")), true);
        }
        while (menuKey.consumeClick()) {
            mc.setScreen(new ConfigScreen());
        }

        // следим за сменой слота: был ли в предыдущем слоте топор
        int slot = mc.player.getInventory().getSelectedSlot();
        if (lastSlot == -1) lastSlot = slot;
        if (slot != lastSlot) {
            axeBlock = mc.player.getInventory().getItem(lastSlot).is(ItemTags.AXES);
            lastSlot = slot;
        }

        if (mc.screen != null) return;
        if (!mc.player.getMainHandItem().is(Items.MACE)) return;
        if (!(mc.hitResult instanceof EntityHitResult hit)) return;

        Entity target = hit.getEntity();
        if (!(target instanceof LivingEntity le) || !le.isAlive() || target instanceof ArmorStand) return;

        if (!cfg.enabled) {
            hint(mc, "выключен (нажми " + toggleKey.getTranslatedKeyMessage().getString() + ")");
            return;
        }
        if (cfg.blockAfterAxe && axeBlock) {
            hint(mc, "не бью: перед этим был слот с топором");
            return;
        }
        if (mc.player.fallDistance < cfg.minHeight) {
            hint(mc, "мало высоты: " + Math.round(mc.player.fallDistance * 10) / 10.0 + " / " + cfg.minHeight);
            return;
        }
        if (cfg.waitCooldown && mc.player.getAttackStrengthScale(0.5f) < 0.95f) {
            hint(mc, "жду перезарядку булавы");
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextAttack) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        hint(mc, "удар!");

        double cps = cfg.minCps + ThreadLocalRandom.current().nextDouble() * (cfg.maxCps - cfg.minCps);
        nextAttack = now + (long) (1000.0 / Math.max(1.0, cps));
    }

    private static void hint(Minecraft mc, String msg) {
        if (Config.INSTANCE.debug && mc.player != null) {
            mc.player.displayClientMessage(Component.literal("Qwerty: " + msg), true);
        }
    }
}
