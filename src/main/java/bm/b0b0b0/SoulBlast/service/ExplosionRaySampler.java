package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.ExplosionAlgorithmSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionLimits;
import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.model.ExplosionRaySamplingProgress;
import bm.b0b0b0.SoulBlast.util.BlockCoordPacker;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public final class ExplosionRaySampler {

    private final BlastResistanceService resistanceService;
    private final ExplosionBlockInclusion inclusion;
    private final Random random = new Random();

    public ExplosionRaySampler(BlastResistanceService resistanceService, ExplosionBlockInclusion inclusion) {
        this.resistanceService = resistanceService;
        this.inclusion = inclusion;
    }

    public void beginSampling(ExplosionJob job, ExplosionLimits limits) {
        ExplosionSettings settings = job.getDynamite().explosion;
        ExplosionRaySamplingProgress progress = job.getRaySampling();
        progress.reset();
        if (!settings.breakBlocks) {
            return;
        }
        if (job.getCenter().getWorld() == null) {
            return;
        }
        if ("EXTREME".equalsIgnoreCase(settings.quality)
                || ExplosionWaveSampler.usesWave(settings)) {
            progress.active = false;
            progress.rayOverlayActive = false;
            return;
        }
        progress.active = true;
        int desiredRays = settings.samplingRayOverride > 0
                ? settings.samplingRayOverride
                : rayCount(settings);
        progress.totalRays = Math.min(desiredRays, Math.max(64, limits.maxSamplingRays()));
        progress.nextRay = 0;
        progress.steps = Integer.MAX_VALUE;
    }

    public int continueSampling(ExplosionJob job, ExplosionLimits limits, int stepBudget) {
        ExplosionRaySamplingProgress progress = job.getRaySampling();
        if (!progress.active || stepBudget <= 0) {
            return stepBudget;
        }
        ExplosionSettings settings = job.getDynamite().explosion;
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            progress.active = false;
            return stepBudget;
        }
        int maxBlocks = Math.max(500, (int) (limits.maxBlocksPerExplosion() * settings.blockBudgetMultiplier));
        ExplosionAlgorithmSettings algorithm = settings.algorithm;
        float minimumPower = algorithm.minimumRayPower;
        double step = algorithm.rayStep;
        double resistanceScale = algorithm.resistanceMultiplier;
        float rayStrength = rayStrength(settings);
        double cx = center.getX();
        double cy = center.getY();
        double cz = center.getZ();
        int spent = 0;
        while (spent < stepBudget && progress.nextRay < progress.totalRays) {
            if (progress.steps >= progress.maxSteps || progress.power <= minimumPower) {
                startNextRay(progress, cx, cy, cz, rayStrength, algorithm, settings, step);
                progress.nextRay++;
                continue;
            }
            if (progress.seen.size() >= maxBlocks) {
                progress.active = false;
                break;
            }
            int bx = (int) Math.floor(progress.x);
            int by = (int) Math.floor(progress.y);
            int bz = (int) Math.floor(progress.z);
            int minY = world.getMinHeight();
            int maxY = world.getMaxHeight() - 1;
            if (by < minY || by > maxY) {
                progress.power -= 0.45f * (float) step;
            } else if (!world.isChunkLoaded(bx >> 4, bz >> 4)) {
                progress.power -= 0.45f * (float) step;
            } else {
                Block block = world.getBlockAt(bx, by, bz);
                float resistance = effectiveResistance(block, resistanceScale);
                progress.power -= (resistance + 0.3f) * (float) step;
                if (progress.power > minimumPower && inclusion.includes(job, block)) {
                    recordRayHit(job, progress, bx, by, bz, false);
                }
            }
            progress.x += progress.ox * step;
            progress.y += progress.oy * step;
            progress.z += progress.oz * step;
            progress.steps++;
            spent++;
        }
        if (progress.nextRay >= progress.totalRays) {
            progress.active = false;
        }
        return stepBudget - spent;
    }

    public void beginWaveOverlay(ExplosionJob job, ExplosionLimits limits) {
        ExplosionSettings settings = job.getDynamite().explosion;
        ExplosionRaySamplingProgress progress = job.getRaySampling();
        if (!settings.breakBlocks || !settings.algorithm.waveRayOverlay) {
            return;
        }
        if (job.getCenter().getWorld() == null) {
            return;
        }
        progress.rayOverlayActive = true;
        progress.nextRay = 0;
        progress.steps = Integer.MAX_VALUE;
        int desired = settings.algorithm.waveRayOverlayRays > 0
                ? settings.algorithm.waveRayOverlayRays
                : overlayRayCount(settings);
        progress.totalRays = Math.min(desired, Math.max(48, limits.maxSamplingRays() / 2));
    }

    public int continueWaveOverlay(ExplosionJob job, ExplosionLimits limits, int stepBudget) {
        ExplosionRaySamplingProgress progress = job.getRaySampling();
        if (!progress.rayOverlayActive || stepBudget <= 0) {
            return stepBudget;
        }
        ExplosionSettings settings = job.getDynamite().explosion;
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            progress.rayOverlayActive = false;
            return stepBudget;
        }
        int maxBlocks = Math.max(500, (int) (limits.maxBlocksPerExplosion() * settings.blockBudgetMultiplier));
        ExplosionAlgorithmSettings algorithm = settings.algorithm;
        float minimumPower = algorithm.minimumRayPower;
        double step = algorithm.rayStep;
        double resistanceScale = algorithm.resistanceMultiplier;
        float rayStrength = rayStrength(settings);
        double cx = center.getX();
        double cy = center.getY();
        double cz = center.getZ();
        int spent = 0;
        while (spent < stepBudget && progress.nextRay < progress.totalRays) {
            if (progress.steps >= progress.maxSteps || progress.power <= minimumPower) {
                startNextRay(progress, cx, cy, cz, rayStrength, algorithm, settings, step);
                progress.nextRay++;
                continue;
            }
            if (progress.seen.size() >= maxBlocks) {
                progress.rayOverlayActive = false;
                break;
            }
            int bx = (int) Math.floor(progress.x);
            int by = (int) Math.floor(progress.y);
            int bz = (int) Math.floor(progress.z);
            int minY = world.getMinHeight();
            int maxY = world.getMaxHeight() - 1;
            if (by < minY || by > maxY) {
                progress.power -= 0.45f * (float) step;
            } else if (!world.isChunkLoaded(bx >> 4, bz >> 4)) {
                progress.power -= 0.45f * (float) step;
            } else {
                Block block = world.getBlockAt(bx, by, bz);
                float resistance = effectiveResistance(block, resistanceScale);
                progress.power -= (resistance + 0.3f) * (float) step;
                if (progress.power > minimumPower && inclusion.includes(job, block)) {
                    recordRayHit(job, progress, bx, by, bz, true);
                }
            }
            progress.x += progress.ox * step;
            progress.y += progress.oy * step;
            progress.z += progress.oz * step;
            progress.steps++;
            spent++;
        }
        if (progress.nextRay >= progress.totalRays) {
            progress.rayOverlayActive = false;
        }
        return stepBudget - spent;
    }

    private void recordRayHit(
            ExplosionJob job,
            ExplosionRaySamplingProgress progress,
            int x,
            int y,
            int z,
            boolean overlay
    ) {
        long key = BlockCoordPacker.pack(x, y, z);
        if (overlay) {
            progress.rayBoosted.add(key);
        }
        if (progress.seen.add(key)) {
            job.getPendingBlocks().addLast(new ExplosionJob.BlockTarget(x, y, z));
        }
    }

    private int overlayRayCount(ExplosionSettings settings) {
        int shell = shellRayCount(settings.radius);
        return Math.max(72, (int) (shell * 0.55f));
    }

    private void startNextRay(
            ExplosionRaySamplingProgress progress,
            double cx,
            double cy,
            double cz,
            float rayStrength,
            ExplosionAlgorithmSettings algorithm,
            ExplosionSettings settings,
            double step
    ) {
        double ox = random.nextDouble() * 2.0 - 1.0;
        double oy = random.nextDouble() * 2.0 - 1.0;
        double oz = random.nextDouble() * 2.0 - 1.0;
        double length = Math.sqrt(ox * ox + oy * oy + oz * oz);
        if (length < 1.0E-4) {
            progress.maxSteps = 0;
            progress.steps = 1;
            return;
        }
        progress.ox = ox / length;
        progress.oy = oy / length;
        progress.oz = oz / length;
        progress.power = rayStrength * (0.75f + 0.5f * random.nextFloat()) * (float) algorithm.rayRandomness;
        progress.x = cx;
        progress.y = cy;
        progress.z = cz;
        progress.steps = 0;
        progress.maxSteps = maxRaySteps(settings, step);
    }

    private float rayStrength(ExplosionSettings settings) {
        float scale = switch (settings.quality.toUpperCase()) {
            case "EXTREME" -> 0.62f;
            case "HIGH" -> 0.52f;
            default -> 0.48f;
        };
        return settings.radius * Math.max(1.0f, settings.power) * scale;
    }

    private float effectiveResistance(Block block, double resistanceScale) {
        float raw = resistanceService.resolve(block);
        float normalized;
        if (raw <= 0f) {
            normalized = 0.2f;
        } else if (raw < 12f) {
            normalized = raw * 0.35f;
        } else {
            normalized = 2.5f + (float) Math.log10(raw) * 1.8f;
        }
        return Math.min(normalized * (float) resistanceScale, 9f);
    }

    private int maxRaySteps(ExplosionSettings settings, double step) {
        int alongRadius = (int) Math.ceil(settings.radius / step);
        int cap = "EXTREME".equalsIgnoreCase(settings.quality) ? 480 : 160;
        return Math.min(cap, Math.max(64, alongRadius * 8));
    }

    private int rayCount(ExplosionSettings settings) {
        int shell = shellRayCount(settings.radius);
        return switch (settings.quality.toUpperCase()) {
            case "LOW" -> Math.max(80, shell / 2);
            case "HIGH" -> shell + shell / 2;
            case "EXTREME" -> shell * 2 + shell / 4;
            default -> Math.max(128, shell);
        };
    }

    private int shellRayCount(float radius) {
        int cube = Math.max(10, (int) Math.ceil(radius * 1.6f));
        int total = cube * cube * cube;
        int inner = Math.max(0, cube - 2);
        int innerVolume = inner * inner * inner;
        return Math.max(128, total - innerVolume);
    }

}
