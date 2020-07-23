package io.github.cottonmc.staticdata;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;

import java.io.File;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * Represents the source of a static data item.
 */
public abstract class DataSource {
	private static final DataSource GLOBAL = new Global();
	private static final Map<ModContainer, Mod> MOD_SOURCES = new IdentityHashMap<>();

	private DataSource() {
	}

	static void init() {
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			DataSource.mod(mod);
		}
	}

	/**
	 * Gets the global data source.
	 *
	 * @return the global data source
	 */
	public static DataSource global() {
		return DataSource.GLOBAL;
	}

	/**
	 * Gets the data source for a mod.
	 *
	 * @param mod the mod
	 * @return the data source for the mod
	 */
	public static DataSource mod(ModContainer mod) {
		return DataSource.MOD_SOURCES.computeIfAbsent(Objects.requireNonNull(mod, "ModContainer cannot be null"), DataSource.Mod::new);
	}

	private static String getRelative(Path parent, Path child) {
		return parent.toAbsolutePath().relativize(child)
				.toString()
				.replace(File.separatorChar, '/')
				.toLowerCase(Locale.ROOT)
				.replace(' ', '_');
	}

	public abstract boolean isGlobal();

	public abstract Optional<ModContainer> getMod();

	abstract Identifier createId(Path path);

	private static final class Global extends DataSource {
		private Global() {
		}

		@Override
		public boolean isGlobal() {
			return true;
		}

		@Override
		public Optional<ModContainer> getMod() {
			return Optional.empty();
		}

		@Override
		Identifier createId(Path path) {
			String rel = DataSource.getRelative(StaticData.GLOBAL_DATA_PATH, path);

			if (rel.startsWith("static_data/")) { //Should always be true
				rel = rel.substring("static_data/".length());
			}

			return new Identifier("global", rel);
		}

		@Override
		public String toString() {
			return "global";
		}
	}

	private static final class Mod extends DataSource {
		private final ModContainer mod;

		private Mod(ModContainer mod) {
			this.mod = mod;
		}

		@Override
		public boolean isGlobal() {
			return false;
		}

		@Override
		public Optional<ModContainer> getMod() {
			return Optional.of(this.mod);
		}

		@Override
		Identifier createId(Path path) {
			String rel = DataSource.getRelative(this.mod.getRootPath(), path);

			if (rel.startsWith("static_data/")) { //Should always be true
				rel = rel.substring("static_data/".length());
			}

			return new Identifier(this.mod.getMetadata().getId(), rel);
		}

		@Override
		public String toString() {
			return "Mod: " + this.mod.getMetadata().getId();
		}
	}
}
