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
}