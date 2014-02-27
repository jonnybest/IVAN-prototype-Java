package edu.kit.ipd.alicenlp.ivan.instrumentation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidTagNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitManager {
	private static final String COMMITED_DANGLING_CHANGES = "commited dangling changes";
	private static final String DOCUMENT_TXT = "document.txt";
	/**
	 * The path to the folder which should contain the tracking
	 */
	final public static String TRACKINGPATH = "tracking/";
	private static Git myGit;

	/**
	 * Commit to the current repository
	 * 
	 * @param branch
	 * @return
	 */
	public static boolean commit(String branch) {
		if (branch.isEmpty()) {
			branch = "master";
		}

		try {
			Git git = getGit();

			checkout(branch, git);

			AddCommand add = git.add();
			add.addFilepattern(DOCUMENT_TXT);
			add.call();
			CommitCommand ci = git.commit();
			ci.setMessage("test commit").call();

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnmergedPathsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConcurrentRefUpdateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongRepositoryStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * @param branch
	 * @param git
	 * @throws GitAPIException
	 * @throws RefNotFoundException
	 * @throws InvalidRefNameException
	 * @throws CheckoutConflictException
	 */
	public static void checkout(String branch, Git git) {
		try {
			if (git == null)
				git = getGit();
			
			// try and create a branch for this file
			CheckoutCommand co = git.checkout();
			co.setName(branch);
			co.setCreateBranch(true);
			co.call();
		} catch (RefAlreadyExistsException e) {
			// that's alright
			CheckoutCommand co = git.checkout();
			co.setName(branch);
			co.setCreateBranch(false);
			try {
				co.call();
			} catch (GitAPIException e1) {
				System.err.println(branch);
				e1.printStackTrace();
			}
		} catch (GitAPIException | IOException e) {
			System.err.println(branch);
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws IOException
	 */
	protected static Git getGit() throws IOException {
		if (myGit != null)
			return myGit;

		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder
				.setGitDir(new File(TRACKINGPATH + ".git")).readEnvironment() // scan
																				// environment
																				// GIT_*
																				// variables
				// .findGitDir() // scan up the file system tree
				.build();
		// ObjectId head = repository.resolve("HEAD");
		Git git = new Git(repository);
		return git;
	}

	/**
	 * Tag the current repository at the HEAD
	 * 
	 * @param tagname
	 */
	public static void tag(final String tagname) {
		Git git = null;
		try {
			git = getGit();
			TagCommand tg = git.tag();
			tg.setName(tagname);
			tg.call();
		} catch (RefAlreadyExistsException e) {
			// this tag is alreay being used. let's increment the counter
			// 1. get tag list
			ListTagCommand tl = git.tagList();
			try {
				int maxcount = 0;
				List<Ref> tags = tl.call();
				for (Ref t : tags) {
					// 2. find latest branch with this branch name as a prefix
					String expandedtagname = "refs/tags/" + tagname;
					if (t.getName().startsWith(expandedtagname)) {
						// 3. apply regex to extract number at the and of the
						// tag
						String suffix = t.getName().substring(
								expandedtagname.length()); // this should yield
															// a number
						// 4. cast to int and find max
						int counter;
						try {
							counter = Integer.parseInt(suffix);
						} catch (NumberFormatException e1) {
							counter = 0;
						}
						if (maxcount < counter) {
							maxcount = counter;
						}
					}
				}
				// 5. increment max number
				maxcount++;
				// 6. call tag() recursively
				tag(tagname + maxcount);
			} catch (GitAPIException e1) {
				// I have no idea what could possibly go wrong, but hey
				e1.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConcurrentRefUpdateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTagNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Attempt to initialize a repository if there is none
	 * 
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public static void safeInit() throws GitAPIException, IOException {
		if (myGit != null)
			return;

		try {
			myGit.status().call();
		} catch (NullPointerException e) {
			// there is no git!
			myGit = Git.init().setDirectory(new File(TRACKINGPATH))
					.setBare(false).call();
		}
		if (!myGit.status().call().isClean()) {
			myGit.add().addFilepattern(DOCUMENT_TXT).call();
			myGit.commit().setAll(true)
			.setMessage(COMMITED_DANGLING_CHANGES).call();
		}
		checkout("master", myGit);

		myGit.commit().setAll(true).setMessage("new session").call();
	}
}
