package winterwell.utils.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import winterwell.jtwitter.guts.Base64Encoder;

public class XStreamBinaryConverter {

	public String toString(final Object o) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
			return Base64Encoder.encode(baos.toByteArray());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}

	public Object fromString(final String s) {
		final byte[] bytes = Base64Encoder.decode(s);
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try {
			final ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

	}
}
