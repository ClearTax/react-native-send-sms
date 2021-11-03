import { NativeModules, Permission, PermissionsAndroid } from "react-native";

const { ReactNativeSendSms } = NativeModules;

const AndroidPermisisons = {
  READ_PHONE_STATE: "android.permission.READ_PHONE_STATE",
  SEND_SMS: "android.permission.SEND_SMS",
};

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
        AndroidPermisisons.READ_PHONE_STATE as Permission,
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

const sendSMS = (
  toAddress: Array<string>,
  messageText: string,
  subscriptionId?: number
): Promise<void> => {
  return new Promise<void>(async (resolve, reject) => {
    try {
      const permissionStatus = await PermissionsAndroid.request(
        AndroidPermisisons.SEND_SMS as Permission,
        {
          title: "Need Send SMS permission",
          message: "Need Send SMS permission for sending the SMS",
          buttonPositive: "Grant",
        }
      );
      if (permissionStatus === PermissionsAndroid.RESULTS.GRANTED) {
        if (subscriptionId) {
          await ReactNativeSendSms.sendSMS(
            toAddress,
            subscriptionId,
            messageText
          );
        } else {
          await ReactNativeSendSms.sendSMSDefault(toAddress, messageText);
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
  sendSMS,
};
