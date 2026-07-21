package mod.amrsatrio.sinkholerestorer.mixin;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(Aquifer.NoiseBasedAquifer.class)
public class NoiseBasedAquiferMixin {
    @Final @Mutable @Shadow
    private static int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS;

    @Unique
    private static final int[][] sinkholeRestorer$SURFACE_SAMPLING_OFFSETS_IN_CHUNKS_1_19_3 = new int[][]{
            {0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
    };

    @Unique
    private static final int[][] sinkholeRestorer$SURFACE_SAMPLING_OFFSETS_IN_CHUNKS_PRE_1_19_3 = new int[][]{
            {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
    };

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void sinkholeRestorer$restoreOldSamplingOffsets(CallbackInfo ci) {
        // Restore the 1.19.2 order where {0, 0} was at index 7 instead of 0
        // Check if the current array is of the expected one
        if (Arrays.deepEquals(SURFACE_SAMPLING_OFFSETS_IN_CHUNKS, sinkholeRestorer$SURFACE_SAMPLING_OFFSETS_IN_CHUNKS_1_19_3)) {
            SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = sinkholeRestorer$SURFACE_SAMPLING_OFFSETS_IN_CHUNKS_PRE_1_19_3;
        } else {
            throw new RuntimeException("Aquifer.NoiseBasedAquifer.SURFACE_SAMPLING_OFFSETS_IN_CHUNKS has been updated since 1.19.3, and this mod needs an update to work!");
        }
    }

//? if >=1.21.9 {
    // 1.21.9+ updated min/max calculation in the constructor, restore to original behavior

    /*private static int oldGridX(int blockCoord) {
        return blockCoord >> 4; // previously Math.floorDiv(blockCoord, 16);
    }

    private static int oldGridY(int blockCoord) {
        return Math.floorDiv(blockCoord, 12);
    }

    private static int oldGridZ(int blockCoord) {
        return blockCoord >> 4; // previously Math.floorDiv(blockCoord, 16);
    }

    // First call: minGridX calculation
    // Old: this.gridX(pos.getMinBlockX()) - 1
    // New: gridX(pos.getMinBlockX() + -5) + 0
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Aquifer$NoiseBasedAquifer;gridX(I)I", ordinal = 0))
    private static int redirectGridXMin(int blockCoord) {
        return oldGridX(blockCoord + 5) - 1;
    }

    // Second call: gridSizeX calculation
    // Old: this.gridX(pos.getMaxBlockX()) + 1
    // New: gridX(pos.getMaxBlockX() + -5) + 1
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Aquifer$NoiseBasedAquifer;gridX(I)I", ordinal = 1))
    private static int redirectGridXMax(int blockCoord) {
        return oldGridX(blockCoord + 5);
    }

    // First call: minGridY calculation
    // Old: this.gridY(minBlockY) - 1
    // New: gridY(minBlockY + 1) + -1
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Aquifer$NoiseBasedAquifer;gridY(I)I", ordinal = 0))
    private static int redirectGridYMin(int blockCoord) {
        return oldGridY(blockCoord - 1);
    }

    // Second call
    // Old: this.gridY(minBlockY + yBlockSize) + 1
    // New: gridY(minBlockY + yBlockSize + 1) + 1
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Aquifer$NoiseBasedAquifer;gridY(I)I", ordinal = 1))
    private static int redirectGridYMax(int blockCoord) {
        return oldGridY(blockCoord - 1);
    }

    // First call: minGridZ calculation
    // Old: this.gridZ(pos.getMinBlockZ()) - 1
    // New: gridZ(pos.getMinBlockZ() + -5) + 0
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Aquifer$NoiseBasedAquifer;gridZ(I)I", ordinal = 0))
    private static int redirectGridZMin(int blockCoord) {
        return oldGridZ(blockCoord + 5) - 1;
    }

    // Second call: gridSizeZ calculation
    // Old: this.gridZ(pos.getMaxBlockZ()) + 1
    // New: gridZ(pos.getMaxBlockZ() + -5) + 1
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Aquifer$NoiseBasedAquifer;gridZ(I)I", ordinal = 1))
    private static int redirectGridZMax(int blockCoord) {
        return oldGridZ(blockCoord + 5);
    }*/

    // 1.21.9+ early returns computeSubstance when block Y is above skipSamplingAboveY. Disable this optimization.

    @Mutable @Shadow @Final
    private int skipSamplingAboveY;

    // Overwrite this to Integer.MAX_VALUE to disable the skipSamplingAboveY feature entirely
    @Inject(method = "<init>", at = @At("RETURN"))
    private void sinkholeRestorer$disableSkipSamplingAboveY(
            NoiseChunk noiseChunk,
            ChunkPos pos,
//? if >=26.3-alpha.4 {
            /*Aquifer.Config config,
*///? } else {
            NoiseRouter router,
//? }
            PositionalRandomFactory positionalRandomFactory,
            int minBlockY,
            int yBlockSize,
            Aquifer.FluidPicker globalFluidPicker,
            CallbackInfo ci) {
        this.skipSamplingAboveY = Integer.MAX_VALUE;
    }
//? }
}
