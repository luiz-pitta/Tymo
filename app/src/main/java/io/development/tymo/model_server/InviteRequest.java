package io.development.tymo.model_server;

public class InviteRequest {

    private String email;
    private long id_act;
    private int status;
    private int type;

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

}
