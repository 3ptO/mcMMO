package com.gmail.nossr50.party.runnables;

import com.gmail.nossr50.party.PartyManager;

public class PartiesLoader implements Runnable {
    @Override
    public void run() {
        PartyManager.loadParties();
    }
}
