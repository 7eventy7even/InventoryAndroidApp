package vkx64.android.scanventory.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QRScanner {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private final Activity activity;
    private final DecoratedBarcodeView barcodeView;
    private final QRScannerCallback callback;

    public QRScanner(Activity activity, DecoratedBarcodeView barcodeView, QRScannerCallback callback) {
        this.activity = activity;
        this.barcodeView = barcodeView;
        this.callback = callback;
    }

    /**
     * Initialize the QR Scanner, request camera permission if not granted.
     */
    public void initialize() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startScanner();
        }
    }

    /**
     * Handle the result of the permission request.
     */
    public void handlePermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                Toast.makeText(activity, "Camera permission is required to scan QR codes.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Start the QR scanner.
     */
    private void startScanner() {
        barcodeView.resume();

        barcodeView.decodeContinuous(result -> {
            if (result != null && result.getText() != null && !result.getText().trim().isEmpty()) {
                String scannedValue = result.getText().trim();
                Log.d("QRScanner", "Scanned value: " + scannedValue);

                // Pause scanning and notify the callback
                barcodeView.pause();
                new Handler().postDelayed(barcodeView::resume, 1000);
                callback.onScanned(scannedValue);
            }
        });
    }

    /**
     * Pause the QR scanner.
     */
    public void pauseScanner() {
        barcodeView.pause();
    }

    /**
     * Resume the QR scanner.
     */
    public void resumeScanner() {
        barcodeView.resume();
    }

    /**
     * Callback interface to notify the activity of the scanned value.
     */
    public interface QRScannerCallback {
        void onScanned(String scannedValue);
    }
}

