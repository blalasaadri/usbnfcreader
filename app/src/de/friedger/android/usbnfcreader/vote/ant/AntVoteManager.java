package de.friedger.android.usbnfcreader.vote.ant;

import de.friedger.android.usbnfcreader.vote.VoteListener;
import de.friedger.android.usbnfcreader.vote.VoteManager;

public class AntVoteManager implements VoteManager {

	private final VoteListener listener;

	public AntVoteManager(VoteListener listener) {
		this.listener = listener;
	}

	@Override
	public void onStop() {
	}

}
