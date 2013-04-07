package de.friedger.android.usbnfcreader;

import java.util.Date;

import com.apiomat.frontend.ApiomatRequestException;
import com.apiomat.frontend.Datastore;
import com.apiomat.frontend.basics.MemberModel;
import com.apiomat.frontend.callbacks.AOMEmptyCallback;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import de.friedger.android.usbnfcreader.io.AttachedDeviceHandler;
import de.friedger.android.usbnfcreader.io.UsbConnectionException;
import de.friedger.android.usbnfcreader.vote.NfcVoteManager;
import de.friedger.android.usbnfcreader.vote.Vote;
import de.friedger.android.usbnfcreader.vote.VoteDbHelper;
import de.friedger.android.usbnfcreader.vote.VoteListener;
import de.friedger.android.usbnfcreader.vote.VoteType;
import de.friedger.android.usbnfcreader.vote.ant.AntVoteManager;

public class MainActivity extends Activity implements VoteListener {

	private NfcVoteManager nfcVoteManager;
    private AntVoteManager antVoteManager;
	private TextView mTextView;
	private Beeper beeper;
	private AttachedDeviceHandler attachedDeviceHandler;
	private VoteDbHelper mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new VoteDbHelper(this);

		initUi();
		initVoteManagers();

		Intent intent = getIntent();
		attachedDeviceHandler = new AttachedDeviceHandler(this, nfcVoteManager);
		initMember();
		try {
			attachedDeviceHandler.handleIntent(intent);
			mTextView.setText("OK");
		} catch (UsbConnectionException e) {
			mTextView.setText(e.getMessage());
		}
	}

	private void initUi() {
		setContentView(R.layout.activity_main);
		mTextView = (TextView) findViewById(R.id.textview);
		beeper = new Beeper(this);
	}

	private void initVoteManagers() {
		nfcVoteManager = new NfcVoteManager("undefined", this);
        antVoteManager = new AntVoteManager(this, this);
	}

	@Override
	public void onVote(final Vote vote) {
		
		final int[] counts = storeVote(vote);
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTextView.setText("Vote: " + vote.getVoteType() + " Id: "
						+ vote.getId() + " Room: " + vote.getRoomId() + " Positive " + counts[0] + " Negative " + counts[1]);
				if (vote.getVoteType() == VoteType.POSITIVE) {
					beepTwice();
					findViewById(android.R.id.content).setBackgroundColor(
							Color.GREEN);
				} else {
					findViewById(android.R.id.content).setBackgroundColor(
							Color.RED);
				}
			}
		});
	}

	public int[] storeVote(Vote vote) {
		storeVoteInBackend(vote);
		
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Vote.COLUMN_NAME_ROOM_ID, vote.getRoomId());
		values.put(Vote.COLUMN_NAME_VOTE_TYPE, vote.getVoteType().ordinal());
		values.put(Vote.COLUMN_NAME_ID, vote.getId());
		values.put(Vote.COLUMN_NAME_TIMESTAMP, vote.getTimestamp());

		db.insertWithOnConflict(VoteDbHelper.TABLE_NAME, "null", values,
				SQLiteDatabase.CONFLICT_REPLACE);

		Cursor cursor = db.query(VoteDbHelper.TABLE_NAME, new String[] {
				Vote.COLUMN_NAME_VOTE_TYPE, "count()" }, null, null,
				Vote.COLUMN_NAME_VOTE_TYPE, null, null);
		int[] counts = new int[2];

		if (cursor != null) {

			if (cursor.moveToFirst()) {
				int voteType1 = cursor.getInt(0);
				int count1 = cursor.getInt(1);
				counts[voteType1 - 1] = count1;

				if (cursor.moveToNext()) {
					int voteType2 = cursor.getInt(0);
					int count2 = cursor.getInt(1);
					counts[voteType2 - 1] = count2;
				}
			}
		}
		db.close();

		return counts;
	}

	private void storeVoteInBackend(Vote v) {
		
		com.apiomat.frontend.voteplusmain.Vote backendVote =new com.apiomat.frontend.voteplusmain.Vote();
	backendVote.setMyVote(new Long(v.getVoteType().ordinal()));
	backendVote.setRoomNumber(v.getRoomId());
	backendVote.setVoterId(v.getId());
	backendVote.setVoteTime(new Date(v.getTimestamp()));
	
		backendVote.saveAsync(new AOMEmptyCallback() {
			
			@Override
			public void isDone(ApiomatRequestException exception) {
		//		Log.DEB// TODO Auto-generated method stub
				
			}
		});
	
	}

	private void initMember() {
		final MemberModel member = new MemberModel();
		member.setUserName("superVoter");
		member.setPassword("secret");
		Datastore.configure( member );
		Log.d("backend", "starting");
			member.loadMeAsync(new AOMEmptyCallback() {
				
				@Override
				public void isDone(ApiomatRequestException exception) {
					Log.d("backend", "loadMe done + exception " + exception);
					
					if(exception!=null)
						{
						member.saveAsync(null);
						}
					
				}
			});

	}

	public void onButtonClick(View target) {
		finish();
	}
	
	public void onButtonPlusClick(View target) {
		onVote(new Vote(VoteType.POSITIVE,"123","myroom",System.currentTimeMillis()));
	}
	
	public void onButtonMinusClick(View target) {
		onVote(new Vote(VoteType.NEGATIVE,"123","myroom",System.currentTimeMillis()));
	}

	@Override
	protected void onStop() {
		super.onStop();
		attachedDeviceHandler.onStop();
		nfcVoteManager.onStop();
        antVoteManager.onStop();
	}

	public void beepTwice() {
		Thread t = new Thread(beeper);
		t.start();
	}
	
}
