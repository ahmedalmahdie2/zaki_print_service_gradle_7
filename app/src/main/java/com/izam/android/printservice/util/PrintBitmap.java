package com.izam.android.printservice.util;


import android.graphics.Bitmap;
import android.graphics.Paint;

import com.zj.btsdk.PrintPic;

public class PrintBitmap extends PrintPic {

    public void drawImage(float x, float y, Bitmap e) {
        if(e == null) return;
        try {
            this.canvas.drawBitmap(e, x, y, (Paint)null);
            if(this.length < y + (float)e.getHeight()) {
                this.length = y + (float)e.getHeight();
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }
}
