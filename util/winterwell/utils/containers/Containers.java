package winterwell.utils.containers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

public class Containers {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void plus(final HashMap cont, final String key, final int amount) {
		final Object value = cont.get(key);
		if (value == null)
			cont.put(key, Integer.valueOf(amount));
		else if (value instanceof Number)
			cont.put(key, Integer.valueOf(((Number) value).intValue() + amount));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<String> getSortedKeys(final HashMap<String, ?> cont) {
		final TreeSet<Entry<String, ?>> sortedEntries = new TreeSet(new Comparator<Entry<String, ?>>() {

			@Override
			public int compare(final Entry<String, ?> o1, final Entry<String, ?> o2) {
				final Object v1 = o1.getValue();
				final Object v2 = o2.getValue();
				final int diff = v1 instanceof Number && v2 instanceof Number ? Integer.compare(((Number) v1).intValue(), ((Number) v2).intValue()) : 0;
				return diff != 0 ? diff : o1.getKey().compareTo(o2.getKey());
			}
		});
		sortedEntries.addAll(cont.entrySet());
		final List<String> sortedKeys = new ArrayList<String>(sortedEntries.size());
		for (final Entry<String, ?> e : sortedEntries)
			sortedKeys.add(e.getKey());
		return sortedKeys;
	}

}
