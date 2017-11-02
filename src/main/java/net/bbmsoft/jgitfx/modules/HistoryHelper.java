package net.bbmsoft.jgitfx.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.bbmsoft.fxtended.annotations.app.launcher.Subapplication;
import net.bbmsoft.jgitfx.utils.HeadComparator;
import net.bbmsoft.jgitfx.utils.HeadInfo;

public class HistoryHelper extends Subapplication {

	private VBox root;
	private Canvas[] canvas;

	public HistoryHelper() {
		Subapplication.launch(this);
	}

	public void visualize(Repository repository, List<RevCommit> commits) {

		try {
			doVisualize(commits, repository);
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void doVisualize(List<RevCommit> commits, Repository repo)
			throws MissingObjectException, IncorrectObjectTypeException, IOException {
		
		if(this.root == null) {
			return;
		}
		
		this.canvas = new Canvas[commits.size()];
		for (int i = 0; i < canvas.length; i++) {
			canvas[i] = new Canvas(600, 24);
		}
		this.root.getChildren().setAll(canvas);

		Pattern branchNamePattern = Pattern.compile("refs\\/((heads)\\/|remotes\\/(.+)\\/)(.+)");

		List<HeadInfo> heads = new ArrayList<>();
		Map<ObjectId, HeadInfo> branchedCommits = new HashMap<>();
		Map<RevCommit, List<RevCommit>> pendingConnections = new HashMap<>();

		Map<String, Ref> allRefs = repo.getAllRefs();

		try (RevWalk walk = new RevWalk(repo)) {

			Ref head = allRefs.get(Constants.HEAD);
			for (Entry<String, Ref> entry : allRefs.entrySet()) {
				Matcher matcher = branchNamePattern.matcher(entry.getKey());
				if (matcher.matches()) {

					String branchName = matcher.group(4);
					boolean local = "heads".equals(matcher.group(2));

					Ref ref = entry.getValue();
					registerHead(heads, branchedCommits, walk, entry.getKey(), ref, branchName, local,
							ref.getObjectId().equals(head.getObjectId()));
				}
			}
			heads.sort(new HeadComparator());
		}

		int index = 0;

		for (RevCommit next : commits) {
			HeadInfo head = branchedCommits.get(next.getId());
			detectParentBranches(next, branchedCommits, head, pendingConnections);
		}

		for (HeadInfo head : new ArrayList<>(heads)) {
			if (head.isEmpty()) {
				heads.remove(head);
			}
		}

		for (RevCommit next : commits) {

			HeadInfo head = branchedCommits.get(next.getId());

			int branchIndex = heads.indexOf(head);

			Color[] colors = { Color.RED, Color.AQUA, Color.GREEN, Color.YELLOW, Color.PURPLE, Color.DARKCYAN };
			Color color = colors[branchIndex % colors.length];

			Canvas canvas = this.canvas[index++];
			renderLines(commits, canvas.getGraphicsContext2D(), heads, branchedCommits, pendingConnections, canvas.getHeight(), next, color, heads.indexOf(head), next.getParentCount(), 1);
		}

		index = 0;

		for (RevCommit next : commits) {

			HeadInfo head = branchedCommits.get(next.getId());

			int branchIndex = heads.indexOf(head);

			Color[] colors = { Color.RED, Color.AQUA, Color.GREEN, Color.YELLOW, Color.PURPLE, Color.DARKCYAN };
			Color color = colors[branchIndex % colors.length];

			Canvas canvas = this.canvas[index++];
			randerCommit(canvas.getGraphicsContext2D(), canvas.getHeight(), next, color, heads.indexOf(head));
		}
	}

	private void renderLines(List<RevCommit> commits, GraphicsContext g, List<HeadInfo> heads,
			Map<ObjectId, HeadInfo> branchedCommits, Map<RevCommit, List<RevCommit>> pendingConnections,
			double lineHeight, RevCommit next, Color color, int branchIndex, int parentCount, int childCount) {

		double x = branchIndex * lineHeight/2;
		
		g.setStroke(color);
		g.strokeLine(x + lineHeight / 4, 0, x + lineHeight / 4, lineHeight);
	}

	private void randerCommit(GraphicsContext g, double lineHeight, RevCommit next, Color color, int branchIndex) {
		
		double x = branchIndex * lineHeight/2;

		g.setFill(color);
		g.fillOval(x, lineHeight/4, lineHeight/2, lineHeight/2);

		g.setFill(Color.BLACK);
		g.fillText(next.getShortMessage(), x + lineHeight, lineHeight * 0.75);
	}

	private void registerHead(List<HeadInfo> heads, Map<ObjectId, HeadInfo> branchedCommits, RevWalk walk,
			String refName, Ref ref, String branchName, boolean local, boolean head)
			throws MissingObjectException, IncorrectObjectTypeException, IOException {

		RevCommit commit = walk.parseCommit(ref.getObjectId());
		HeadInfo headInfo = new HeadInfo(head, branchName, local);
		heads.add(headInfo);

		HeadInfo existingHead = branchedCommits.putIfAbsent(commit.getId(), headInfo);
		if (existingHead == null) {
			headInfo.setEmpty(false);
		}

		ArrayList<RevCommit> branch = new ArrayList<>();
		branch.add(commit);
	}

	private void detectParentBranches(RevCommit commit, Map<ObjectId, HeadInfo> branchedCommits, HeadInfo head,
			Map<RevCommit, List<RevCommit>> pendingConnections) {

		RevCommit[] parents = commit.getParents();

		for (RevCommit parent : parents) {
			ObjectId id = parent.getId();
			if (!branchedCommits.containsKey(id)) {
				branchedCommits.put(id, head);
			}
			List<RevCommit> children = pendingConnections.get(parent);
			if (children == null) {
				pendingConnections.put(parent, children = new ArrayList<>());
			}
			children.add(commit);
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.root = new VBox();
		stage.setScene(new Scene(new ScrollPane(this.root)));
		stage.show();
	}
}
