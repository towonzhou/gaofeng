package com.redflag.Passenger;

import org.json.JSONObject;

public interface PassengerResponse {
    enum STATE {
        NORMAL,
        WANTING_TAXI,

    };


    public void startTransaction();
    public void endTransaction();
    public void rollbackTransaction();

    public void sendPID();
    public void onGotACar(JSONObject json);
    public void onBeingRejectACar();
    public void onConfirmTaxi();
    public void onError(Exception e);
}
