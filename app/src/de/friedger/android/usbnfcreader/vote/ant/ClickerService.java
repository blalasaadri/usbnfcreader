package de.friedger.android.usbnfcreader.vote.ant;

import android.content.Context;
import android.util.Log;
import com.dsi.ant.plugins.AntPluginMsgDefines;
import com.dsi.ant.plugins.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusControlsPcc;
import de.friedger.android.usbnfcreader.vote.VoteType;

public class ClickerService {

	private static final String TAG = "ClickerService";
	private static final int DEVICE_AUTO = 0;

	private final Context context;
	private final AntPluginPcc.IPluginAccessResultReceiver<AntPlusControlsPcc> resultReceiver;
	private final AntPluginPcc.IDeviceStateChangeReceiver stateReceiver;
	private final AntPlusControlsPcc.IGenericCommandReceiver commandReceiver;
	private ButtonListener listener;
	private AntPlusControlsPcc controller;

	public ClickerService(Context context) {
		this.context = context;
		this.resultReceiver = new AntResultReceiver();
		this.stateReceiver = new AntStateReceiver();
		this.commandReceiver = new AntCommandReceiver();
	}

	public void setListener(ButtonListener listener) {
		this.listener = listener;
	}

	public void onResume() {
		AntPlusControlsPcc.requestAccessGenericMode(context, resultReceiver, stateReceiver, commandReceiver, DEVICE_AUTO);
	}

	public void onPause() {
		if (controller != null) {
			controller.releaseAccess();
			controller = null;
		}
	}

	private void fireListener(int serialNumber, VoteType voteType) {
		if (listener != null) {
			listener.onButtonPressed(serialNumber, voteType);
		}
	}

	private final class AntResultReceiver implements AntPluginPcc.IPluginAccessResultReceiver<AntPlusControlsPcc> {

		@Override
		public void onResultReceived(AntPlusControlsPcc result, int resultCode, int initialDeviceStateCode) {
			if (resultCode == AntPluginMsgDefines.MSG_REQACC_RESULT_whatSUCCESS) {
				controller = result;
			}
		}

	}

	private final class AntStateReceiver implements AntPluginPcc.IDeviceStateChangeReceiver {

		@Override
		public void onDeviceStateChange(int newDeviceState) {
			// TODO do something important here
		}

	}

	private final class AntCommandReceiver implements AntPlusControlsPcc.IGenericCommandReceiver {

		@Override
		public int onNewGenericCommand(final int currentMessageCount, final int serialNumber,
									   final int manufacturerID, final int sequenceNumber, final int commandNumber) {
			switch(commandNumber)
			{
				case AntPlusControlsPcc.GenericCommandNumber.MENU_UP:
					fireListener(serialNumber, VoteType.POSITIVE);
					break;
				case AntPlusControlsPcc.GenericCommandNumber.MENU_DOWN:
					fireListener(serialNumber, VoteType.NEGATIVE);
					break;
				case AntPlusControlsPcc.GenericCommandNumber.MENU_SELECT:
				case AntPlusControlsPcc.GenericCommandNumber.MENU_BACK:
				case AntPlusControlsPcc.GenericCommandNumber.HOME:
				case AntPlusControlsPcc.GenericCommandNumber.START:
				case AntPlusControlsPcc.GenericCommandNumber.STOP:
				case AntPlusControlsPcc.GenericCommandNumber.RESET:
				case AntPlusControlsPcc.GenericCommandNumber.LENGTH:
				case AntPlusControlsPcc.GenericCommandNumber.LAP:
				case AntPlusControlsPcc.GenericCommandNumber.NO_COMMAND:
				default:
					return AntPlusControlsPcc.CommandStatus.REJECTED;
			}
			return AntPlusControlsPcc.CommandStatus.PASS;
		}

	}

}
