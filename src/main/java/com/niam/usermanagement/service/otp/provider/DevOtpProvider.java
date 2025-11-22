package com.niam.usermanagement.service.otp.provider;

import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.model.payload.request.OtpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Slf4j
@RequiredArgsConstructor
@Service("devOtpProvider")
public class DevOtpProvider implements OtpProvider {
    private final UMConfigFile configFile;

    @Override
    public void send(OtpRequest request) {
        log.warn("DEV OTP for {} => {}", request.destination(), request.code());
        log.warn("MASTER OTP (bypass code) => {}", configFile.getOtpDevMasterCode());
    }

    /**
     * Used only for development to bypass normal OTP flow.
     */
    public boolean isMasterCode(String input) {
        return configFile.getOtpDevMasterCode().equals(input);
    }
}