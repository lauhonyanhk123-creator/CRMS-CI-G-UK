package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubbieGateStatus {
    
    private Long operativeId;
    
    private Boolean isHMRCVerified;
    
    private Boolean isCSCSValid;
    
    private Boolean isRAMSValid;
    
    private Boolean isInductionValid;
    
    private Boolean isPlantTicketValid;
    
    private Boolean isGateOpen;
    
    private String statusMessage;

    public boolean isHMRCVerified() { return Boolean.TRUE.equals(isHMRCVerified); }
    public boolean isCSCSValid() { return Boolean.TRUE.equals(isCSCSValid); }
    public boolean isRAMSValid() { return Boolean.TRUE.equals(isRAMSValid); }
    public boolean isInductionValid() { return Boolean.TRUE.equals(isInductionValid); }
    public boolean isPlantTicketValid() { return Boolean.TRUE.equals(isPlantTicketValid); }
    public boolean isGateOpen() { return Boolean.TRUE.equals(isGateOpen); }

}