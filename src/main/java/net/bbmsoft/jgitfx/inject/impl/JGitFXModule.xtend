package net.bbmsoft.jgitfx.inject.impl

import com.google.inject.AbstractModule
import com.google.inject.TypeLiteral
import java.io.File
import java.util.List
import java.util.concurrent.ExecutorService
import net.bbmsoft.bbm.utils.Persistor
import net.bbmsoft.bbm.utils.concurrent.SimpleTaskHelper
import net.bbmsoft.bbm.utils.concurrent.TaskHelper
import net.bbmsoft.jgitfx.event.EventBroker
import net.bbmsoft.jgitfx.event.SimpleEventBroker
import net.bbmsoft.jgitfx.messaging.DialogMessenger
import net.bbmsoft.jgitfx.messaging.MessengerListener
import net.bbmsoft.jgitfx.modules.AppDirectoryProvider
import net.bbmsoft.jgitfx.modules.GitWorkerExecutorManager
import net.bbmsoft.jgitfx.modules.Preferences
import net.bbmsoft.jgitfx.registry.RepositoryRegistry
import net.bbmsoft.jgitfx.registry.impl.JsonFilePersistor
import net.bbmsoft.jgitfx.registry.impl.PersistingRepositoryRegistry

class JGitFXModule extends AbstractModule {

	final EventBroker eventBroker
	final MessengerListener messageListener
	final ExecutorService gitWorker
	final Preferences prefs
	final TaskHelper taskHelper

	new() {
		
		this.eventBroker = new SimpleEventBroker
		this.messageListener = new MessengerListener(new DialogMessenger)
		this.gitWorker = new GitWorkerExecutorManager
		this.prefs = Preferences.loadFromFile(AppDirectoryProvider.getFilePathFromAppDirectory('config.json'))
		this.taskHelper = new SimpleTaskHelper(this.gitWorker)
	}

	override protected void configure() {
		bind(EventBroker).toInstance(this.eventBroker)
		bind(MessengerListener).toInstance(this.messageListener)
		bind(Preferences).toInstance(this.prefs)
		bind(RepositoryRegistry).to(PersistingRepositoryRegistry)
		bind(new TypeLiteral<Persistor<List<File>>>(){}).to(JsonFilePersistor)
		bind(TaskHelper).toInstance(this.taskHelper)
	}
}
