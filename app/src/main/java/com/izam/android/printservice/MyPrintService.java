package com.izam.android.printservice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.print.PrintAttributes;
import android.print.PrintJobInfo;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Base64;
import android.util.Log;
import androidx.core.content.ContextCompat;

import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import net.sf.andpdf.nio.ByteBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


class IzamPrintService extends PrintService {
    private static final String PRINTER = "iZAM Print Service";
    private static final String PRINTER_ID = "aaa";
    private static final float PRINTER_WIDTH = 534;
    PrinterInfo mThermalPrinter;

    @Override
    public void onCreate() {
        mThermalPrinter = new PrinterInfo.Builder(generatePrinterId(PRINTER_ID),
                PRINTER, PrinterInfo.STATUS_IDLE).build();
    }

    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        Log.d("myprinter", "IzamPrintService#onCreatePrinterDiscoverySession() called");
        return new ThermalPrinterDiscoverySession(mThermalPrinter);
    }

    Bitmap CropBitmapTransparency(Bitmap sourceBitmap)
    {
        int minX = sourceBitmap.getWidth();
        int minY = sourceBitmap.getHeight();
        int maxX = -1;
        int maxY = -1;
        for(int y = 0; y < sourceBitmap.getHeight(); y++)
        {
            for(int x = 0; x < sourceBitmap.getWidth(); x++)
            {
                int alpha = (sourceBitmap.getPixel(x, y) >> 24) & 255;
                if(alpha > 0)   // pixel is not 100% transparent
                {
                    if(x < minX)
                        minX = x;
                    if(x > maxX)
                        maxX = x;
                    if(y < minY)
                        minY = y;
                    if(y > maxY)
                        maxY = y;
                }
            }
        }
        if((maxX < minX) || (maxY < minY))
            return null; // Bitmap is entirely transparent

        // crop bitmap to non-transparent area and return:
        return Bitmap.createBitmap(sourceBitmap, minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
    }

    Bitmap getScaledBitmap(Bitmap bitmap, float width, boolean filter)
    {
        return  Bitmap.createScaledBitmap(
                        bitmap,
                (int) width,
                        Math.round(((float) bitmap.getHeight()) * width / ((float) bitmap.getWidth())),
                filter
                );
    }


    private static Bitmap removeMargins2(Bitmap bmp, int color) {
        // TODO Auto-generated method stub

        long dtMili = System.currentTimeMillis();
        int MTop = 0, MBot = 0, MLeft = 0, MRight = 0;
        boolean found1 = false, found2 = false;

        int[] bmpIn = new int[bmp.getWidth() * bmp.getHeight()];
        int[][] bmpInt = new int[bmp.getWidth()][bmp.getHeight()];

        bmp.getPixels(bmpIn, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
                bmp.getHeight());

        for (int ii = 0, contX = 0, contY = 0; ii < bmpIn.length; ii++) {
            bmpInt[contX][contY] = bmpIn[ii];
            contX++;
            if (contX >= bmp.getWidth()) {
                contX = 0;
                contY++;
                if (contY >= bmp.getHeight()) {
                    break;
                }
            }
        }

        for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
            // looking for MTop
            for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MTop 2", "Pixel found @" + hP);
                    MTop = hP;
                    found2 = true;
                    break;
                }
            }
        }

        found2 = false;

        for (int hP = bmpInt[0].length - 1; hP >= 0 && !found2; hP--) {
            // looking for MBot
            for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MBot 2", "Pixel found @" + hP);
                    MBot = bmp.getHeight() - hP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
            // looking for MLeft
            for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MLeft 2", "Pixel found @" + wP);
                    MLeft = wP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int wP = bmpInt.length - 1; wP >= 0 && !found2; wP--) {
            // looking for MRight
            for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MRight 2", "Pixel found @" + wP);
                    MRight = bmp.getWidth() - wP;
                    found2 = true;
                    break;
                }
            }

        }
        found2 = false;

        int sizeY = bmp.getHeight() - MBot - MTop, sizeX = bmp.getWidth()
                - MRight - MLeft;

        Bitmap bmp2 = Bitmap.createBitmap(bmp, MLeft, MTop, sizeX, sizeY);
        dtMili = (System.currentTimeMillis() - dtMili);
        Log.e("Margin   2",
                "Time needed " + dtMili + "mSec\nh:" + bmp.getWidth() + "w:"
                        + bmp.getHeight() + "\narray x:" + bmpInt.length + "y:"
                        + bmpInt[0].length);
        return bmp2;
    }

    void doPrintJobOnBluetoothPrinter(PrintJob printJob)
    {
        Bitmap[] bitmaps = null;

        if (printJob.isQueued()) {
            printJob.start();
        }
        final PrintJobInfo info = printJob.getInfo();
        final File file = new File(getFilesDir(), info.getLabel());

        InputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(printJob.getDocument().getData().getFileDescriptor());
            out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            out.flush();
            out.close();

        } catch (IOException ioe) {

        }

        FileChannel channel = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            channel = raf.getChannel();
            ByteBuffer bb = ByteBuffer.NEW(channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, channel.size()));
            byte[] tempBytes = new byte[bb.remaining()];
            bb.get(tempBytes, 0, tempBytes.length);

            String base64String= Base64.encodeToString(tempBytes, 0);
//            byte[] tempBytes02 = Base64.decode(base64String, 0);
            Log.d("bytearray base64", base64String != null ? base64String : "is NULL!");
            PDDocument document = PDDocument.load(tempBytes);
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            bitmaps = new Bitmap[document.getNumberOfPages()];
            Log.d("bitmaps", bitmaps.length + "");

            float scale = 1;
            if(bitmaps.length > 0)
            {
                final Bitmap tempBitmap = pdfRenderer.renderImage(0, 1, Bitmap.Config.RGB_565);
                scale = PRINTER_WIDTH / (float) tempBitmap.getWidth();
                Log.d("scale", "" + scale + ", Width: ");
            }

            for(int i = 0; i < bitmaps.length; i++)
            {
                bitmaps[i] = CropBitmapTransparency(removeMargins2(pdfRenderer.renderImage(i, scale, Bitmap.Config.RGB_565), Color.WHITE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {

            Log.d("doPrintJobOnBluetooth","bluetooth " + printJob.getDocument().getInfo().getName());
            // Your printing code/method HERE
            try{
                EscPosPrinterCommands printerCommands = new EscPosPrinterCommands(BluetoothPrintersConnections.selectFirstPaired());
                try {
                    printerCommands.connect();
                    Log.d("getAsyncEscPosPrinter", "printerCommands.connect()");

                    printerCommands.reset();
                    Log.d("getAsyncEscPosPrinter", "printerCommands.reset()");

                    ArrayList<Byte> allBytes = new ArrayList<Byte>();

                    for(int i =0; i < bitmaps.length; i++)
                    {
                        final byte[] bytes = printerCommands.bitmapToBytes(bitmaps[i]);
                        for(int j =0; j < bytes.length; j++)
                        {
                            allBytes.add(bytes[j]);
                        }
                    }
                    Byte[] array = (Byte[]) allBytes.toArray(new Byte[allBytes.size()]);
                    byte[] array02 = new byte[array.length];
                    int i = 0;
                    for(Byte b: array)
                        array02[i++] = b.byteValue();

                    printerCommands.printImage(array02);

                    printerCommands.feedPaper(255);
                    printerCommands.feedPaper(255);
                    Log.d("getAsyncEscPosPrinter", "printerCommands.feedPaper()");

                    printerCommands.cutPaper();
                    Log.d("getAsyncEscPosPrinter", "printerCommands.cutPaper()");
                } catch (EscPosConnectionException e) {
                    e.printStackTrace();
                }
            }catch(Exception e)
            {
                Log.d("createWebPrintJob Error",e.toString());
            }
        }
        printJob.complete();
//        try{
//            in.close();
//        } catch(Exception ee)
//        {
//
//        }
    }

    @Override
    protected void onPrintJobQueued(PrintJob printJob) {
        if (printJob.isQueued()) {
            Log.d("myprinter", "queued: " + printJob.getId().toString());
            Log.d("myprinter", "document info: " + printJob.getDocument().getInfo());
            printJob.start();
        }

        doPrintJobOnBluetoothPrinter(printJob);
    }

    @Override
    protected void onRequestCancelPrintJob(PrintJob printJob) {
        Log.d("myprinter", "canceled: " + printJob.getId().toString());
        printJob.cancel();
    }
}

class ThermalPrinterDiscoverySession extends PrinterDiscoverySession {

    private PrinterInfo printerInfo;

    ThermalPrinterDiscoverySession(PrinterInfo printerInfo) {
        PrintAttributes.MediaSize custom58 = new PrintAttributes.MediaSize("58Thermal" , "58mm_Thermal", 2200,5000);
        custom58.asPortrait();
        PrintAttributes.MediaSize custom80 = new PrintAttributes.MediaSize("80Thermal" , "80mm_Thermal", 3000,7100);
        custom80.asPortrait();

        PrinterCapabilitiesInfo.Builder capabilitiesBuilder =
                new PrinterCapabilitiesInfo.Builder(printerInfo.getId());

        capabilitiesBuilder.addMediaSize(custom58, true);
        capabilitiesBuilder.addMediaSize(custom80, false);
        capabilitiesBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A3, false);
        capabilitiesBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, false);
        capabilitiesBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A5, false);
        capabilitiesBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A7, false);
        capabilitiesBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

        capabilitiesBuilder.addResolution(new PrintAttributes.Resolution("200dpi","paperRoll",203,203), true);
        capabilitiesBuilder.setColorModes(PrintAttributes.COLOR_MODE_MONOCHROME, PrintAttributes.COLOR_MODE_MONOCHROME);
        this.printerInfo = new PrinterInfo.Builder(printerInfo)
                .setCapabilities(capabilitiesBuilder.build())
                .build();
    }

    @Override
    public void onStartPrinterDiscovery(List<PrinterId> priorityList) {
        List<PrinterInfo> printers = new ArrayList<PrinterInfo>();
        printers.add(printerInfo);
        addPrinters(printers);
    }

    @Override
    public void onStopPrinterDiscovery() {

    }

    @Override
    public void onValidatePrinters(List<PrinterId> printerIds) {

    }

    @Override
    public void onStartPrinterStateTracking(PrinterId printerId) {

    }

    @Override
    public void onStopPrinterStateTracking(PrinterId printerId) {

    }

    @Override
    public void onDestroy() {

    }
}