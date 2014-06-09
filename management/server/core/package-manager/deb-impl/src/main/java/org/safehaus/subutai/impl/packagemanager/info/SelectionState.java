package org.safehaus.subutai.impl.packagemanager.info;

/**
 * Debian package selection states. Refer to dpkg man pages for more info.
 *
 */
public enum SelectionState {

    UNKNOWN('u'),
    INSTALL('i'),
    HOLD('h'),
    DEINSTALL('r'),
    PURGE('p');

    private final char abbrev;

    private SelectionState(char abbrev) {
        this.abbrev = abbrev;
    }

    public char getAbbrev() {
        return abbrev;
    }

    public static SelectionState getByAbbrev(char abbrev) {
        for(SelectionState s : values()) {
            if(s.abbrev == abbrev) return s;
        }
        return UNKNOWN;
    }
}
