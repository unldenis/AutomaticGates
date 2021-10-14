package com.github.unldenis.obj;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Sound;

@Getter
public final class CSound {
    private final Sound sound;
    private final double volume, pitch;
    private final boolean enabled;

    public CSound(@NonNull Sound sound, double volume, double pitch, boolean enabled) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.enabled = enabled;
    }

    public CSound(@NonNull String sound, double volume, double pitch, boolean enabled) {
        this(Sound.valueOf(sound), volume, pitch, enabled);
    }

    public void playSound(@NonNull Location location) {
        location.getWorld().playSound(location, sound, (float) volume, (float) pitch);
    }
}
