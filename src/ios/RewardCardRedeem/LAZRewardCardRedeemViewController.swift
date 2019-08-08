//
//  LAZRewardCardRedeemViewController.swift
//  learningazquizroom
//
//  Created by Hubert Hung on 7/19/19.
//  Copyright © 2019 Learning A-Z. All rights reserved.
//

import UIKit
import LAZCore
import AVFoundation

class LAZRewardCardRedeemViewController: UIViewController, UITextFieldDelegate, LAZQrCodeReaderViewControllerDelegate {

    @IBOutlet weak var scanQrCodeButton: UIButton!
    @IBOutlet weak var rewardCardCodeTextField: UITextField!
    @IBOutlet weak var messageLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "Redeem Reward Card"
        rewardCardCodeTextField.delegate = self
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if textField === rewardCardCodeTextField{
            willRedeem()
        }
        return true
    }
    
    func qrCodeDidScan(rewardCardCode: String) {
        redeem(rewardCardCode: rewardCardCode)
    }
    
    @IBAction func scanQrCodeButtonTapped(_ sender: UIButton) {
        let qrCodeReaderViewController = LAZQrCodeReaderViewController(nibName: "LAZQrCodeReaderViewController", bundle: nil)
        qrCodeReaderViewController.delegate = self
        navigationController?.pushViewController(qrCodeReaderViewController, animated: true)
    }
    
    @IBAction func redeemButtonTapped(_ sender: UIButton) {
        willRedeem()
    }
    
    private func willRedeem() {
        if let rewardCardCode = rewardCardCodeTextField.text {
            rewardCardCodeTextField.resignFirstResponder()
            redeem(rewardCardCode: rewardCardCode)
        }
    }
    
    private func redeem(rewardCardCode: String) {
        if rewardCardCode == "" {
            return
        }
        
        let params = ["reward_card_code": rewardCardCode]
        KAZNetworkService.sharedInstance.makePostRequest(KAZNetworkPath.rewardCardRedeem, tokens: [], params: params, datasources: [], showSpinner: true) { (result, error, shouldRetry) in
            let redeemStatus = result?["redeemStatus"] as? String ?? ""
            let starAmount = result?["starAmount"] as? Int ?? 0
            switch redeemStatus {
            case "successful":
                self.messageLabel.text = "You've earned \(starAmount) stars!"
            case "invalid":
                self.messageLabel.text = "Invalid code."
            case "used":
                self.messageLabel.text = "This code has been redeemed."
            default:
                self.messageLabel.text = "There was an error redeeming the code."
            }
        }
    }
}
