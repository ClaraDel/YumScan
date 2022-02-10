package fr.epf.min.yumscan.ui.scanner

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.Result
import fr.epf.min.yumscan.ui.DetailsProductActivity
import me.dm7.barcodescanner.zxing.ZXingScannerView

class SimpleScannerFragment : Fragment(), ZXingScannerView.ResultHandler {
    private var mScannerView: ZXingScannerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mScannerView = ZXingScannerView(activity)
        return mScannerView!!
    }

    override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera()
    }

    override fun handleResult(rawResult: Result) {
        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        val handler = Handler()
        handler.postDelayed(
            { mScannerView!!.resumeCameraPreview(this@SimpleScannerFragment) },
            2000
        )

        Log.d("DEBUG", "INTENT")
        val intent = Intent(context, DetailsProductActivity::class.java)
        intent.putExtra("code", rawResult.text)
        startActivity(intent)

    }

    override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()
    }
}