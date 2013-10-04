package com.gfairchild.hasher;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

public class HasherHandler extends AbstractHandler {
	private static final MessageConsoleStream messageConsoleStream;

	static {
		// initialize output console
		MessageConsole console = new MessageConsole(Hasher.PLUGIN_NAME, null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
		messageConsoleStream = console.newMessageStream();
		messageConsoleStream.setActivateOnWrite(true);
	}

	/**
	 * Ordering of {@link IResource}s is as follows: first, all files are listed alphabetically, and then all folders
	 * are listed alphabetically. We don't need to worry about non-files and folders because of the
	 * <code>visibleWhen</code> property of the menu.
	 */
	private static final Comparator<IResource> resourceComparator = new Comparator<IResource>() {
		@Override
		public int compare(IResource o1, IResource o2) {
			if (o1.getClass().equals(o2.getClass()))
				return o1.getName().compareTo(o2.getName());
			else if (o1 instanceof IFile) // o1 is a file, so o2 is a folder
				return -1;
			return 1;
		}
	};

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// do the actual work in a separate thread
		Worker worker = new Worker(event);
		Thread thread = new Thread(worker);
		thread.start();

		return null;
	}

	/**
	 * Figure out which files to hash, generate the hashes, and output the results. This class implements
	 * {@link Runnable} so that it can be run in a separate thread (hash computation could be expensive).
	 */
	private class Worker implements Runnable {
		private ExecutionEvent event;

		public Worker(ExecutionEvent event) {
			this.event = event;
		}

		@Override
		public void run() {
			// dynamically create message digest based on parameter provided by command
			String algorithmName = event.getParameter("com.gfairchild.hasher.hash.algorithmname");
			MessageDigest messageDigest = DigestUtils.getDigest(algorithmName);

			ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection instanceof IStructuredSelection) {
				List<?> selectionList = ((IStructuredSelection) selection).toList();
				Set<IFile> fileSet = new LinkedHashSet<>(); // maintain insertion order

				try {
					// iterate through each selected item and pull out files
					for (Object object : selectionList)
						if (object instanceof IFile)
							fileSet.add((IFile) object);
						else if (object instanceof IFolder)
							fileSet.addAll(getSubFiles((IFolder) object));

					// generate and output hashes
					messageConsoleStream.println(messageDigest.getAlgorithm());
					for (IFile file : fileSet)
						messageConsoleStream.println(file.getProjectRelativePath() + " : "
								+ getHash(messageDigest, file));
					messageConsoleStream.println("----------------------------------------");
				} catch (IOException e) {
					messageConsoleStream.println(e.toString());
				} catch (CoreException e) {
					messageConsoleStream.println(e.toString());
				}
			}
		}
	}

	/**
	 * Recursively go through a folder to get a listing of all the files inside. The returned {@link Set} will be a
	 * {@link LinkedHashSet} that iterates according to {@link #resourceComparator}.
	 * 
	 * @param folder
	 * @return
	 * @throws CoreException
	 */
	private static Set<IFile> getSubFiles(IFolder folder) throws CoreException {
		Set<IFile> fileSet = new LinkedHashSet<>();

		// throw all members into a TreeSet so that they'll be recalled in alphabetical order (folder.members() makes no
		// guarantees about member order)
		SortedSet<IResource> sortedMemberSet = new TreeSet<>(resourceComparator);
		sortedMemberSet.addAll((List<IResource>) Arrays.asList(folder.members()));

		// now either add each member to fileSet or recursively go deeper
		for (IResource resource : sortedMemberSet)
			if (resource instanceof IFile)
				fileSet.add((IFile) resource);
			else if (resource instanceof IFolder)
				fileSet.addAll(getSubFiles((IFolder) resource));

		return fileSet;
	}

	/**
	 * Generate hash of specified file using the specified algorithm. Uses the <a
	 * href="https://commons.apache.org/proper/commons-codec/">Apache Commons Codec</a> library.
	 * 
	 * @param file
	 * @param messageDigest
	 * @return
	 * @throws IOException
	 * @throws CoreException
	 */
	private static String getHash(MessageDigest messageDigest, IFile file) throws IOException, CoreException {
		return Hex.encodeHexString(DigestUtils.updateDigest(messageDigest, file.getContents()).digest());
	}
}