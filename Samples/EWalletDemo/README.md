# T104 E-Wallet Demo #

## About T104 E-Wallet Demo ##
The E-Wallet demo presents a proof-of-concept of using the T104 card as a traditional stored-value card with the enhancement of a screen to display the stored value on the card on demand.

## Demo Components ##
The demo components consists of a wallet applet calling the T104 Open API and an Android application that simulates as both a vending machine to reset the card values and load funds into the card as well a merchant terminal to demonstrate the ability to deduct funds from the card.

## Installing Ready-to-Go Executables ##
For the convenience of the demo, executables in the form of an applet CAP file for the card and an Android APK for an Android smartphone with NFC capabilities are ready for users to download and use immediately. Alternatively, you might want to build your own demo CAP file for the card and the Android APK on your own.

To locate the card applet CAP file, you can find it in the `\Samples\EWalletDemo\T104EWalletApplet\bin\EWalletApplet\javacard\EWalletApplet.cap` location and for the Android APK file, you can find it in the `\Samples\EWalletDemo\T104EWalletDemo\app\build\outputs\apk\debug\app-debug.apk` location.

You will need to have knowledge of uploading CAP files to cards and using the Android smartphone's developer mode to sideload the APK for the demo.

## Post-Installation Steps ##
After installing the CAP file to the card, follow the steps for the T104 card.
1. Select the applet `43 57 4D 49 4E 49 45 57 41 4C 4C 45 54 00`.
2. Initilize the card applet by issuing the APDU manually `CLA=00, INS=FF, P1=00, P2=00`.
3. Use the T104Manager to enable E-Wallet access for the applet AID `43 57 4D 49 4E 49 45 57 41 4C 4C 45 54 00`. Without assigning access the E-Wallet demo applet to the E-Wallet Access Control List, access to the funds would be denied.

The Android application does not require any further configuration after sideloading the APK into the Android smartphone. Note that NFC needs to be enabled for use.

## Loading Funds ##
You will need to load demo funds to begin the demo. 
1. Open the `T104EWalletDemo` Android application and click on the top right hand corner selection to select `Vending Machine Mode` to begin loading funds.
2. In the `Vending Machine` activity page, select the dropdown spinner to select `Load Wallet Funds` if it has not been selected.
3. Enter between 1 to 1000 (positive integer only) for the amount of credits you want to load into the card and press the `Execute` button.
4. Switch on the power button on the E-Wallet applet equipped T104 card and tap and hold the card against the NFC reading area on the Android smartphone and you will be prompted with an icon and a prompt message on the bottom of the screen to tap and hold and finally remove the card when done.
5. Press the Back button to return to the main menu for the E-Wallet Demo when done or when you want to exit an activity.

## Resetting Card Funds ##
In the event you may want to reset all the funds (Balance, Payment and Loaded Funds) for a new demo, follow these steps.
1. Open the `T104EWalletDemo` Android application and click on the top right hand corner selection to select `Vending Machine Mode` to begin loading funds.
2. In the `Vending Machine` activity page, select the dropdown spinner to select `Reset Wallet Funds` if it has not been selected.
3. Switch on the power button on the E-Wallet applet equipped T104 card and tap and hold the card against the NFC reading area on the Android smartphone and you will be prompted with an icon and a prompt message on the bottom of the screen to tap and hold and finally remove the card when done.
4. Press the Back button to return to the main menu for the E-Wallet Demo when done or when you want to exit an activity.

## Simulated Spending of Card Funds ##
You may simulate the expenditure of credits loaded to the card by following these steps.
1. Open the `T104EWalletDemo` Android application and click on the top right hand corner selection to select `Merchant Terminal Mode` to begin loading funds.
2. In the `Merchant Terminal` activity page, Enter between 1 to 1000 (positive integer only) for the amount of credits you want to deduct from the card and press the `BEGIN PAYMENT` button.
3. Switch on the power button on the E-Wallet applet equipped T104 card and tap and hold the card against the NFC reading area on the Android smartphone and you will be prompted with an icon and a prompt message on the bottom of the screen to tap and hold and finally remove the card when done.
4. Press the Back button to return to the main menu for the E-Wallet Demo when done or when you want to exit an activity.

## Viewing Card Funds and Transaction Logs ##
You may simulate the viewing of all the card funds and card transaction logs following these steps.
1. Open the `T104EWalletDemo` Android application. If you are in any of the other activity pages, press Back button to return back to the Main activity (Main Menu).
2. Switch on the power button on the E-Wallet applet equipped T104 card and tap and hold the card against the NFC reading area on the Android smartphone and you will be prompted with an icon and a prompt message on the bottom of the screen to tap and hold and finally remove the card when done.
3. The current card funds should be available with a list of up to 100 most recent transaction activities. The transaction logs will clean themselves and be reused in a cyclic fashion after 100 logs have been populated in the card.

## THETAKey Product Page ##
* [https://thothtrust.com/products.html#thetakey](https://thothtrust.com/products.html#thetakey)
* Email to purchase a sample [here](mailto:sales@thothtrust.com).