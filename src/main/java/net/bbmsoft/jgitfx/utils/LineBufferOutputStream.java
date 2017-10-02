package net.bbmsoft.jgitfx.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class LineBufferOutputStream extends OutputStream {
	
	private final Consumer<String> lineConsumer;
	private final Charset charset;
	
	private StringBuilder stringBuilder;

	public LineBufferOutputStream(Consumer<String> lineConsumer) {
		this(lineConsumer, Charset.forName("UTF-8"));
	}

	public LineBufferOutputStream(Consumer<String> lineConsumer, final Charset charset) {
		this.lineConsumer = lineConsumer;
		this.charset = charset;
		this.stringBuilder = new StringBuilder();
	}

	@Override
	public void write(int b) throws IOException {
		new String(new byte[] {(byte)b}, this.charset);
		String strg = String.valueOf((char)b);
		if("\n".equals(strg)) {
			String line = this.stringBuilder.toString();
			this.lineConsumer.accept(line);
			this.stringBuilder = new StringBuilder();
		} else {
			this.stringBuilder.append(strg);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + this.stringBuilder + "]";
	}

}
