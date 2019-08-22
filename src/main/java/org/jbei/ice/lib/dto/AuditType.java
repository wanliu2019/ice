package org.jbei.ice.lib.dto;

/**
 * @author Hector Plahar
 */
public enum AuditType {

    READ("+r"),
    EDIT("e");

    private String abbrev;

    AuditType(String abbrev) {
        this.abbrev = abbrev;
    }

    public String getAbbrev() {
        return abbrev;
    }
}
