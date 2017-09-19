package net.bbmsoft.jgitfx.modules;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javafx.util.Pair;
import net.bbmsoft.bbm.utils.concurrent.ThreadUtils;

public class DialogUsernamePasswordProvider extends InteractiveCredentialsProvider {
	
	private final LoginDialog dialog;
	
	private AtomicReference<UsernamePasswordCredentialsProvider> delegate;

	public DialogUsernamePasswordProvider() {
		this.dialog = new LoginDialog();
		this.delegate = new AtomicReference<>();
	}

	@Override
	public synchronized boolean supports(CredentialItem... items) {
		
		for (CredentialItem i : items) {
			if (i instanceof CredentialItem.Username)
				continue;

			else if (i instanceof CredentialItem.Password)
				continue;

			else
				return false;
		}
		
		return true;
	}

	@Override
	public synchronized boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
		
		UsernamePasswordCredentialsProvider theDelegate = this.delegate.get();
		
		if(theDelegate != null) {
			return theDelegate.get(uri, items);
		} else {
			try {
				
				ThreadUtils.runOnJavaFXThreadAndWait(() -> this.delegate.set(updateDelegate(uri)));
				
				theDelegate = this.delegate.get();
				
				if(theDelegate != null) {
					return theDelegate.get(uri, items);
				}
			} catch (InterruptedException e) {
				// ignore
			}
		}
	
		return false;
	}

	private UsernamePasswordCredentialsProvider updateDelegate(URIish uri) {
		
		this.dialog.setLocation(uri.toString());
		Optional<Pair<String, String>> result = dialog.showAndWait();
		
		if(result.isPresent()) {
			Pair<String, String> newCredentials = result.get();
			return new UsernamePasswordCredentialsProvider(newCredentials.getKey(), newCredentials.getValue());
		}
		
		return null;
	}

	@Override
	public boolean retry() {
		this.dialog.setTitle("Login failed: not authorized");
		return this.delegate.getAndSet(null) != null;
	}
}
