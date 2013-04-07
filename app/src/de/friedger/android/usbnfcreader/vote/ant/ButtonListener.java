package de.friedger.android.usbnfcreader.vote.ant;

import de.friedger.android.usbnfcreader.vote.VoteType;

public interface ButtonListener {

	void onButtonPressed(int serialNumber, VoteType voteType);
}
