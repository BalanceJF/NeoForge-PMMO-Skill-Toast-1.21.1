package net.drog701.pmmoskillcommand;

public class ToastConfig {
    public String texture = "pmmoskillcommand:textures/gui/toast.png";
    public AnimationConfig animation = new AnimationConfig();
    public String sound = "pmmoskillcommand:toast_level_up";

    public static class AnimationConfig {
        public int frames = 1;      // 1 = static image, >1 = animated
        public int frame_time = 20; // Ticks per frame (1/20th sec each)
    }
}