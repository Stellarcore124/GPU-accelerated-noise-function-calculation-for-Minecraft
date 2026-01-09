package com.example.gpunoise.gpu;

import com.example.gpunoise.config.GpuNoiseConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.Locale;
import java.util.stream.IntStream;

public class GpuNoiseEngine {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GpuNoiseConfig.Common config;
    private final NoiseBackend gpuBackend;
    private final NoiseBackend cpuBackend;

    public GpuNoiseEngine(GpuNoiseConfig.Common config) {
        this.config = config;
        this.gpuBackend = new OpenClProbeBackend();
        this.cpuBackend = new ParallelCpuBackend(config);
    }

    public boolean isEnabled() {
        return config.enabled.get();
    }

    public NoiseBackend selectBackend() {
        if (config.preferGpu.get() && gpuBackend.isAvailable()) {
            return gpuBackend;
        }
        return cpuBackend;
    }

    public int runBenchmark() {
        if (!isEnabled()) {
            LOGGER.info("GPU noise acceleration disabled; skipping benchmark.");
            return 0;
        }
        NoiseBackend backend = selectBackend();
        int batch = config.batchSize.get();
        RandomSource random = RandomSource.create();
        SimplexNoise noise = new SimplexNoise(random);
        float[] positions = new float[batch * 3];
        for (int i = 0; i < batch; i++) {
            positions[i * 3] = Mth.nextFloat(random, -2048.0f, 2048.0f);
            positions[i * 3 + 1] = Mth.nextFloat(random, 0.0f, 256.0f);
            positions[i * 3 + 2] = Mth.nextFloat(random, -2048.0f, 2048.0f);
        }
        float[] samples = backend.sample(noise, positions);
        float checksum = 0.0f;
        for (float sample : samples) {
            checksum += sample;
        }
        LOGGER.info("Noise benchmark using {} complete. checksum={}", backend.name(), checksum);
        return samples.length;
    }

    public Component benchmarkMessage(int samples) {
        NoiseBackend backend = selectBackend();
        String message = String.format(Locale.ROOT,
            "GPU Noise benchmark: backend=%s samples=%d enabled=%s",
            backend.name(), samples, isEnabled());
        return Component.literal(message);
    }

    public interface NoiseBackend {
        boolean isAvailable();

        String name();

        float[] sample(SimplexNoise noise, float[] positions);
    }

    private static class ParallelCpuBackend implements NoiseBackend {
        private final GpuNoiseConfig.Common config;

        private ParallelCpuBackend(GpuNoiseConfig.Common config) {
            this.config = config;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public String name() {
            return config.parallelCpuFallback.get() ? "cpu-parallel" : "cpu-single";
        }

        @Override
        public float[] sample(SimplexNoise noise, float[] positions) {
            int count = positions.length / 3;
            float[] out = new float[count];
            IntStream range = IntStream.range(0, count);
            if (config.parallelCpuFallback.get()) {
                range = range.parallel();
            }
            range.forEach(i -> {
                int index = i * 3;
                out[i] = (float) noise.getValue(
                    positions[index],
                    positions[index + 1],
                    positions[index + 2]);
            });
            return out;
        }
    }

    private static class OpenClProbeBackend implements NoiseBackend {
        private boolean available;

        private OpenClProbeBackend() {
            this.available = probe();
        }

        private boolean probe() {
            try {
                System.loadLibrary("OpenCL");
                LOGGER.info("OpenCL library detected; GPU backend available.");
                return true;
            } catch (UnsatisfiedLinkError error) {
                LOGGER.info("OpenCL library not found; using CPU fallback.");
                return false;
            }
        }

        @Override
        public boolean isAvailable() {
            return available;
        }

        @Override
        public String name() {
            return "gpu-opencl-probe";
        }

        @Override
        public float[] sample(SimplexNoise noise, float[] positions) {
            int count = positions.length / 3;
            float[] out = new float[count];
            for (int i = 0; i < count; i++) {
                int index = i * 3;
                out[i] = (float) noise.getValue(
                    positions[index],
                    positions[index + 1],
                    positions[index + 2]);
            }
            return out;
        }
    }
}
