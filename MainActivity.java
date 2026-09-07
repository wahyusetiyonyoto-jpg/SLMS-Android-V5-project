package com.wahyusetiyonyoto.quickidentifyequipment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.print.PrintManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.util.Base64;
import android.util.Base64InputStream;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

    private static final String APP_ORIGIN = "https://appassets.androidplatform.net";
    private static final String START_URL = APP_ORIGIN + "/assets/index.html";
    private static final int CAMERA_PERMISSION_REQUEST = 2001;
    private static final int FILE_CHOOSER_REQUEST = 2002;

    private WebView webView;
    private PermissionRequest pendingWebPermission;
    private ValueCallback<Uri[]> filePathCallback;
    private Uri cameraPhotoUri;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(11, 45, 92));
        getWindow().setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.addView(webView);
        setContentView(root);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", path -> {
                    try {
                        if ("index.html".equals(path)) {
                            InputStream stream = new GZIPInputStream(openBase64AssetParts("index.html.gz.b64", 2));
                            return new WebResourceResponse("text/html", "UTF-8", stream);
                        }
                        if ("initial-state.js".equals(path)) {
                            InputStream stream = new GZIPInputStream(openBase64AssetParts("initial-state.js.gz.b64", 2));
                            return new WebResourceResponse("application/javascript", "UTF-8", stream);
                        }
                        if ("bundled-qr.js".equals(path)) {
                            InputStream stream = new GZIPInputStream(openBase64AssetParts("bundled-qr.js.gz.b64", 6));
                            return new WebResourceResponse("application/javascript", "UTF-8", stream);
                        }
                        if ("logo.webp".equals(path)) {
                            return new WebResourceResponse("image/webp", null, openBase64AssetParts("logo.webp.b64", 1));
                        }
                        if ("jsQR.js".equals(path)) {
                            return new WebResourceResponse("application/javascript", "UTF-8", getAssets().open("jsQR.js"));
                        }
                        if ("qie-qrcode-local.js".equals(path)) {
                            return new WebResourceResponse("application/javascript", "UTF-8", getAssets().open("qie-qrcode-local.js"));
                        }
                    } catch (IOException ignored) {
                        return null;
                    }
                    return null;
                })
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (uri != null && "appassets.androidplatform.net".equalsIgnoreCase(uri.getHost())) {
                    return false;
                }
                if (uri != null && ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        return true;
                    } catch (Exception ignored) {
                        return false;
                    }
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url != null && url.startsWith(APP_ORIGIN)) {
                    injectAndroidHelpers();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> handleWebPermissionRequest(request));
            }

            @Override
            public void onPermissionRequestCanceled(PermissionRequest request) {
                if (pendingWebPermission == request) pendingWebPermission = null;
            }

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePath,
                    FileChooserParams fileChooserParams
            ) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = filePath;
                return launchImageChooser(fileChooserParams);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.d(
                            "QuickIdentifyEquipment",
                            consoleMessage.message() + " @" + consoleMessage.lineNumber()
                    );
                }
                return true;
            }
        });

        webView.addJavascriptInterface(new AndroidHost(), "AndroidHost");
        webView.loadUrl(resolveLaunchUrl(getIntent()));
    }


    private InputStream openBase64AssetParts(String prefix, int count) throws IOException {
        Vector<InputStream> parts = new Vector<>();
        for (int i = 1; i <= count; i++) {
            parts.add(getAssets().open(String.format(Locale.US, "%s.%03d", prefix, i)));
        }
        SequenceInputStream sequence = new SequenceInputStream(parts.elements());
        return new Base64InputStream(sequence, Base64.DEFAULT);
    }

    private String resolveLaunchUrl(Intent intent) {
        if (intent == null) return START_URL;
        Uri uri = intent.getData();
        if (uri == null) return START_URL;
        if (!"https".equalsIgnoreCase(uri.getScheme())) return START_URL;
        if (!"appassets.androidplatform.net".equalsIgnoreCase(uri.getHost())) return START_URL;
        String fragment = uri.getEncodedFragment();
        return fragment == null || fragment.isEmpty() ? START_URL : START_URL + "#" + fragment;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (webView != null) webView.loadUrl(resolveLaunchUrl(intent));
    }

    private void handleWebPermissionRequest(PermissionRequest request) {
        Uri origin = request.getOrigin();
        if (origin == null || !"appassets.androidplatform.net".equalsIgnoreCase(origin.getHost())) {
            request.deny();
            return;
        }

        boolean wantsCamera = false;
        for (String resource : request.getResources()) {
            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) {
                wantsCamera = true;
                break;
            }
        }

        if (!wantsCamera) {
            request.deny();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
        } else {
            pendingWebPermission = request;
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != CAMERA_PERMISSION_REQUEST) return;

        PermissionRequest request = pendingWebPermission;
        pendingWebPermission = null;
        if (request == null) return;

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
        } else {
            request.deny();
            Toast.makeText(this, "Izin kamera diperlukan untuk QR Scanner Equipment.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean launchImageChooser(WebChromeClient.FileChooserParams params) {
        Intent contentIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentIntent.setType("image/*");

        Intent cameraIntent = null;
        try {
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = createImageFile();
                cameraPhotoUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        photoFile
                );
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                cameraIntent = null;
            }
        } catch (Exception ignored) {
            cameraIntent = null;
        }

        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, contentIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Pilih foto");
        if (cameraIntent != null) {
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        }

        try {
            startActivityForResult(chooser, FILE_CHOOSER_REQUEST);
            return true;
        } catch (Exception e) {
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(null);
                filePathCallback = null;
            }
            Toast.makeText(this, "Pemilih foto tidak tersedia pada perangkat ini.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private File createImageFile() throws IOException {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) dir = getCacheDir();
        return File.createTempFile("EQUIPMENT_" + stamp + "_", ".jpg", dir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST || filePathCallback == null) return;

        Uri[] result = null;
        if (resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                result = new Uri[]{data.getData()};
            } else if (cameraPhotoUri != null) {
                result = new Uri[]{cameraPhotoUri};
            }
        }

        filePathCallback.onReceiveValue(result);
        filePathCallback = null;
        cameraPhotoUri = null;
    }

    private void injectAndroidHelpers() {
        String js = "javascript:(function(){"
                + "if(window.__quickIdentifyEquipmentAndroidHost)return;"
                + "window.__quickIdentifyEquipmentAndroidHost=true;"
                + "document.addEventListener('click',async function(e){"
                + "var a=e.target&&e.target.closest?e.target.closest('a[download]'):null;"
                + "if(!a||!a.href)return;"
                + "var href=a.href,name=a.download||'download';"
                + "if(!(href.indexOf('blob:')===0||href.indexOf('data:')===0))return;"
                + "e.preventDefault();"
                + "try{"
                + "if(href.indexOf('blob:')===0){"
                + "var b=await fetch(href);var blob=await b.blob();"
                + "if((blob.type||'').indexOf('text/')===0||/\\.(csv|txt|json)$/i.test(name)){"
                + "AndroidHost.saveText(await blob.text(),name,blob.type||'text/plain');"
                + "}else{var r=new FileReader();r.onloadend=function(){AndroidHost.saveDataUrl(String(r.result||''),name);};r.readAsDataURL(blob);}"
                + "}else{AndroidHost.saveDataUrl(href,name);}"
                + "}catch(err){console.error('Android download bridge',err);}"
                + "},true);"
                + "window.print=function(){AndroidHost.printPage();};"
                + "})();void(0);";
        webView.evaluateJavascript(js, null);
    }

    private final class AndroidHost {
        @JavascriptInterface
        public void saveText(String text, String filename, String mimeType) {
            byte[] bytes = (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
            saveBytes(bytes, safeFilename(filename), normalizeMime(mimeType, filename));
        }

        @JavascriptInterface
        public void saveDataUrl(String dataUrl, String filename) {
            if (dataUrl == null || !dataUrl.startsWith("data:")) return;
            try {
                int comma = dataUrl.indexOf(',');
                if (comma < 0) return;
                String meta = dataUrl.substring(5, comma);
                String payload = dataUrl.substring(comma + 1);
                boolean base64 = meta.toLowerCase(Locale.US).contains(";base64");
                String mime = meta.split(";", 2)[0];
                byte[] bytes = base64
                        ? android.util.Base64.decode(payload, android.util.Base64.DEFAULT)
                        : URLDecoder.decode(payload, "UTF-8").getBytes(StandardCharsets.UTF_8);
                saveBytes(bytes, safeFilename(filename), normalizeMime(mime, filename));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        MainActivity.this,
                        "File tidak dapat disimpan.",
                        Toast.LENGTH_LONG
                ).show());
            }
        }

        @JavascriptInterface
        public void printPage() {
            runOnUiThread(() -> {
                PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                String jobName = getString(com.wahyusetiyonyoto.quickidentifyequipment.R.string.app_name) + " - Label";
                printManager.print(jobName, webView.createPrintDocumentAdapter(jobName), null);
            });
        }

        @JavascriptInterface
        public void openAppSettings() {
            runOnUiThread(() -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            });
        }
    }

    private String safeFilename(String filename) {
        String name = filename == null ? "download" : filename.trim();
        if (name.isEmpty()) name = "download";
        name = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_");
        if (name.length() > 120) name = name.substring(0, 120);
        return name;
    }

    private String normalizeMime(String mime, String filename) {
        if (mime != null && !mime.trim().isEmpty()) return mime.split(";", 2)[0].trim();
        String extension = MimeTypeMap.getFileExtensionFromUrl(filename == null ? "" : filename);
        String guessed = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return guessed == null ? "application/octet-stream" : guessed;
    }

    private void saveBytes(byte[] bytes, String filename, String mimeType) {
        new Thread(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Quick Identify Equipment");
                    values.put(MediaStore.MediaColumns.IS_PENDING, 1);

                    Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri == null) throw new IOException("Unable to create download");
                    try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                        if (out == null) throw new IOException("Unable to open download");
                        out.write(bytes);
                    }
                    values.clear();
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    getContentResolver().update(uri, values, null, null);
                } else {
                    File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    if (dir == null) dir = getFilesDir();
                    if (!dir.exists() && !dir.mkdirs()) throw new IOException("Unable to create downloads directory");
                    try (OutputStream out = new FileOutputStream(new File(dir, filename))) {
                        out.write(bytes);
                    }
                }
                runOnUiThread(() -> Toast.makeText(
                        MainActivity.this,
                        "Tersimpan di folder Download/Quick Identify Equipment: " + filename,
                        Toast.LENGTH_LONG
                ).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        MainActivity.this,
                        "Gagal menyimpan " + filename,
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (pendingWebPermission != null) {
            pendingWebPermission.deny();
            pendingWebPermission = null;
        }
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
        if (webView != null) {
            webView.removeJavascriptInterface("AndroidHost");
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
