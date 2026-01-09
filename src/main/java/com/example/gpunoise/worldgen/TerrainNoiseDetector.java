package com.example.gpunoise.worldgen;

import com.example.gpunoise.gpu.GpuNoiseEngine;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public final class TerrainNoiseDetector {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TerrainNoiseDetector() {
    }

    public static void inspect(MinecraftServer server, GpuNoiseEngine engine) {
        if (!engine.isEnabled()) {
            LOGGER.info("GPU noise acceleration disabled; skipping terrain inspection.");
            return;
        }
        ServerLevel level = server.overworld();
        if (level == null) {
            LOGGER.warn("No overworld available to inspect terrain generator.");
            return;
        }
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        LOGGER.info("Detected chunk generator: {}", generator.getClass().getName());
        if (generator instanceof NoiseBasedChunkGenerator) {
            LOGGER.info("Noise-based terrain generator detected; GPU acceleration hooks ready.");
        } else {
            LOGGER.info("Non-noise terrain generator detected; GPU acceleration not applied.");
        }
    }
}
