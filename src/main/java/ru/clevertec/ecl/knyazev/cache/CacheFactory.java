package ru.clevertec.ecl.knyazev.cache;

import java.util.Locale;

public class CacheFactory {

	public <K, V> Cache<K, V> initCache(String cacheAlgorithm, Integer cacheSize) {
		if (cacheAlgorithm == null) {
			cacheAlgorithm = "";
		}

		CacheAlgorithm algorithm = CacheAlgorithm.valueOf(cacheAlgorithm.toUpperCase(Locale.ROOT));

		return switch (algorithm) {
		case LFU -> new LFUCache<>(cacheSize);
		case LRU -> new LRUCache<>(cacheSize);

		default -> new LRUCache<>(cacheSize);
		};
	}

	private enum CacheAlgorithm {
		LRU, LFU
	}
}
