package io.github.cottonmc.staticdata;

import net.minecraft.util.Identifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class ItemImpl implements StaticData.Item {
	private final Path path;
	private final DataSource source;
	private final Identifier id;
	private volatile String readCache = null;

	ItemImpl(Path path, DataSource source) {
		this.path = path;
		this.source = source;
		this.id = source.createId(path);
	}

	@Override
	public InputStream createInputStream() throws IOException {
		return Files.newInputStream(this.path, StandardOpenOption.READ);
	}

	@Override
	public String getAsString() throws IOException {
		// DCL (Double checked locking)
		if (this.readCache != null) {
			return this.readCache;
		}

		synchronized(this) {
			if (this.readCache != null) {
				return this.readCache;
			}

			if (!Files.exists(this.path)) {
				throw new FileNotFoundException(); //Should never happen
			}

			this.readCache = new String(Files.readAllBytes(this.path), StandardCharsets.UTF_8);
			return this.readCache;
		}
	}

	@Override
	public Identifier getId() {
		return this.id;
	}

	@Override
	public DataSource getSource() {
		return this.source;
	}
}
