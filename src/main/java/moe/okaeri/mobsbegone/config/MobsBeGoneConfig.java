package moe.okaeri.mobsbegone.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

import static moe.okaeri.mobsbegone.MobsBeGone.LOGGER;

public class MobsBeGoneConfig {
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("mobsbegone-blacklist.json");
	private static final Type SET_STRING = new TypeToken<Set<String>>(){}.getType();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static long[] loadBlacklist() {
		Set<String> rawStrings = loadBlacklistStrings();

		int maxRegistryRaw = Registries.ENTITY_TYPE.stream().mapToInt(Registries.ENTITY_TYPE::getRawId).max().orElse(8192);
		if (maxRegistryRaw == 8192) {
			LOGGER.warn("MobsBeGone >> Error accessing the registry. Defaulting to 8192 total entities.");
		}
		int bitArraySize = (maxRegistryRaw + 1 + 63) >>> 6;
		long[] lookup = new long[bitArraySize];

		for (String s : rawStrings) {
			try {
				Identifier id = Identifier.tryParse(s);
				if (id == null) {
					LOGGER.warn("MobsBeGone >> Invalid entity ID in config: {}", s);
					continue;
				}
				var type = Registries.ENTITY_TYPE.get(id);
				if (type == null) {
					LOGGER.warn("MobsBeGone >> Unknown entity ID in config: {}", s);
					continue;
				}
				int raw = Registries.ENTITY_TYPE.getRawId(type);
				if (raw >= 0 && raw < (bitArraySize << 6)) {
					lookup[raw >>> 6] |= 1L << (raw & 63);
				}
			} catch (Exception ex) {
				LOGGER.warn("MobsBeGone >> Invalid entity ID in config: {}", s);
			}
		}

		int blacklistedEntities = 0;
		for (long word : lookup) {
			blacklistedEntities += Long.bitCount(word);
		}

		LOGGER.info("MobsBeGone >> Total Entity Types: {} | Bit Array Size: {} longs / {} bits | Blacklisted Entities: {}", maxRegistryRaw, bitArraySize, (bitArraySize << 6), blacklistedEntities);
		return lookup;
	}

	private static Set<String> loadBlacklistStrings() {
		try {
			if (Files.notExists(CONFIG_PATH)) {
				try (InputStream in = getDefaultStream()) {
					if (in == null) throw new IOException("MobsBeGone >> Default config not found!");
					Files.copy(in, CONFIG_PATH);
				}
			}
			String content = Files.readString(CONFIG_PATH);
			return GSON.fromJson(content, SET_STRING);

		} catch (IOException e) {
			LOGGER.error("MobsBeGone >> Failed to load or create blacklist config: {}", e.getMessage(), e);
			try (InputStream in = getDefaultStream()) {
				if (in == null) {
					LOGGER.warn("MobsBeGone >> Fallback default config not found either. Using empty blacklist.");
					return Collections.emptySet();
				}
				LOGGER.warn("MobsBeGone >> Using fallback default config.");
				return GSON.fromJson(new InputStreamReader(in), SET_STRING);
			} catch (IOException inner) {
				LOGGER.error("MobsBeGone >> Failed to load fallback config: {}", inner.getMessage(), inner);
				return Collections.emptySet();
			}
		}
	}

	private static InputStream getDefaultStream() {
		return MobsBeGoneConfig.class.getClassLoader().getResourceAsStream("mobsbegone-blacklist.json");
	}
}
