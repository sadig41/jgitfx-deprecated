package net.bbmsoft.jgitfx.utils;

import java.util.function.Function;

public class ErrorHelper {
	
	public static <T> T getRoot(T child, Function<T, T> parentProvider) {
		
		T parent = parentProvider.apply((T)child);
		if(parent == null) {
			return child;
		} else {
			return getRoot(parent, parentProvider);
		}
	}
}
