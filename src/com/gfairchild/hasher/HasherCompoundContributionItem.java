package com.gfairchild.hasher;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

public class HasherCompoundContributionItem extends CompoundContributionItem {
	private static final Set<String> algorithmNameSet = new LinkedHashSet<>();

	static {
		// all the algorithm names should go here, in the desired order
		algorithmNameSet.add(MessageDigestAlgorithms.MD5);
		algorithmNameSet.add(MessageDigestAlgorithms.SHA_1);
		algorithmNameSet.add(MessageDigestAlgorithms.SHA_256);
		algorithmNameSet.add(MessageDigestAlgorithms.SHA_384);
		algorithmNameSet.add(MessageDigestAlgorithms.SHA_512);
	}

	public HasherCompoundContributionItem() {
	}

	public HasherCompoundContributionItem(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		// note that this can't be cached: per the Eclipse docs, we must recreate this on every call
		IContributionItem[] contributionItems = new IContributionItem[algorithmNameSet.size()];

		// dynamically populate the menu
		int count = 0;
		for (String algorithmName : algorithmNameSet) {
			Map<String, String> parameters = new HashMap<>();
			parameters.put("com.gfairchild.hasher.hash.algorithmname", algorithmName);

			contributionItems[count++] = new CommandContributionItem(new CommandContributionItemParameter(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow(), null, "com.gfairchild.hasher.hash", parameters, null,
					null, null, algorithmName, null, null, CommandContributionItem.STYLE_PUSH, null, true));
		}

		return contributionItems;
	}
}