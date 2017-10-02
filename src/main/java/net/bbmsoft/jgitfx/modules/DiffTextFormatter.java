package net.bbmsoft.jgitfx.modules;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.css.PseudoClass;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.bbmsoft.jgitfx.utils.Resetter;
import net.bbmsoft.jgitfx.utils.Terminator;

public class DiffTextFormatter implements Resetter, Terminator {

	private final static PseudoClass PSEUDO_CLASS_DIFF_FILE_HEADER = PseudoClass.getPseudoClass("diff-file-header");
	private final static PseudoClass PSEUDO_CLASS_DIFF_HEADER = PseudoClass.getPseudoClass("diff-header");
	private final static PseudoClass PSEUDO_CLASS_DIFF_ADDED = PseudoClass.getPseudoClass("diff-added");
	private final static PseudoClass PSEUDO_CLASS_DIFF_REMOVED = PseudoClass.getPseudoClass("diff-removed");

	private static final Pattern DIFF_HEADER_PATTERN = Pattern.compile("@@ -[0-9]+,[0-9]+ \\+[0-9]+,[0-9]+ @@",
			Pattern.DOTALL);

	private final TextFlow textFlow;
	private final ByteArrayOutputStream outputStream;
	
	private Charset charSet;
	
	private boolean showFileHeader;

	public DiffTextFormatter(TextFlow textFlow) {
		this.textFlow = textFlow;
		this.outputStream = new ByteArrayOutputStream();
		this.charSet = Charset.forName("UTF-8");
	}

	private void updateText(String wholeText) {

		Matcher matcher = DIFF_HEADER_PATTERN.matcher(wholeText);

		int cursor = 0;

		while (matcher.find()) {
			boolean fileHeader = cursor == 0;
			String match = matcher.group();
			int matchIndex = wholeText.indexOf(match);
			String partition = wholeText.substring(cursor, cursor = matchIndex);
			processPartition(partition, fileHeader);
		}
		// process last part of whole text
		String partition = wholeText.substring(cursor, wholeText.length());
		processPartition(partition, false);
	}

	private void processPartition(String partition, boolean fileHeader) {
		if (fileHeader && this.showFileHeader) {
			Text text = new Text(partition + "\n");
			text.pseudoClassStateChanged(PSEUDO_CLASS_DIFF_FILE_HEADER, true);
			text.getStyleClass().add("diff-partition");
			textFlow.getChildren().add(text);
		} else if(!fileHeader) {
			String[] split = partition.split("\n");
			Text header = new Text(split[0] + "\n\n");
			header.pseudoClassStateChanged(PSEUDO_CLASS_DIFF_HEADER, true);
			header.getStyleClass().add("diff-partition");
			textFlow.getChildren().add(header);
			for (int i = 1; i < split.length; i++) {
				Text text = new Text("\t" + split[i] + "\n");
				text.pseudoClassStateChanged(PSEUDO_CLASS_DIFF_ADDED, split[i].startsWith("+"));
				text.pseudoClassStateChanged(PSEUDO_CLASS_DIFF_REMOVED, split[i].startsWith("-"));
				text.getStyleClass().add("diff-partition");
				textFlow.getChildren().add(text);
			}
			textFlow.getChildren().add(new Text("\n\n"));
		}
	}

	@Override
	public void reset() {
		this.textFlow.getChildren().clear();
	}

	@Override
	public void terminate() {
		String wholeText = new String(this.outputStream.toByteArray(), this.charSet);
		updateText(wholeText);
		this.outputStream.reset();
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public Charset getCharSet() {
		return charSet;
	}

	public void setCharSet(Charset charSet) {
		this.charSet = charSet;
	}
}
