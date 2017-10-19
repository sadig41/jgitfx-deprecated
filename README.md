# ![logo](src/main/resources/logo/jgitfx-full-512.png)

## What is it?

JGitFX is a graphical user interface for JGit, a java implementation of the git source control management software.

## Why is it?

While git is a great command line tool on linux and unix based systems, support on Windows is not that great. Even in the command line the Windows version is slow and most GUIs are either slow, unintuitive or too complicated. So my goal was to create a cross-platform UI that also gives a solid performance and a good user experience on Windows.

Primary design goals are:

 1. speed - git was designed to be fast. Unfortunately most UIs aren't (especially on Windows)
 2. the ability to easily handle LOTS of repositories - git is designed to have one repository per project/module/bundle. In larger projects that can be hundreds. Core git uses submodules to deal with this but from my experience that approach is difficult for many users, so I would prefer a GUI that is designed to give good control over a large number of repositories at the same time
 3. good looks - in my opinion it is easier and more fun to work with software that looks good. Nobody wants to deal with ugly UIs

## In which state is it?

Right now JGitFX is in early alpha stage. Many features that are important for daily work are not available yet, let alone everything that is required to fulfill the aforementioned design goals. So if you want to try it, feel free, but don't expect to be using it as your main SCM tool at work any time soon.

## How to build?

Run ```mvn package``` to create an executable jar file named jgitfx-&lt;version&gt;-jar-with-dependencies.jar in the target folder or ```mvn compile exec:java``` run run the application directly.

## Logo
The JGitFX Logo is based on the original git logo by Jason Long.
For more info please refer to https://git-scm.com/downloads/logos
