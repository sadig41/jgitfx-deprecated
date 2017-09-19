package net.bbmsoft.jgitfx.modules;

import org.eclipse.jgit.transport.CredentialsProvider;

public abstract class InteractiveCredentialsProvider extends CredentialsProvider {

	@Override
	public boolean isInteractive() {
		return true;
	}

	public abstract boolean retry();

}
