import { NativeModules } from 'react-native';

const {ReactNativeSendSms} = NativeModules;

type SubscriptionInfo = {
    subscriptionId: number;
    phoneNumber: string;
    carrierName: string;
}

const getActiveSubscriptionInfo = (): Promise<SubscriptionInfo> => {
    return ReactNativeSendSms.getActiveSubscriptionInfo();
}

const sendSMS = (toAddress: Array<string> ,messageText: string, subscriptionId?: number): Promise<void> => {
    return !subscriptionId ? ReactNativeSendSms.sendSMSDefault(toAddress, messageText) :ReactNativeSendSms.sendSMS(toAddress, subscriptionId, messageText) ;
}

export default {
  getActiveSubscriptionInfo,
  sendSMS,
}



