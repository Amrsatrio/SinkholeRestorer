package mod.amrsatrio.sinkholerestorer.fabric;

//? fabric {
import mod.amrsatrio.sinkholerestorer.SinkholeRestorer;
import net.fabricmc.api.ModInitializer;

public class FabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        SinkholeRestorer.onInitialize();
    }
}
//? }
