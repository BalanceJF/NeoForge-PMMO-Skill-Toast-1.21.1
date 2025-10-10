package net.drog701.pmmoskillcommand;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CustomToast implements Toast {
    private final String title;
    private final String desc;
    private final ItemStack icon;
    private boolean playedSound = false;

    public CustomToast(String title, String desc, ItemStack icon) {
        this.title = title;
        this.desc = desc;
        this.icon = icon;
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastGui, long time) {
        ToastConfig cfg = ToastConfigLoader.getConfig();

        // Texture and animation
        ResourceLocation texture = ResourceLocation.tryParse(cfg.texture);
        int frames = Math.max(cfg.animation.frames, 1);
        int frameTime = Math.max(cfg.animation.frame_time, 1);
        int frame = (int)((time / frameTime) % frames);

        // Draw background (assuming 160x32 per frame, vertically stacked)
        RenderSystem.setShaderTexture(0, texture);
        int texY = frame * 32;
        guiGraphics.blit(texture, 0, 0, 0, texY, this.width(), this.height());

        // Draw icon (optional)
        if (!icon.isEmpty())
            guiGraphics.renderItem(icon, 8, 8);

        // Draw text
        guiGraphics.drawString(Minecraft.getInstance().font, title, 30, 7, 0xFFFFFF, false);
        guiGraphics.drawString(Minecraft.getInstance().font, desc, 30, 18, 0xAAAAAA, false);

        // Play sound once
        if (!playedSound) {
            playedSound = true;
            ResourceLocation soundId = ResourceLocation.tryParse(cfg.sound);
            Minecraft.getInstance().player.playSound(
                    net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(soundId),
                    1.0F, 1.0F
            );
        }

        return time >= 120L ? Visibility.HIDE : Visibility.SHOW;
    }
}