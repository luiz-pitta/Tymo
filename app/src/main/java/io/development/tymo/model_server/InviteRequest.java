package io.development.tymo.model_server;

import java.util.ArrayList;
import java.util.List;

public class InviteRequest {

    private String email;
    private long id_act;
    private ArrayList<Long> ids_act = new ArrayList<>();
    private int status;
    private int type;
    private long date_time_now;
    private List<Integer> repeat_list_accepted = new ArrayList<>();

    public void setRepeatListAccepted(List<Integer> repeat_list_accepted) {
        this.repeat_list_accepted.addAll(repeat_list_accepted);
    }

    public List<Integer> getRepeatListAccepted() {
        return repeat_list_accepted;
    }

    public void setDateTimeNow(long date_time_now) {
        this.date_time_now = date_time_now;
    }

    public long getDateTimeNow() {
        return date_time_now;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIdAct(long id_act) {
        this.id_act = id_act;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void addIds(long id){
        ids_act.add(id);
    }

}
