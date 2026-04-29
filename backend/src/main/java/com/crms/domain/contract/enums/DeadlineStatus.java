package com.crms.domain.contract.enums;

/**
 * Represents the deadline status for pay-less notice s.111 enforcement.
 */
public enum DeadlineStatus {
    /**
     * The pay-less notice deadline has passed.
     */
    DEADLINE_PASSED,
    
    /**
     * The pay-less notice deadline is approaching (within 2 days).
     */
    DEADLINE_APPROACHING,
    
    /**
     * The pay-less notice deadline is active (more than 2 days remaining).
     */
    DEADLINE_ACTIVE,
    
    /**
     * No deadline applies (no application date set).
     */
    NO_DEADLINE
}
