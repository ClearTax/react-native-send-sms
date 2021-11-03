import { NativeModules, PermissionsAndroid } from "react-native";

const { ReactNativeSendSms } = NativeModules;

type SubscriptionInfo = {
  subscriptionId: number;
  phoneNumber: string;
  carrierName: string;
  simSlotIndex: number;
};

const getActiveSubscriptionInfo = (): Promise<Array<SubscriptionInfo>> => {
  return new Promise<Array<SubscriptionInfo>>(async (resolve, reject) => {
    try {
      const permissionStatus = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE,
        {
          title: "Need to read phone state",
          message: "Need to read phone state to utilize dual sim",
          buttonPositive: "Grant",
        }
      );
      if (permissionStatus === PermissionsAndroid.RESULTS.GRANTED) {
        const subscriptionList =
          await ReactNativeSendSms.getActiveSubscriptionInfo();
        resolve(subscriptionList);
      } else {
        reject("Permission not granted");
      }
    } catch (err) {
      reject(err);
    }
  });
};

const sendSingleSMS = (
  toAddress: String,
  messageText: string,
  subscriptionId?: number
): Promise<void> => {
  return new Promise<void>(async (resolve, reject) => {
    try {
      const permissionStatus = await PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.SEND_SMS,
        PermissionsAndroid.PERMISSIONS.READ_SMS,
      ]);
      if (
        permissionStatus[PermissionsAndroid.PERMISSIONS.SEND_SMS] ===
          PermissionsAndroid.RESULTS.GRANTED &&
        permissionStatus[PermissionsAndroid.PERMISSIONS.READ_SMS] ===
          PermissionsAndroid.RESULTS.GRANTED
      ) {
        if (subscriptionId) {
          await ReactNativeSendSms.sendSingleSMS(
            toAddress,
            subscriptionId,
            messageText
          );
        } else {
          await ReactNativeSendSms.sendSingleSMSDefault(toAddress, messageText);
        }
        resolve();
      } else {
        reject("Permission not granted");
      }
    } catch (err) {
      reject(err);
    }
  });
};

const sendBulkSMS = (
  toAddress: Array<string>,
  messageText: string,
  subscriptionId?: number
): Promise<void> => {
  return new Promise<void>(async (resolve, reject) => {
    try {
      const permissionStatus = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.SEND_SMS,
        {
          title: "Need Send SMS permission",
          message: "Need Send SMS permission for sending the SMS",
          buttonPositive: "Grant",
        }
      );
      if (permissionStatus === PermissionsAndroid.RESULTS.GRANTED) {
        if (subscriptionId) {
          await ReactNativeSendSms.sendBulkSMS(
            toAddress,
            subscriptionId,
            messageText
          );
        } else {
          await ReactNativeSendSms.sendBulkSMSDefault(toAddress, messageText);
        }
        resolve();
      } else {
        reject("Permission not granted");
      }
    } catch (err) {
      reject(err);
    }
  });
};

export default {
  getActiveSubscriptionInfo,
  sendBulkSMS,
  sendSingleSMS,
};
