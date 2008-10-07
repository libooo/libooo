package com.okay.validate;

public abstract class AbstractFilter  implements Filter {
    public void doFilter(int[][] data) {
        int h = data.length;
        if (h<=0)
            return;
        int w = data[0].length;
        if (w<=0)
            return ;
        for(int i=0;i<h;i++) {
            for(int j=0;j<w;j++) {
                data[i][j] = filter(data[i][j]);
            }
        }
    }
    
    protected abstract int filter(int p);
}