package io.github.cottonmc.staticdata;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.IOException;
import java.nio.file.Files;

public final class StaticDataInitializer implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		// PreLaunch is used to create the static_data directory as early as possible.
		// We don't touch any minecraft context during initialization of data sources, so we don't have a high chance of breaking anything.
		// Since mods aren't supposed to be loaded at this point, we can usually assume all data sources being made is fine.
		// Though if someone does something stupid (reflection) the data source should be generated on the fly.
		// Of course static data shouldn't be touched until after preLaunch to prevent class loading order issues (we use Identifier in static data items).
		try {
			Files.createDirectories(StaticData.GLOBAL_DATA_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Initialize all data sources
		DataSource.init();
	}
}
