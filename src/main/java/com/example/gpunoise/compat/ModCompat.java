package com.example.gpunoise.compat;

import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, String> KNOWN_COMPAT_MODS = new LinkedHashMap<>();

    static {
        KNOWN_COMPAT_MODS.put("create", "Create");
        KNOWN_COMPAT_MODS.put("valkyrienskies", "Valkyrien Skies");
    }

    private ModCompat() {
    }

    public static void logLoadedCompatMods() {
        ModList modList = ModList.get();
        boolean found = false;
        for (Map.Entry<String, String> entry : KNOWN_COMPAT_MODS.entrySet()) {
            if (modList.isLoaded(entry.getKey())) {
                LOGGER.info("Compatibility mode: detected {} (modId={})", entry.getValue(), entry.getKey());
                found = true;
            }
        }
        if (!found) {
            LOGGER.info("Compatibility mode: no known terrain-altering mods detected.");
        }
    }
}
