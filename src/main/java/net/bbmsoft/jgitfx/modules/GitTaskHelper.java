package net.bbmsoft.jgitfx.modules;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.controlsfx.control.TaskProgressView;

import javafx.concurrent.Task;
import net.bbmsoft.bbm.utils.concurrent.SimpleTaskHelper;
import net.bbmsoft.bbm.utils.concurrent.TaskHelper;

public class GitTaskHelper implements TaskHelper {

	private final ExecutorService workerManager;
	private final TaskProgressView<Task<?>> progressView;
	private final SimpleTaskHelper delegate;

	public GitTaskHelper(ExecutorService workerManager, TaskProgressView<Task<?>> progressView) {
		this.workerManager = workerManager;
		this.progressView = progressView;
		this.delegate = new SimpleTaskHelper(this.progressView.getTasks(), this.workerManager);
	}

	@Override
	public <T> void submitTask(Task<T> task, String label, Consumer<T> resultConsumer,
			Consumer<Exception> exceptionhandler) {
		this.delegate.submitTask(task, label, resultConsumer, exceptionhandler);
	}

}
