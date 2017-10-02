package net.bbmsoft.jgitfx.modules;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
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

	private final List<Node> diffTextContainer;
	private final ByteArrayOutputStream outputStream;
	
	private Charset charSet;
	
	private boolean showFileHeader;

	public DiffTextFormatter(List<Node> diffTextContainer) {
		this.diffTextContainer = diffTextContainer;
		this.outputStream = new ByteArrayOutputStream();
		this.charSet = Charset.forName("UTF-8");
		this.showFileHeader = false;
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
		
		if (fileHeader && !this.showFileHeader) {
			return;
		}
		
		TextFlow partitionFlow = new TextFlow();
		partitionFlow.getStyleClass().add("diff-partition");
		
		if (fileHeader) {
			Text text = new Text(partition);
			text.getStyleClass().add("text");
			text.pseudoClassStateChanged(PSEUDO_CLASS_DIFF_FILE_HEADER, true);
			partitionFlow.getChildren().add(text);
			partitionFlow.getStyleClass().add("diff-partition-file-header");
			this.diffTextContainer.add(partitionFlow);
		} else {
			
			String[] split = partition.split("\n");
			Text header = new Text(split[0]);
			header.getStyleClass().add("text");
			header.pseudoClassStateChanged(PSEUDO_CLASS_DIFF_HEADER, true);
			partitionFlow.getChildren().add(header);
			partitionFlow.getStyleClass().add("diff-partition-header");
			this.diffTextContainer.add(partitionFlow);
			
			VBox partitionBody = new VBox();
			partitionBody.getStyleClass().add("diff-partition");
			partitionBody.getStyleClass().add("diff-partition-body");
			this.diffTextContainer.add(partitionBody);
			
			TextFlow noChangeFlow = null, addedFlow = null, removedFlow = null;
			
			for (int i = 1; i < split.length; i++) {
				
				Text text = new Text(split[i]);
				
				boolean added = split[i].startsWith("+");
				boolean removed = split[i].startsWith("-");
				
				if(added) {
					
					if(noChangeFlow != null) {
						partitionBody.getChildren().add(noChangeFlow);
						noChangeFlow = null;
					}
					if(addedFlow == null) {
						addedFlow = new TextFlow();
						addedFlow.getStyleClass().add("diff-sub-partition");
						addedFlow.pseudoClassStateChanged(PSEUDO_CLASS_DIFF_ADDED, true);
					} else {
						addedFlow.getChildren().add(new Text("\n"));
					}
					if(removedFlow != null) {
						partitionBody.getChildren().add(removedFlow);
						removedFlow = null;
					}

					addedFlow.getChildren().add(text);
					
				} else if(removed) {
					
					if(noChangeFlow != null) {
						partitionBody.getChildren().add(noChangeFlow);
						noChangeFlow = null;
					}
					if(addedFlow != null) {
						partitionBody.getChildren().add(addedFlow);
						addedFlow = null;
					}
					if(removedFlow == null) {
						removedFlow = new TextFlow();
						removedFlow.getStyleClass().add("diff-sub-partition");
						removedFlow.pseudoClassStateChanged(PSEUDO_CLASS_DIFF_REMOVED, true);
					} else {
						removedFlow.getChildren().add(new Text("\n"));
					}
					
					removedFlow.getChildren().add(text);
				} else {
					
					if(noChangeFlow == null) {
						noChangeFlow = new TextFlow();
						noChangeFlow.getStyleClass().add("diff-sub-partition");
					} else {
						noChangeFlow.getChildren().add(new Text("\n"));
					}
					if(addedFlow != null) {
						partitionBody.getChildren().add(addedFlow);
						addedFlow = null;
					}
					if(removedFlow != null) {
						partitionBody.getChildren().add(removedFlow);
						removedFlow = null;
					}
					
					noChangeFlow.getChildren().add(text);
				}
				
				text.getStyleClass().add("text");
			}

			
			if(noChangeFlow != null) {
				partitionBody.getChildren().add(noChangeFlow);
				noChangeFlow = null;
			}
			if(addedFlow != null) {
				partitionBody.getChildren().add(addedFlow);
				addedFlow = null;
			}
			if(removedFlow != null) {
				partitionBody.getChildren().add(removedFlow);
				removedFlow = null;
			}
		}		
	}

	@Override
	public void reset() {
		this.diffTextContainer.clear();
	}

	@Override
	public void terminate() {
		try {
			String wholeText = new String(this.outputStream.toByteArray(), this.charSet);
			updateText(wholeText);
		} finally {
			this.outputStream.reset();
		}
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

	public boolean isShowFileHeader() {
		return showFileHeader;
	}

	public void setShowFileHeader(boolean showFileHeader) {
		this.showFileHeader = showFileHeader;
	}
}
