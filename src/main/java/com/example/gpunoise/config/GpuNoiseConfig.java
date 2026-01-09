package com.example.gpunoise.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class GpuNoiseConfig {
    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        SPEC = builder.build();
    }

    public static final class Common {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue preferGpu;
        public final ForgeConfigSpec.IntValue batchSize;
        public final ForgeConfigSpec.BooleanValue parallelCpuFallback;

        private Common(ForgeConfigSpec.Builder builder) {
            builder.push("gpu_noise");
            enabled = builder.comment("Enable GPU noise acceleration hooks.")
                .define("enabled", true);
            preferGpu = builder.comment("Prefer GPU backend when available.")
                .define("preferGpu", true);
            batchSize = builder.comment("Batch size used for GPU/CPU noise sampling.")
                .defineInRange("batchSize", 4096, 256, 262144);
            parallelCpuFallback = builder.comment("Allow parallel CPU fallback sampling.")
                .define("parallelCpuFallback", true);
            builder.pop();
        }
    }

    private GpuNoiseConfig() {
    }
}
