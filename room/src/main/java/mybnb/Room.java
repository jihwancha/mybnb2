package mybnb;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Room_table")
public class Room {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private Long price;
    private String address;
    private String host;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getPrice() {
        return price;
    }
    public void setPrice(Long price) {
        this.price = price;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    @PostPersist
    public void onPostPersist(){
        // 등록시 트랜잭션을 통합을 위해 인증서비스 직접 호출
        {
            mybnb.external.Auth auth = new mybnb.external.Auth();
            auth.setRoomId(getId());
            auth.setName(getName());
            auth.setHost(getHost());
            auth.setStatus("AuthApproved");

            try {
                RoomApplication.applicationContext.getBean(mybnb.external.AuthService.class)
                        .auth(auth);
            }catch(Exception e) {
                throw new RuntimeException("인증서비스 호출 실패입니다.", e);
            }
        }

        RoomRegistered roomRegistered = new RoomRegistered();
        BeanUtils.copyProperties(this, roomRegistered);
        roomRegistered.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        RoomChanged roomChanged = new RoomChanged();
        BeanUtils.copyProperties(this, roomChanged);
        roomChanged.publishAfterCommit();
    }

    @PostRemove
    public void onPostRemove(){
        RoomDeleted roomDeleted = new RoomDeleted();
        BeanUtils.copyProperties(this, roomDeleted);
        roomDeleted.publishAfterCommit();
    }

}
