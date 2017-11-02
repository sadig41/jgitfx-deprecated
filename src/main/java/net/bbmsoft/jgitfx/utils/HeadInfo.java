package net.bbmsoft.jgitfx.utils;

public class HeadInfo {

	private final boolean head;
	private final String branchName;
	private final boolean local;
	
	private boolean empty;

	public HeadInfo(boolean head, String branchName, boolean local) {
		this.head = head;
		this.branchName = branchName;
		this.local = local;
		this.setEmpty(true);
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		if (local) {
			sb.append("Local ");
		} else {
			sb.append("Remote ");
		}
		sb.append("head of ").append(branchName);
		if(head) {
			sb.append(" (HEAD)");
		}

		return sb.toString();
	}

	public boolean isHead() {
		return head;
	}

	public String getBranchName() {
		return branchName;
	}

	public boolean isLocal() {
		return local;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}
}