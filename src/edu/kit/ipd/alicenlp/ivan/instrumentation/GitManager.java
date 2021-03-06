package edu.kit.ipd.alicenlp.ivan.instrumentation;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidTagNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.StashApplyFailureException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitManager {
	private static final String COMMITED_DANGLING_CHANGES = "commited dangling changes";
	private static final String DOCUMENT_TXT = "document.txt";
	private static final String PANEL_TXT = "panel.txt";
	/**
	 * The path to the folder which should contain the tracking
	 */
	final public static String TRACKINGPATH = "tracking/";
	private static Git myGit;
	private static Logger log = Logger.getLogger("edu.kit.ipd.alicenlp.ivan.instrumentation.GitManager");;

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

			log.info("Commiting to " + branch);
			if(!isOnBranch(branch))
				checkout(branch, git);

			AddCommand add = git.add();
			add.addFilepattern(DOCUMENT_TXT);
			add.addFilepattern(PANEL_TXT);
			add.call();
			CommitCommand ci = git.commit();
			ci.setMessage("user interaction").call();

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

	private static boolean isOnBranch(String branch) throws IOException {
		String current = getGit().getRepository().getFullBranch();
		Ref ref = getGit().getRepository().getRef(branch);
		if(ref == null)
			return false;
		String target = ref.getTarget().getName();
		return current.equals(target);
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
		boolean restoreStash = false;
		// stash changes if not clean
		try {
 			if(!getGit().status().call().isClean()){
				RevCommit result = getGit().stashCreate().setIndexMessage("branching").call();
				restoreStash = true;
			}
		} catch (NoWorkTreeException | GitAPIException | IOException e2) {
			log.warning("failed to stash save before checkout");
			e2.printStackTrace();
		}
		
		// do work
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
		
		// restore stash if neccessary
		if(restoreStash){
			try {
				if(getGit().stashList().call().size() > 0)
					getGit().stashApply().call();
			} catch (IOException | WrongRepositoryStateException | NoHeadException e) {
				log.warning("failed to apply stash after checkout");
				e.printStackTrace();
			} catch (StashApplyFailureException e) {
				try {
					log.warning("Failed to apply stash after checkout (from " + branch +" to "+getGit().getRepository().getBranch()+") because of a merge conflict. Changes remain in stash.");
				} catch (IOException e1) {
					log.warning("Failed to apply stash after checkout (from " + branch +") because of a merge conflict. Changes remain in stash.");
				}
			} catch (GitAPIException e) {
				log.warning("failed to apply stash after checkout");
				e.printStackTrace();
			}
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
		else 
			myGit = getGit();
		
		if(myGit.branchList().call().size() == 0) {
			// there is no git!
			myGit = Git.init().setDirectory(new File(TRACKINGPATH))
					.setBare(false).call();
			RevCommit thing = myGit.commit().setMessage("initialized new repository").call();
			myGit.tag().setObjectId(thing).setName("IvanBaseline").setMessage("This tag marks the ").call();
		}
		
		if (!myGit.status().call().isClean()) {
			myGit.add().addFilepattern(DOCUMENT_TXT).call();
			myGit.add().addFilepattern(PANEL_TXT).call();
			myGit.commit().setAll(true)
			.setMessage(COMMITED_DANGLING_CHANGES).call();
		}
		checkout("master", myGit);
		myGit.reset().setRef("IvanBaseline").setMode(ResetType.HARD).call();

		myGit.commit().setAll(true).setMessage("new session").call();
	}

}