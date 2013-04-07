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

	public void onResume() {
		clickerService.onResume();
	}

	@Override
	public void onStop() {
		clickerService.onPause();
	}

	@Override
	public void onButtonPressed(int serialNumber, VoteType voteType) {
		listener.onVote(new Vote(voteType, "ant-remote-" + serialNumber, "some-room", System.currentTimeMillis()));
	}

}
