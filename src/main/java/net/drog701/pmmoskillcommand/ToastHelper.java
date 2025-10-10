package net.drog701.pmmoskillcommand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ToastHelper {
    public static final SystemToast.SystemToastId PMMO_SKILL = new SystemToast.SystemToastId();

    @OnlyIn(Dist.CLIENT)
    public static void showSkillToast(String skill, int level) {
        ToastComponent var10000 = Minecraft.getInstance().getToasts();
        SystemToast.SystemToastId var10003 = PMMO_SKILL;
        MutableComponent var10004 = Component.literal("Skill Leveled Up!");
        String var10005 = capitalize(skill);
        var10000.addToast(new SystemToast(var10003, var10004, Component.literal(var10005 + " " + level)));
    }

    private static String capitalize(String str) {
        if (str != null && !str.isEmpty()) {
            String var10000 = str.substring(0, 1).toUpperCase();
            return var10000 + str.substring(1);
        } else {
            return str;
        }
    }
}