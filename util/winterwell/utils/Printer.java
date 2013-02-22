package winterwell.utils;

public class Printer {

	public static void out(final Object o, final Object... args) {
		if (args.length != 0)
			for (final Object arg : args)
				System.out.println(o + ": " + arg);
		else
			System.out.println(o);
	}

}
