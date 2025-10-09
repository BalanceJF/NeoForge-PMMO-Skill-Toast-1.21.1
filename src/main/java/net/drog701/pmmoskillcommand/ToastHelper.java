package net.drog701.pmmoskillcommand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

public class ToastHelper {
    public static final SystemToast.SystemToastId PMMO_SKILL = new SystemToast.SystemToastId();

    @OnlyIn(Dist.CLIENT)
    public static void showSkillToast(String skill, int level) {
        Minecraft.getInstance().getToasts().addToast(new SystemToast(
                PMMO_SKILL,
                Component.literal("Skill Leveled Up!"),
                Component.literal(capitalize(skill) + " " + level)
        ));
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}