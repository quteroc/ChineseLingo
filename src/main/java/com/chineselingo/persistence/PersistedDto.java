package com.chineselingo.persistence;

import java.time.Instant;

public abstract class PersistedDto {

    private long lastSave;

    public long getLastSave() {
        return lastSave;
    }

    protected void setLastSave(long lastSave) {
        this.lastSave = lastSave;
    }

    /** wywoływane przez repozytorium */
    public void markSavedNow() {
        this.lastSave = Instant.now().getEpochSecond();
    }
}
