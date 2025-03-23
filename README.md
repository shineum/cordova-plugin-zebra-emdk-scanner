# cordova-plugin-zebra-emdk-scanner
Cordova Scanner for Zebra EMDK Scanners

note. tested with Zebra TC58 on Android 11

# Install

```javascript

cordova plugin add cordova-plugin-zebra-emdk-scanner

```

# Usages

### 1. Initialize

```javascript

// initialize scanner
document.addEventListener("deviceready", () => {
  cordova.plugins.zebra.emdkscanner.init();
}, false);

```

### 2. Set Callbacks

```javascript
// when scan is done, code will be returned to callback
cordova.plugins.zebra.emdkscanner.set((code)=>{}, (error)=>{});

```

### 3. Reset Callbacks

```javascript
// reset callback
cordova.plugins.zebra.emdkscanner.reset();

```

### 4. Scan

```javascript
// this will trigger the scanner and code will be returned to callback
cordova.plugins.zebra.emdkscanner.scan();

```

### 5. Cancel

```javascript
// cancel the scan trigger
cordova.plugins.zebra.emdkscanner.cancel();

```

# history

### 0.0.1
- Initial publish

# License

MIT
