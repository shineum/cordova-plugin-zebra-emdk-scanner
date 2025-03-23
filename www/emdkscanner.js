var exec = cordova.require("cordova/exec");

/**
 * Constructor.
 *
 * @returns {ZebraEmdkScanner}
 */
function ZebraEmdkScanner() {
}

/**
 * Read code from scanner.
 *
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @param config
 */

/**
 * Init.
 */
ZebraEmdkScanner.prototype.init = function () {
      exec(null, null, 'ZebraEmdkScanner', "init", null);
};
/**
 * Scan.
 */
ZebraEmdkScanner.prototype.scan = function () {
      exec(null, null, 'ZebraEmdkScanner', "scan", null);
};

/**
 * Cancel.
 */
ZebraEmdkScanner.prototype.cancel = function () {
      exec(null, null, 'ZebraEmdkScanner', 'cancel', null);
};

// /**
//  * Beep.
//  */
// ZebraEmdkScanner.prototype.beep = function (mode) {
//       exec(null, null, 'ZebraEmdkScanner', 'beep', [mode ? 1 : 0]);
// };

/**
 * Register Callback.
 */
ZebraEmdkScanner.prototype.set = function (successCallback, errorCallback) {
      exec(
            (ret) => {
                  try {
                        successCallback && successCallback(ret);
                  } catch (ex) {}
                  ZebraEmdkScanner.set(successCallback, errorCallback);
            }, 
            (err) => {
                  try {
                        errorCallback && errorCallback(err);
                  } catch (ex) {}
                  ZebraEmdkScanner.set(successCallback, errorCallback);
            }, 
            'ZebraEmdkScanner', 
            'set', 
            null
      );
};

/**
 * Reset Callback.
 */
ZebraEmdkScanner.prototype.reset = function () {
      exec(null, null, 'ZebraEmdkScanner', 'reset', null);
};

var ZebraEmdkScanner = new ZebraEmdkScanner();
module.exports = ZebraEmdkScanner;
