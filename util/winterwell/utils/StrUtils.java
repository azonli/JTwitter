package winterwell.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StrUtils {

	public static List<String> split(final String longString) {
		return Arrays.asList(longString.split(",*\\s+"));
	}

	public static String join(final List<?> whatever, final String seperator) {
		final StringBuilder sb = new StringBuilder();
		final Iterator<?> it = whatever.iterator();
		if (it.hasNext()) {
			sb.append(it.next());
			while (it.hasNext()) {
				sb.append(seperator);
				sb.append(it.next());
			}
		}
		return sb.toString();
	}

}
