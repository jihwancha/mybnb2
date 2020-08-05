package mybnb;

import mybnb.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{

    @Autowired
    private AlarmRepository alarmRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayApproved_Notify(@Payload PayApproved payApproved){

        if(payApproved.isMe()){
            //System.out.println("##### listener Notify : " + payApproved.toJson());
            addNotificationHistory("(guest)" + payApproved.getGuest(), "PayApproved");
            addNotificationHistory("(host)" + payApproved.getHost(), "PayApproved");
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_Notify(@Payload PayCanceled payCanceled){

        if(payCanceled.isMe()){
            //System.out.println("##### listener Notify : " + payCanceled.toJson());
            addNotificationHistory("(guest)" + payCanceled.getGuest(), "PayCanceled");
            addNotificationHistory("(host)" + payCanceled.getHost(), "PayCanceled");
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverBooked_Notify(@Payload Booked booked){

        if(booked.isMe()){
            //System.out.println("##### listener Notify : " + booked.toJson());
            addNotificationHistory("(guest)" + booked.getGuest(), "Booked");
            addNotificationHistory("(host)" + booked.getHost(), "Booked");
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverBookCanceled_Notify(@Payload BookCanceled bookCanceled){

        if(bookCanceled.isMe()){
            //System.out.println("##### listener Notify : " + bookCanceled.toJson());
            addNotificationHistory("(guest)" + bookCanceled.getGuest(), "BookCanceled");
            addNotificationHistory("(host)" + bookCanceled.getHost(), "BookCanceled");
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReviewRegistered_Notify(@Payload ReviewRegistered reviewRegistered){

        if(reviewRegistered.isMe()){
            //System.out.println("##### listener Notify : " + reviewRegistered.toJson());
            addNotificationHistory("(host)" + reviewRegistered.getHost(), "ReviewRegistered");
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverAuthApproved_Notify(@Payload AuthApproved authApproved){

        if(authApproved.isMe()){
            //System.out.println("##### listener Notify : " + authApproved.toJson());
            addNotificationHistory("(host)" + authApproved.getHost(), "AuthApproved");
        }
    }

    private void addNotificationHistory(String receiver, String message) {
        Alarm history = new Alarm();
        history.setReceiver(String.valueOf(receiver));
        history.setMessage(message);
        alarmRepository.save(history);
    }

}
