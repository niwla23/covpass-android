package com.ibm.health.common.vaccination.app.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.triggerBackPress
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.databinding.FragmentQrScannerBinding
import com.ibm.health.common.vaccination.app.utils.getScreenSize
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.Size

/**
 * QR Scanner Fragment extending from BaseFragment to display a custom layout form scanner view.
 */
public abstract class QRScannerFragment : BaseFragment(), LifecycleObserver {

    private val binding by viewBinding(FragmentQrScannerBinding::inflate)

    public abstract val callback: BarcodeCallback

    public lateinit var barcodeView: DecoratedBarcodeView

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startScanning()
            } else {
                finishScanning()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barcodeView = binding.zxingBarcodeScanner
        val screenSize = requireContext().getScreenSize()
        binding.zxingBarcodeScanner.barcodeView.framingRectSize = Size(screenSize.x, screenSize.y)
        binding.scannerCloseButton.setOnClickListener { requireActivity().onBackPressed() }
        checkPermission(Manifest.permission.CAMERA) { startScanning() }
    }

    private fun checkPermission(targetPermission: String, targetAction: () -> Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                targetPermission
            ),
            -> {
                targetAction.invoke()
            }
            else -> {
                requestPermissionLauncher.launch(targetPermission)
            }
        }
    }

    private fun startScanning() {
        binding.zxingBarcodeScanner.barcodeView.decodeContinuous(callback)
    }

    override fun onResume() {
        super.onResume()
        binding.zxingBarcodeScanner.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.zxingBarcodeScanner.pause()
    }

    public fun finishScanning() {
        triggerBackPress()
    }
}
