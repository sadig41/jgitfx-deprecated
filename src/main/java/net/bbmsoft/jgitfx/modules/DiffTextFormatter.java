package net.bbmsoft.jgitfx.modules;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.css.PseudoClass;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.bbmsoft.jgitfx.utils.Resetter;
import net.bbmsoft.jgitfx.utils.Terminator;

public class DiffTextFormatter implements Resetter, Terminator, Consumer<String> {

	private final static PseudoClass PSEUDO_CLASS_DIFF_FILE_HEADER = PseudoClass.getPseudoClass("diff-file-header");
	private final static PseudoClass PSEUDO_CLASS_DIFF_HEADER = PseudoClass.getPseudoClass("diff-header");
	private final static PseudoClass PSEUDO_CLASS_DIFF_ADDED = PseudoClass.getPseudoClass("diff-added");
	private final static PseudoClass PSEUDO_CLASS_DIFF_REMOVED = PseudoClass.getPseudoClass("diff-removed");

	private static final Pattern DIFF_HEADER_PATTERN = Pattern.compile("@@ -[0-9]+,[0-9]+ \\+[0-9]+,[0-9]+ @@",
			Pattern.DOTALL);

	private final TextFlow textFlow;

	private StringBuilder stringBuilder;
	
	private boolean showFileHeader;

	public DiffTextFormatter(TextFlow textFlow) {
		this.textFlow = textFlow;
		this.stringBuilder = new StringBuilder();
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
			Text header = new Text("\n\n" + split[0] + "\n\n");
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
		}
	}

	@Override
	public void accept(String line) {
		this.stringBuilder.append(line).append("\n");
	}

	@Override
	public void reset() {
		this.textFlow.getChildren().clear();
	}

	@Override
	public void terminate() {
		updateText(this.stringBuilder.toString());
		this.stringBuilder = new StringBuilder();
	}
}
