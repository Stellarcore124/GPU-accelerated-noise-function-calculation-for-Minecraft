package com.example.gpunoise;

import com.example.gpunoise.config.GpuNoiseConfig;
import com.example.gpunoise.gpu.GpuNoiseEngine;
import com.example.gpunoise.worldgen.TerrainNoiseDetector;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(GpuNoiseMod.MOD_ID)
public class GpuNoiseMod {
    public static final String MOD_ID = "gpunoise";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GpuNoiseEngine engine;

    public GpuNoiseMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GpuNoiseConfig.SPEC);
        this.engine = new GpuNoiseEngine(GpuNoiseConfig.COMMON);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("GPU Noise mod initialized.");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        TerrainNoiseDetector.inspect(event.getServer(), engine);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("gpunoise_bench")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                int samples = engine.runBenchmark();
                context.getSource().sendSuccess(() -> engine.benchmarkMessage(samples), true);
                return 1;
            }));
    }
}
