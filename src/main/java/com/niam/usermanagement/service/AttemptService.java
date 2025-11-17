package com.niam.usermanagement.service;

public interface AttemptService {
    boolean registerFailureForUsername(String username);

    boolean registerFailureForIp(String ip);

    void registerSuccess(String username, String ip);

    boolean isUsernameBlocked(String username);

    boolean isIpBlocked(String ip);

    void resetUsername(String username);

    void resetIp(String ip);
}