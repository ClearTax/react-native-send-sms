import { NativeModules } from 'react-native';

const {ReactNativeSendSms} = NativeModules;

type SubscriptionInfo = {
    subscriptionId: number;
    phoneNumber: string;
    carrierName: string;
}

export const getActiveSubscriptionInfo = (): Promise<SubscriptionInfo> => {
    return ReactNativeSendSms.getActiveSubscriptionInfo();
}

export const sendSMS = (toAddress: Array<string> ,messageText: string, subscriptionId?: number): Promise<void> => {
    return !subscriptionId ? ReactNativeSendSms.sendSMSDefault(toAddress, messageText) :ReactNativeSendSms.sendSMS(toAddress, subscriptionId, messageText) ;
}

