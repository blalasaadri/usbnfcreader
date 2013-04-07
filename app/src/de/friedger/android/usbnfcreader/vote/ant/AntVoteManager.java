package de.friedger.android.usbnfcreader.vote.ant;

import android.content.Context;
import de.friedger.android.usbnfcreader.vote.Vote;
import de.friedger.android.usbnfcreader.vote.VoteListener;
import de.friedger.android.usbnfcreader.vote.VoteManager;
import de.friedger.android.usbnfcreader.vote.VoteType;

public class AntVoteManager implements VoteManager, ButtonListener {

	private final VoteListener listener;
	private final ClickerService clickerService;

	public AntVoteManager(Context context, VoteListener listener) {
		this.listener = listener;
		clickerService = new ClickerService(context);
		clickerService.setListener(this);
	}

	@Override
	public void onStop() {
		clickerService.onPause();
	}

	@Override
	public void onButtonPressed(VoteType voteType) {
		listener.onVote(new Vote(voteType, "demo-clicker", "some-room", System.currentTimeMillis()));
	}

}
