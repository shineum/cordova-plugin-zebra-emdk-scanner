package com.shineum.zebra;

import java.util.ArrayList;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.ScanDataCollection.ScanData;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.Scanner.TriggerType;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;
import com.symbol.emdk.barcode.StatusData.ScannerStates;

public class EmdkScanner extends CordovaPlugin implements EMDKListener, StatusListener, DataListener {
    private static CallbackContext callbackContext = null;

    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    private boolean isBeep = true;

    private void callbackMsg(String msg, String errMsg) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (EmdkScanner.callbackContext != null) {
                    if (msg != null) {
                        EmdkScanner.callbackContext.success(msg);
                    } else if (errMsg != null) {
                        EmdkScanner.callbackContext.error(errMsg);
                    }
                }
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("init".equals(action)) {
            EMDKResults results = EMDKManager.getEMDKManager(cordova.getActivity().getApplicationContext(), this);
            return (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS);
        } else if ("set".equals(action)) {
            EmdkScanner.callbackContext = callbackContext;
            return true;
        }
        else if ("reset".equals(action)) {
            EmdkScanner.callbackContext = null;
            return true;
        }
        else if ("scan".equals(action)) {
            softScan();
            return true;
        }
        else if ("cancel".equals(action)) {
            cancelScan();
            return true;
        }
        // else if ("beep".equals(action)) {
        //     isBeep = (args.getInt(0) == 1);
        //     updateScannerConfig();
        //     return true;
        // }
        return false;  // Returning false results in a "MethodNotFound" error.
    }

    //
    @Override
    public void onOpened(EMDKManager emdkManager) {
        // Get a reference to EMDKManager
        this.emdkManager = emdkManager;
        // Get a  reference to the BarcodeManager feature object
        initBarcodeManager();
        // Initialize the scanner
        initScanner();        
    }
    @Override
    public void onClosed() {
        closeScanner();
        releaseEmdkManager();
    }
    @Override
    public void onStatus(StatusData statusData) {
        ScannerStates state =  statusData.getState();    
        switch(state) {
            case ScannerStates.IDLE:
                // Scanner is idle and ready to change configuration and submit read.
                // statusStr = statusData.getFriendlyName()+" is   enabled and idle...";
                // Change scanner configuration. This should be done while the scanner is in IDLE state.
                setConfig();
                try {
                    // Starts an asynchronous Scan. The method will NOT turn ON the scanner beam,
                    //but puts it in a  state in which the scanner can be turned on automatically or by pressing a hardware trigger.
                    scanner.read();
                }
                catch (ScannerException e)   {
                    // updateStatus(e.getMessage());
                }
            break;
        }
    }
    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        // The ScanDataCollection object gives scanning result and the collection of ScanData. Check the data and its status.
        if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanData> scanData =  scanDataCollection.getScanData();
            // Iterate through scanned data and prepare the data.
            String dataStr = "";
            for (ScanData data: scanData) {
                // // Get the scanned data
                // String barcodeData = data.getData();
                // // Get the type of label being scanned
                // ScanDataCollection.LabelType labelType = data.getLabelType();
                // // Concatenate barcode data and label type
                // // dataStr =  barcodeData + "  " +  labelType;
                dataStr = data.getData();
            }
            callbackMsg(dataStr, null);
            // Update EditText with scanned data and type of label on UI thread.
            // updateData(dataStr);
        }

        // if (scanner.triggerType) {
        //     cancelScan();
        // }
        cancelScan();
    }

    private void releaseEmdkManager() {
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

    private void closeScanner() {
        if (scanner != null) {
            try {
                // Release the scanner
                // scanner.disable();
                scanner.release();
            } catch (Exception e)   {
                // updateStatus(e.getMessage());
            }
            scanner = null;
        }
    }

    private void initBarcodeManager() {
        // Get the feature object such as BarcodeManager object for accessing the feature.
        barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        // Add external scanner connection listener.
        if (barcodeManager == null) {
            // Toast.makeText(this, "Barcode scanning is not supported.", Toast.LENGTH_LONG).show();
            cordova.getActivity().finish();
        }
    }

    private void initScanner() {        
        if (scanner == null) {
            // Get default scanner defined on the device
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            if (scanner != null) {
                // Implement the DataListener interface and pass the pointer of this object to get the data callbacks.
                scanner.addDataListener(this);

                // Implement the StatusListener interface and pass the pointer of this object to get the status callbacks.
                scanner.addStatusListener(this);

                // Hard trigger. When this mode is set, the user has to manually
                // press the trigger on the device after issuing the read call.
                // NOTE: For devices without a hard trigger, use TriggerType.SOFT_ALWAYS.
                scanner.triggerType = TriggerType.HARD;

                try {
                    // Enable the scanner
                    // NOTE: After calling enable(), wait for IDLE status before calling other scanner APIs
                    // such as setConfig() or read().
                    scanner.enable();
                } catch (ScannerException e) {
                    // updateStatus(e.getMessage());
                    closeScanner();
                }
            }
        }
    }

    private void setConfig() {
        if (scanner != null) {
            try {
                // Get scanner config
                ScannerConfig config = scanner.getConfig();
                // Enable haptic feedback
                // if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                //     config.scanParams.decodeHapticFeedback = true;
                // }
                scanner.setConfig(config);
            } catch (ScannerException e)   {
            }
        }
    }    

    //
    // private void updateScannerConfig() {
    //     if (scanner != null) {
    //         // try {
    //         // } catch (ScannerException e) {                
    //         // }
    //     }
    // }

    private void cancelScan() {
        if (scanner != null) {
            try {
                if (scanner.isReadPending()) {
                    scanner.cancelRead();
                }
                scanner.triggerType = TriggerType.HARD;
                scanner.read();
            } catch (ScannerException e) {                
            }
        }
    }

    private void softScan() {
        if (scanner != null) {
            try {
                // Reset continuous flag
                // bContinuousMode = false;
                if (scanner.isReadPending()) {
                    // Cancel the pending read.
                    scanner.cancelRead();
                }
                scanner.triggerType = TriggerType.SOFT_ONCE;
                scanner.read();

                // new AsyncUiControlUpdate().execute(true);
            } catch (ScannerException e) {
            }
        }
    }

    @Override
    public void onPause(boolean multitasking) {
		super.onPause(multitasking);
        closeScanner();
        releaseEmdkManager();
    }

    @Override
    public void onResume(boolean multitasking) {
		super.onResume(multitasking);
        if (emdkManager == null) {
            EMDKResults results = EMDKManager.getEMDKManager(cordova.getActivity().getApplicationContext(), this);
        }
    }

    // @Override
    // public void onStart() {
    // }

    @Override
    public void onStop() {
        closeScanner();
        releaseEmdkManager();
    }
}