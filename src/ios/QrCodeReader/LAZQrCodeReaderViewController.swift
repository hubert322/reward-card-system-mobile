//
//  QrCodeReaderViewController.swift
//  learningazquizroom
//
//  Created by Hubert Hung on 7/22/19.
//  Copyright © 2019 Learning A-Z. All rights reserved.
//

import UIKit
import AVFoundation

protocol LAZQrCodeReaderViewControllerDelegate: class {
    func qrCodeDidScan(rewardCardCode: String)
}

class LAZQrCodeReaderViewController: UIViewController {
    
    @IBOutlet weak var flipCameraButton: UIButton!
    
    var captureSession = AVCaptureSession()
    var videoPreviewLayer: AVCaptureVideoPreviewLayer?
    var qrCodeFrameView: UIView?
    var frontCamera:AVCaptureDevice?
    var backCamera: AVCaptureDevice?
    var currentCamera: AVCaptureDevice?
    
    weak var delegate: LAZQrCodeReaderViewControllerDelegate?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "QR Code Reader"
        
        askCameraPermission()
        
        setCamera()
        
        guard let captureDevice = backCamera != nil ? backCamera : frontCamera else {
            print("Failed to retrieve any camera")
            return
        }
        
        if !setInput(captureDevice: captureDevice) {
            print("Failed to set input")
            return
        }
        
        if !twoCamerasAvailable() {
            flipCameraButton.isEnabled = false
        }
    
        setOutput()
        setVideoPreview()
        setQrCodeFrameView()
        
        view.bringSubviewToFront(flipCameraButton)
        
        captureSession.startRunning()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        videoPreviewLayer?.frame = view.layer.bounds
        
        if let connection = videoPreviewLayer?.connection, connection.isVideoOrientationSupported {
            let orientation = UIApplication.shared.statusBarOrientation
            switch (orientation) {
            case .portrait:
                setVideoOrientation(layer: connection, orientation: .portrait)
            case .portraitUpsideDown:
                setVideoOrientation(layer: connection, orientation: .portraitUpsideDown)
            case .landscapeRight:
                setVideoOrientation(layer: connection, orientation: .landscapeRight)
            case .landscapeLeft:
                setVideoOrientation(layer: connection, orientation: .landscapeLeft)
            default:
                setVideoOrientation(layer: connection, orientation: .landscapeRight)
            }
        }
    }
    

    
    @IBAction func flipCamera(_ sender: UIButton) {
        removeInput(captureDevice: currentCamera!)
        setInput(captureDevice: (currentCamera === frontCamera ? backCamera : frontCamera)!)
    }
    
    private func setCamera() {
        let cameras = AVCaptureDevice.devices(for: AVMediaType.video)
        for camera in cameras {
            if camera.position == .front {
                if #available(iOS 10.0, *), camera.deviceType != .builtInWideAngleCamera {
                    break
                }
                frontCamera = camera
            }
            else if camera.position == .back {
                if #available(iOS 10.0, *), camera.deviceType != .builtInWideAngleCamera {
                    break
                }
                backCamera = camera
            }
        }
    }
    
    private func askCameraPermission() {
        if AVCaptureDevice.authorizationStatus(for: .video) != .authorized {
            AVCaptureDevice.requestAccess(for: .video, completionHandler: { (granted: Bool) in
                if !granted {
                    self.showCameraDeniedDialog()
                }
            })
        }
    }
    
    private func showCameraDeniedDialog() {
        DispatchQueue.main.async {
            let alertTitle = "Error: Camera Permission"
            let alertText = "Camera has been disabled. Please enable it in your Settings."
            let cancelAction: UIAlertAction
            let cancelActionText: String
            let goAction: UIAlertAction?
            
            if UIApplication.shared.canOpenURL(URL(string: UIApplication.openSettingsURLString)!) {
                let goActionText = "Settings"
                goAction = UIAlertAction(title: goActionText, style: .default, handler: {(alert: UIAlertAction!) in
                    if #available(iOS 10.0, *) {
                        UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!, options: [:], completionHandler: nil)
                    } else {
                        UIApplication.shared.openURL(URL(string: UIApplication.openSettingsURLString)!)
                    }
                })
                
                cancelActionText = "Cancel"
            }
            else {
                cancelActionText = "OK"
                goAction = nil
            }
            
            cancelAction = UIAlertAction(title: cancelActionText, style: .default, handler: {(alert: UIAlertAction!) in
                self.navigationController?.popViewController(animated: true)
            })
            
            let alert = UIAlertController(title: alertTitle, message: alertText, preferredStyle: .alert)
            alert.addAction(cancelAction)
            if let goAction = goAction {
                alert.addAction(goAction)
            }
            self.present(alert, animated: true, completion: nil)
        }
    }
    
    private func twoCamerasAvailable() -> Bool {
        do {
            _ = try AVCaptureDeviceInput(device: frontCamera!)
            _ = try AVCaptureDeviceInput(device: backCamera!)
        }
        catch {
            return false
        }
        return true
    }
    
    private func setInput(captureDevice: AVCaptureDevice) -> Bool {
        do {
            let input = try AVCaptureDeviceInput(device: captureDevice)
            captureSession.addInput(input)
            currentCamera = captureDevice
        }
        catch {
            print(error)
            return false
        }
        return true
    }
    
    private func removeInput(captureDevice: AVCaptureDevice) {
        captureSession.removeInput(captureSession.inputs[0])
    }
    
    private func setOutput() {
        let captureMetadataOutput = AVCaptureMetadataOutput()
        captureSession.addOutput(captureMetadataOutput)
        
        captureMetadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
        captureMetadataOutput.metadataObjectTypes = [AVMetadataObject.ObjectType.qr]
    }
    
    private func setVideoPreview() {
        videoPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        videoPreviewLayer?.videoGravity = AVLayerVideoGravity.resizeAspectFill
        //videoPreviewLayer?.frame = view.layer.bounds
        view.layer.addSublayer(videoPreviewLayer!)
    }
    
    private func setQrCodeFrameView() {
        qrCodeFrameView = UIView()
        if let qrCodeFrameView = qrCodeFrameView {
            qrCodeFrameView.layer.borderColor = UIColor.green.cgColor
            qrCodeFrameView.layer.borderWidth = 2
            view.addSubview(qrCodeFrameView)
            view.bringSubviewToFront(qrCodeFrameView)
        }
    }
    
    private func noQrCodeDetected(metadataObjects: [AVMetadataObject]) -> Bool {
        return metadataObjects.count == 0
    }
    
    private func setVideoOrientation(layer: AVCaptureConnection, orientation: AVCaptureVideoOrientation) {
        layer.videoOrientation = orientation
    }
}

extension LAZQrCodeReaderViewController: AVCaptureMetadataOutputObjectsDelegate {
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        guard let metadataObj = metadataObjects.first as? AVMetadataMachineReadableCodeObject else {
            qrCodeFrameView?.frame = CGRect.zero
            return
        }
        
        if metadataObj.type == AVMetadataObject.ObjectType.qr {
            setQrCodeBound(metadataObj: metadataObj)
            
            if metadataObj.stringValue != nil && metadataObj.stringValue != "" {
                captureSession.stopRunning()
                delegate?.qrCodeDidScan(rewardCardCode: metadataObj.stringValue!)
                navigationController?.popViewController(animated: true)
            }
        }
    }
    
    private func setQrCodeBound(metadataObj: AVMetadataMachineReadableCodeObject) {
        let barCodeObject = videoPreviewLayer?.transformedMetadataObject(for: metadataObj)
        qrCodeFrameView?.frame = barCodeObject!.bounds
    }
}
