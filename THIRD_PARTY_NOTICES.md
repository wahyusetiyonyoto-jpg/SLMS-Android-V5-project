# Third-Party Notices

Quick Identify Equipment bundles the following QR components for offline runtime.

## jsQR

Source: https://github.com/cozmo/jsQR  
Pinned commit: `8e6a036beafa7053dd44b1b76ac578d22b1b3311`  
License: Apache License 2.0

The build workflow downloads `dist/jsQR.js` from the pinned immutable commit and verifies the Git blob SHA `99ea9df26907009e5553233ffe03c529c1521739` before packaging it into the APK.

## QRCode for JavaScript / qrcode-terminal vendored QR core

The local QR generator `qie-qrcode-local.js` is built from the QRCode for JavaScript implementation by Kazuhiko Arase as vendored by qrcode-terminal.

Original QRCode implementation notice:
- Copyright (c) 2009 Kazuhiko Arase
- License: MIT

qrcode-terminal:
- Source: https://github.com/gtanner/qrcode-terminal
- License: Apache License 2.0

These libraries are used only to provide QR generation/decoding without runtime internet access.
