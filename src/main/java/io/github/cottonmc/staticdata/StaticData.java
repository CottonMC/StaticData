package io.github.cottonmc.staticdata;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class StaticData {
	public static final Path GLOBAL_DATA_PATH = FabricLoader.getInstance().getGameDir().resolve("static_data");

	/**
	 * Gets all data available.
	 *
	 * <p>All of it.
	 *
	 * <p>You should probably use a different method instead.
	 * The data is returned in no particular order.
	 */
	@Nonnull
	public static Set<Item> getAll() {
		final Set<StaticData.Item> items = new HashSet<>();

		// Get all static data from mods.
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			try (Stream<Path> data = StaticData.getStaticDataFromMod(mod, path -> true)) {
				data.forEach(datum -> items.add(new ItemImpl(datum, DataSource.mod(mod))));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Get all global data
		try (Stream<Path> data = StaticData.getGlobalData(path -> true)) {
			data.forEach(datum -> items.add(new ItemImpl(datum, DataSource.global())));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Collections.unmodifiableSet(items);
	}

	@Nonnull
	public static Set<StaticData.Item> getAll(String name) {
		final Set<StaticData.Item> items = new HashSet<>();

		// Get the static data from mods.
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			final Path staticDataPath = mod.getRootPath().resolve("static_data");

			if (Files.isDirectory(staticDataPath)) {
				final Path resolved = staticDataPath.resolve(name);

				if (Files.exists(resolved) && !Files.isDirectory(resolved)) {
					items.add(new ItemImpl(resolved, DataSource.mod(mod)));
				}
			}
		}

		// Get the global data of the same name.
		if (Files.isDirectory(StaticData.GLOBAL_DATA_PATH)) {
			final Path resolved = StaticData.GLOBAL_DATA_PATH.resolve(name);

			if (Files.exists(resolved) && !Files.isDirectory(resolved)) {
				items.add(new ItemImpl(resolved, DataSource.global()));
			}
		}

		return Collections.unmodifiableSet(items);
	}

	@Nonnull
	public static Set<StaticData.Item> getAllIn(String... childDirs) {
		Objects.requireNonNull(childDirs, "Child directories array cannot be null");
		final Set<StaticData.Item> items = new HashSet<>();

		// Get static data from mods.
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			try (Stream<Path> data = StaticData.getStaticDataFromMod(mod, path -> {
				for (String childDir : childDirs) {
					path = path.resolve(childDir);

					if (!Files.isDirectory(path)) {
						return false;
					}
				}

				return true;
			})) {
				data.forEach(datum -> items.add(new ItemImpl(datum, DataSource.mod(mod))));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Get global data
		try (Stream<Path> data = StaticData.getGlobalData(path -> {
			for (String childDir : childDirs) {
				path = path.resolve(childDir);

				if (!Files.isDirectory(path)) {
					return false;
				}
			}

			return true;
		})) {
			data.forEach(datum -> items.add(new ItemImpl(datum, DataSource.global())));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Collections.unmodifiableSet(items);
	}

	@Nonnull
	public static Optional<StaticData.Item> getFromMod(ModContainer mod, String name) {
		final Path staticDataPath = mod.getRootPath().resolve("static_data");

		if (Files.isDirectory(staticDataPath)) {
			final Path resolved = staticDataPath.resolve(name);

			if (Files.exists(resolved)) {
				return Optional.of(new ItemImpl(resolved, DataSource.mod(mod)));
			}
		}

		return Optional.empty();
	}

	@Nonnull
	public static Optional<StaticData.Item> getFromGlobal(String name) {
		if (Files.isDirectory(StaticData.GLOBAL_DATA_PATH)) {
			final Path resolved = StaticData.GLOBAL_DATA_PATH.resolve(name);

			if (Files.exists(resolved)) {
				return Optional.of(new ItemImpl(resolved, DataSource.global()));
			}
		}

		return Optional.empty();
	}

	/**
	 * Gets static data from a mod.
	 *
	 * @param mod the mod
	 * @param dataPredicate whether this data should be included in the stream
	 * @return a stream of data
	 */
	@Nonnull
	private static Stream<Path> getStaticDataFromMod(ModContainer mod, Predicate<Path> dataPredicate) throws IOException {
		final Path staticDataPath = mod.getRootPath().resolve("static_data");

		if (Files.isDirectory(staticDataPath)) {
			return Files.walk(staticDataPath).filter(dataPredicate);
		}

		return Stream.empty();
	}

	/**
	 * Gets global static data.
	 *
	 * @param dataPredicate whether this data should be included in the stream
	 * @return a stream of data
	 */
	@Nonnull
	private static Stream<Path> getGlobalData(Predicate<Path> dataPredicate) throws IOException {
		if (Files.isDirectory(StaticData.GLOBAL_DATA_PATH)) {
			return Files.walk(StaticData.GLOBAL_DATA_PATH).filter(dataPredicate);
		}

		return Stream.empty();
	}

	private StaticData() {
	}

	/**
	 * Represents a single file from static data.
	 */
	public interface Item {
		InputStream createInputStream() throws IOException;

		/**
		 * Reads the data as a UTF-8 string, and caches the data for later accesses.
		 *
		 * <p>This method is thread safe.
		 *
		 * @return a string representing the contents of this static data
		 * @throws IOException
		 */
		String getAsString() throws IOException;

		Identifier getId();

		/**
		 * Gets the data source for this item.
		 *
		 * <p>This can be used to determine if this item is from a mod or the global static data.
		 *
		 * @return the data source
		 */
		DataSource getSource();
	}
}
