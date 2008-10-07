package com.okay.validate;

public class WhiteFilter extends AbstractFilter {
    
    protected int filter(int p) {
        if (isWhite(p)) {
            return 1;
        }
        else {
            return 0;
        }
    }
    private boolean isWhite(int p) {
        return (p & 0x0ff) > 240 && (p >> 8 & 0x0ff) > 240
                && (p >> 16 & 0xff) > 240;
    }
}