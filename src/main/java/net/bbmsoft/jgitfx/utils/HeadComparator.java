package net.bbmsoft.jgitfx.utils;

import java.util.Comparator;

public class HeadComparator implements Comparator<HeadInfo> {

	@Override
	public int compare(HeadInfo o1, HeadInfo o2) {
		
		if (o1.isLocal() && !o2.isLocal()) {
			return -1;
		}
		if (!o1.isLocal() && o2.isLocal()) {
			return 1;
		}
		
		if (o1.isHead() && !o2.isHead()) {
			return -1;
		}
		if (!o1.isHead() && o2.isHead()) {
			return 1;
		}

		if ("master".equals(o1.getBranchName())) {
			return -1;
		}
		if ("master".equals(o2.getBranchName())) {
			return 1;
		}

		if ("develop".equals(o1.getBranchName())) {
			return -1;
		}
		if ("develop".equals(o2.getBranchName())) {
			return 1;
		}

		if (o1.getBranchName().startsWith("feature/")) {
			return -1;
		}
		if (o2.getBranchName().startsWith("feature/")) {
			return 1;
		}

		if (o1.getBranchName().startsWith("hotfix/")) {
			return -1;
		}
		if (o2.getBranchName().startsWith("hotfix/")) {
			return 1;
		}

		if (o1.getBranchName().startsWith("release/")) {
			return -1;
		}
		if (o2.getBranchName().startsWith("release/")) {
			return 1;
		}

		return o1.getBranchName().compareToIgnoreCase(o2.getBranchName());
	}

}